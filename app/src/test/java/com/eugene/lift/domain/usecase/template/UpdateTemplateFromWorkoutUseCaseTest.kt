package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class UpdateTemplateFromWorkoutUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var getTemplateDetailUseCase: GetTemplateDetailUseCase
    private lateinit var useCase: UpdateTemplateFromWorkoutUseCase

    @Before
    fun setup() {
        repository = mockk()
        getTemplateDetailUseCase = mockk()
        useCase = UpdateTemplateFromWorkoutUseCase(repository, getTemplateDetailUseCase)
    }

    @Test
    fun `invoke skips if templateId is null`() = runTest {
        val session = WorkoutSession(
            id = "ws1",
            name = "Session",
            date = LocalDateTime.now(),
            templateId = null,
            durationSeconds = 0,
            exercises = emptyList()
        )

        useCase(session)

        coVerify(exactly = 0) { repository.saveTemplate(any()) }
    }

    @Test
    fun `invoke skips if template not found`() = runTest {
        val session = WorkoutSession(
            id = "ws1",
            name = "Session",
            date = LocalDateTime.now(),
            templateId = "t1",
            durationSeconds = 0,
            exercises = emptyList()
        )
        
        coEvery { getTemplateDetailUseCase("t1") } returns flowOf(null)

        useCase(session)

        coVerify(exactly = 0) { repository.saveTemplate(any()) }
    }

    @Test
    fun `invoke updates template maintaining existing targets where applicable`() = runTest {
        val exercise1 = Exercise("ex1", "Squat", ExerciseCategory.BARBELL, MeasureType.REPS_AND_WEIGHT, "", null, emptyList())
        val exercise2 = Exercise("ex2", "Leg Press", ExerciseCategory.MACHINE, MeasureType.REPS_AND_WEIGHT, "", null, emptyList())
        
        val template = WorkoutTemplate(
            id = "t1",
            name = "Leg Day",
            exercises = listOf(
                TemplateExercise(
                    id = "te1",
                    exercise = exercise1,
                    orderIndex = 0,
                    targetSets = 3,
                    targetReps = "8-12",
                    restTimerSeconds = 120,
                    note = "Go deep"
                )
            )
        )
        
        coEvery { getTemplateDetailUseCase("t1") } returns flowOf(template)
        coEvery { repository.saveTemplate(any()) } returns Unit

        val session = WorkoutSession(
            id = "ws1",
            name = "Leg Day Real",
            date = LocalDateTime.now(),
            templateId = "t1",
            durationSeconds = 0,
            exercises = listOf(
                // Exercise 1 exists in template, did 4 sets now
                SessionExercise(
                    id = "se1",
                    exercise = exercise1,
                    sets = listOf(
                        WorkoutSet(id = "s1", weight = 100.0, reps = 10, completed = true),
                        WorkoutSet(id = "s2", weight = 100.0, reps = 10, completed = true),
                        WorkoutSet(id = "s3", weight = 100.0, reps = 10, completed = true),
                        WorkoutSet(id = "s4", weight = 100.0, reps = 10, completed = true)
                    )
                ),
                // Exercise 2 is new
                SessionExercise(
                    id = "se2",
                    exercise = exercise2,
                    sets = listOf(
                        WorkoutSet(id = "s5", weight = 200.0, reps = 12, completed = true)
                    )
                )
            )
        )
        
        useCase(session)

        coVerify { 
            repository.saveTemplate(match { updated -> 
                updated.id == "t1" &&
                updated.exercises.size == 2 &&
                // Check mapped existing
                updated.exercises[0].exercise.id == "ex1" &&
                updated.exercises[0].targetSets == 4 && // Updated from 3 sets done
                updated.exercises[0].targetReps == "8-12" && // Kept original
                updated.exercises[0].restTimerSeconds == 120 && // Kept original
                updated.exercises[0].note == "Go deep" &&
                // Check mapped new
                updated.exercises[1].exercise.id == "ex2" &&
                updated.exercises[1].targetSets == 1 && // 1 set done
                updated.exercises[1].targetReps == "6-10" && // Default
                updated.exercises[1].restTimerSeconds == 90 // Default
            })
        }
    }
}
