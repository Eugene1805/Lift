package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class ResolveExerciseHistoryUseCaseTest {

    private lateinit var repository: WorkoutRepository
    private lateinit var useCase: ResolveExerciseHistoryUseCase

    private val exercise = Exercise(
        id = "exercise-1",
        name = "Bench Press",
        bodyParts = listOf(BodyPart.CHEST),
        category = ExerciseCategory.BARBELL,
        measureType = MeasureType.REPS_AND_WEIGHT,
        instructions = "",
        imagePath = null
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = ResolveExerciseHistoryUseCase(repository)
    }

    @Test
    fun `invoke uses global history for display and same template history for prefill`() = runTest {
        val globalSession = historySession(
            templateId = "other-template",
            weight = 110.0,
            reps = 6
        )
        val sameTemplateSession = historySession(
            templateId = "template-1",
            weight = 100.0,
            reps = 8
        )

        coEvery { repository.getLastHistoryForExercise("exercise-1", null) } returns globalSession
        coEvery { repository.getLastHistoryForExercise("exercise-1", "template-1") } returns sameTemplateSession

        val snapshot = useCase("exercise-1", "template-1")

        assertEquals(110.0, snapshot.displaySets.single().weight, 0.0)
        assertEquals(6, snapshot.displaySets.single().reps)
        assertEquals(100.0, snapshot.prefillSets.single().weight, 0.0)
        assertEquals(8, snapshot.prefillSets.single().reps)
        coVerify(exactly = 1) { repository.getLastHistoryForExercise("exercise-1", null) }
        coVerify(exactly = 1) { repository.getLastHistoryForExercise("exercise-1", "template-1") }
    }

    @Test
    fun `invoke falls back to global history for both display and prefill on empty workouts`() = runTest {
        val globalSession = historySession(
            templateId = null,
            weight = 90.0,
            reps = 10
        )

        coEvery { repository.getLastHistoryForExercise("exercise-1", null) } returns globalSession

        val snapshot = useCase("exercise-1", null)

        assertEquals(90.0, snapshot.displaySets.single().weight, 0.0)
        assertEquals(90.0, snapshot.prefillSets.single().weight, 0.0)
        coVerify(exactly = 1) { repository.getLastHistoryForExercise("exercise-1", null) }
    }

    private fun historySession(
        templateId: String?,
        weight: Double,
        reps: Int
    ): WorkoutSession {
        return WorkoutSession(
            id = "session-$templateId-$weight",
            templateId = templateId,
            name = "History",
            date = LocalDateTime.now().minusDays(1),
            durationSeconds = 1800,
            exercises = listOf(
                SessionExercise(
                    id = "session-ex-$templateId-$weight",
                    exercise = exercise,
                    sets = listOf(
                        WorkoutSet(
                            id = "set-$templateId-$weight",
                            weight = weight,
                            reps = reps,
                            completed = true
                        )
                    )
                )
            )
        )
    }
}
