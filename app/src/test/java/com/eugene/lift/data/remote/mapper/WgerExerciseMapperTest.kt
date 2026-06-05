package com.eugene.lift.data.remote.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.eugene.lift.data.remote.dto.WgerExerciseDto
import com.eugene.lift.data.remote.dto.WgerPaginatedResponseDto
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.ExerciseSource
import com.eugene.lift.domain.model.MeasureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WgerExerciseMapperTest {

    @Test
    fun `maps fixture into remote page and local exercise`() {
        val response = loadFixture("fixtures/remote/wger-exercise-page.json")

        val page = response.toRemoteExercisePage()
        val exercise = page.exercises.single().toExercise(
            existingLocalId = "existing-local-id",
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
            .toRemoteExercisePage()
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
            .toRemoteExercisePage()
            .exercises
            .single()
        val exercise = payload.toExercise(existingLocalId = "cardio-1")

        assertNull(payload.primaryImageUrl)
        assertEquals(emptyList<String>(), payload.equipmentNames)
        assertEquals(ExerciseCategory.CARDIO, exercise.category)
        assertEquals(MeasureType.DISTANCE_TIME, exercise.measureType)
        assertEquals(listOf(BodyPart.QUADRICEPS, BodyPart.CALVES), exercise.bodyParts)
        assertEquals("cardio-1", exercise.id)
    }

    private fun loadFixture(path: String): WgerPaginatedResponseDto<WgerExerciseDto> {
        val json = checkNotNull(javaClass.classLoader?.getResource(path)) {
            "Fixture not found: $path"
        }.readText()

        val type = object : TypeToken<WgerPaginatedResponseDto<WgerExerciseDto>>() {}.type
        return Gson().fromJson(json, type)
    }
}
