package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.TemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class CreateTemplateFromWorkoutUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var useCase: CreateTemplateFromWorkoutUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = CreateTemplateFromWorkoutUseCase(repository)
    }

    @Test
    fun `invoke converts session to template properly`() = runTest {
        val exercise = Exercise(
            id = "ex1",
            name = "Bench Press",
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null,
            bodyParts = emptyList()
        )
        val session = WorkoutSession(
            id = "ws1",
            name = "Morning Workout",
            date = LocalDateTime.now(),
            templateId = null,
            durationSeconds = 0,
            exercises = listOf(
                SessionExercise(
                    id = "se1",
                    exercise = exercise,
                    sets = listOf(
                        WorkoutSet(id = "s1", weight = 100.0, reps = 5, completed = true),
                        WorkoutSet(id = "s2", weight = 100.0, reps = 5, completed = true)
                    )
                )
            )
        )

        coEvery { repository.saveTemplate(any()) } returns Unit

        useCase(session)

        coVerify { 
            repository.saveTemplate(match { template -> 
                template.name == "Morning Workout" &&
                template.exercises.size == 1 &&
                template.exercises[0].exercise.id == "ex1" &&
                template.exercises[0].targetSets == 2 &&
                template.exercises[0].targetReps == "6-10" &&
                template.exercises[0].restTimerSeconds == 90
            })
        }
    }

    @Test
    fun `invoke with empty session creates empty template`() = runTest {
        val session = WorkoutSession(
            id = "ws1",
            name = "Empty Session",
            date = LocalDateTime.now(),
            templateId = null,
            durationSeconds = 0,
            exercises = emptyList()
        )

        coEvery { repository.saveTemplate(any()) } returns Unit

        useCase(session)

        coVerify { 
            repository.saveTemplate(match { template -> 
                template.name == "Empty Session" &&
                template.exercises.isEmpty()
            })
        }
    }
}
