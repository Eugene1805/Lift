package com.eugene.lift.data.remote.mapper

import com.eugene.lift.data.remote.dto.WgerExerciseDto
import com.eugene.lift.data.remote.dto.WgerExerciseVariationDto
import com.eugene.lift.data.remote.dto.WgerImageDto
import com.eugene.lift.data.remote.dto.WgerNamedResourceDto
import com.eugene.lift.data.remote.dto.WgerPaginatedResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WgerExerciseMapperTest {

    @Test
    fun `maps paginated response to remote page`() {
        val response = WgerPaginatedResponseDto(
            count = 1,
            next = "next-page",
            previous = null,
            results = listOf(
                WgerExerciseDto(
                    id = 42,
                    name = "Bench Press",
                    description = "Press the bar.",
                    category = WgerNamedResourceDto(id = 7, name = "Barbell"),
                    muscles = listOf(WgerNamedResourceDto(id = 1, name = "Chest")),
                    muscles_secondary = listOf(WgerNamedResourceDto(id = 2, name = "Triceps")),
                    equipment = listOf(WgerNamedResourceDto(id = 3, name = "Bench")),
                    images = listOf(WgerImageDto(id = 9, image = "main-image", is_main = true)),
                    variations = listOf(
                        WgerExerciseVariationDto(
                            id = 77,
                            images = listOf(WgerImageDto(id = 10, image = "variation-image"))
                        )
                    )
                )
            )
        )

        val page = response.toRemoteExercisePage()

        assertEquals(1, page.totalCount)
        assertEquals("next-page", page.nextPageUrl)
        assertEquals(1, page.exercises.size)
        assertEquals(42, page.exercises.single().remoteId)
        assertEquals("Bench Press", page.exercises.single().name)
        assertEquals("Barbell", page.exercises.single().categoryName)
        assertEquals("main-image", page.exercises.single().primaryImageUrl)
        assertEquals(listOf("Bench"), page.exercises.single().equipmentNames)
        assertEquals(listOf("Chest"), page.exercises.single().primaryMuscleNames)
        assertEquals(listOf("Triceps"), page.exercises.single().secondaryMuscleNames)
    }

    @Test
    fun `falls back to variation image when direct image missing`() {
        val dto = WgerExerciseDto(
            id = 99,
            name = "Pull Up",
            variations = listOf(
                WgerExerciseVariationDto(
                    id = 12,
                    images = listOf(WgerImageDto(id = 1, image = "variation-image"))
                )
            )
        )

        val payload = dto.toRemoteExercisePayload()

        assertEquals("variation-image", payload.primaryImageUrl)
    }

    @Test
    fun `keeps image null when api returns none`() {
        val dto = WgerExerciseDto(
            id = 100,
            name = "Air Squat"
        )

        val payload = dto.toRemoteExercisePayload()

        assertNull(payload.primaryImageUrl)
    }
}
