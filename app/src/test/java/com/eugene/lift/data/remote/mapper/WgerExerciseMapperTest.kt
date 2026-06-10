package com.eugene.lift.data.remote.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.eugene.lift.data.remote.dto.WgerExerciseDto
import com.eugene.lift.data.remote.dto.WgerExerciseTranslationDto
import com.eugene.lift.data.remote.dto.WgerPaginatedResponseDto
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.ExerciseSource
import com.eugene.lift.domain.model.MeasureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WgerExerciseMapperTest {

    private val languageIdByCode = mapOf(
        "en" to 2,
        "es" to 4
    )

    @Test
    fun `maps fixture into remote page and local exercise`() {
        val response = loadFixture("fixtures/remote/wger-exercise-page.json")

        val page = response.toRemoteExercisePage(
            preferredLanguageCode = "en",
            languageIdByCode = languageIdByCode
        )
        val exercise = page.exercises.single().toExercise(
            existingExercise = com.eugene.lift.data.local.entity.ExerciseEntity(
                id = "existing-local-id",
                name = "Bench Press",
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = "",
                imagePath = null
            ),
            syncedAt = 1710000000000,
            syncVersion = 1
        )

        assertEquals(1, page.totalCount)
        assertEquals("next-page", page.nextPageUrl)
        assertEquals(1, page.exercises.size)
        assertEquals(42, page.exercises.single().remoteId)
        assertEquals("Bench Press", page.exercises.single().name)
        assertEquals("Strength", page.exercises.single().categoryName)
        assertEquals("https://wger.de/media/exercise-images/9/main-image.png", page.exercises.single().primaryImageUrl)
        assertEquals(listOf("Bench"), page.exercises.single().equipmentNames)
        assertEquals(listOf("Chest"), page.exercises.single().primaryMuscleNames)
        assertEquals(listOf("Triceps", "Front deltoid"), page.exercises.single().secondaryMuscleNames)

        assertEquals("existing-local-id", exercise.id)
        assertEquals(ExerciseCategory.MACHINE, exercise.category)
        assertEquals(MeasureType.REPS_AND_WEIGHT, exercise.measureType)
        assertEquals(
            listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS),
            exercise.bodyParts
        )
        assertEquals(ExerciseSource.WGER, exercise.source)
        assertEquals(42, exercise.remoteId)
        assertEquals(1710000000000, exercise.lastSyncedAt)
        assertEquals(1, exercise.syncVersion)
    }

    @Test
    fun `unknown category falls back without breaking muscle mapping`() {
        val payload = loadFixture("fixtures/remote/wger-unknown-category-page.json")
            .toRemoteExercisePage(
                preferredLanguageCode = "en",
                languageIdByCode = languageIdByCode
            )
            .exercises
            .single()
        val exercise = payload.toExercise()

        assertEquals(ExerciseCategory.REPS_ONLY, exercise.category)
        assertEquals(MeasureType.REPS_ONLY, exercise.measureType)
        assertEquals(listOf(BodyPart.BICEPS, BodyPart.FOREARMS), exercise.bodyParts)
    }

    @Test
    fun `exercise without image or equipment maps cleanly`() {
        val payload = loadFixture("fixtures/remote/wger-no-image-page.json")
            .toRemoteExercisePage(
                preferredLanguageCode = "en",
                languageIdByCode = languageIdByCode
            )
            .exercises
            .single()
        val exercise = payload.toExercise(
            existingExercise = com.eugene.lift.data.local.entity.ExerciseEntity(
                id = "cardio-1",
                name = "Air Bike Sprint",
                category = ExerciseCategory.CARDIO,
                measureType = MeasureType.DISTANCE_TIME,
                instructions = "",
                imagePath = "air_bike_local"
            )
        )

        assertNull(payload.primaryImageUrl)
        assertEquals(emptyList<String>(), payload.equipmentNames)
        assertEquals(ExerciseCategory.CARDIO, exercise.category)
        assertEquals(MeasureType.DISTANCE_TIME, exercise.measureType)
        assertEquals(listOf(BodyPart.QUADRICEPS, BodyPart.CALVES), exercise.bodyParts)
        assertEquals("cardio-1", exercise.id)
        assertEquals("air_bike_local", exercise.imagePath)
    }

    @Test
    fun `preferred spanish translation is selected when available`() {
        val payload = loadFixture("fixtures/remote/wger-spanish-translation-page.json")
            .toRemoteExercisePage(
                preferredLanguageCode = "es",
                languageIdByCode = languageIdByCode
            )
            .exercises
            .single()

        assertEquals("Elevacion lateral con mancuerna", payload.name)
        assertEquals("Eleva las mancuernas hacia los lados.", payload.description)
    }

    @Test
    fun `prefers description source and strips html fallback`() {
        val payload = WgerExerciseDto(
            id = 77,
            translations = listOf(
                WgerExerciseTranslationDto(
                    id = 701,
                    name = "Hip Thrust",
                    description = "<p><strong>Start</strong></p><ol><li>Drive hips up</li><li>Pause</li></ol>",
                    description_source = "Start\n\n1. Drive hips up\n2. Pause",
                    language = 2
                )
            )
        ).toRemoteExercisePayload(
            preferredLanguageCode = "en",
            languageIdByCode = languageIdByCode
        )

        assertEquals("Start\n\n1. Drive hips up\n2. Pause", payload.description)

        val fallbackPayload = WgerExerciseDto(
            id = 78,
            translations = listOf(
                WgerExerciseTranslationDto(
                    id = 702,
                    name = "Dips",
                    description = "<p>Start</p><ol><li>Lower down</li><li>Press up</li></ol>",
                    language = 2
                )
            )
        ).toRemoteExercisePayload(
            preferredLanguageCode = "en",
            languageIdByCode = languageIdByCode
        )

        assertEquals("Start\n\n- Lower down\n- Press up", fallbackPayload.description)
    }

    private fun loadFixture(path: String): WgerPaginatedResponseDto<WgerExerciseDto> {
        val json = checkNotNull(javaClass.classLoader?.getResource(path)) {
            "Fixture not found: $path"
        }.readText()

        val type = object : TypeToken<WgerPaginatedResponseDto<WgerExerciseDto>>() {}.type
        return Gson().fromJson(json, type)
    }
}
