package com.eugene.lift.ui

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import app.cash.turbine.test
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.usecase.GetExerciseDetailUseCase
import com.eugene.lift.domain.usecase.SaveExerciseUseCase
import com.eugene.lift.ui.feature.exercises.AddExerciseViewModel
import com.eugene.lift.ui.feature.exercises.MAX_EXERCISE_NAME_LENGTH
import com.eugene.lift.ui.navigation.ExerciseAddRoute
import com.eugene.lift.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit test for AddExerciseViewModel
 * Tests ViewModel logic without Android dependencies
 * Uses MockK to mock use cases following Clean Architecture
 */
class AddExerciseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var saveExerciseUseCase: SaveExerciseUseCase
    private lateinit var getExerciseDetailUseCase: GetExerciseDetailUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddExerciseViewModel

    @Before
    fun setup() {
        saveExerciseUseCase = mockk(relaxed = true)
        getExerciseDetailUseCase = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)

        // Default: no exercise ID (creating new exercise) - route parsing fails
        every { savedStateHandle.toRoute<ExerciseAddRoute>() } throws Exception("No route")
    }

    @Test
    fun `initial state has empty name and default values`() = runTest {
        // WHEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)

        // THEN
        viewModel.name.test {
            assertEquals("", awaitItem())
        }

        viewModel.selectedBodyParts.test {
            assertEquals(setOf(BodyPart.OTHER), awaitItem())
        }

        viewModel.selectedCategory.test {
            assertEquals(ExerciseCategory.MACHINE, awaitItem())
        }

        viewModel.selectedMeasureType.test {
            assertEquals(MeasureType.REPS_AND_WEIGHT, awaitItem())
        }
    }

    @Test
    fun `onNameChange updates name state correctly`() = runTest {
        // GIVEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)

        // WHEN
        viewModel.onNameChange("Bench Press")

        // THEN
        viewModel.name.test {
            assertEquals("Bench Press", awaitItem())
        }
    }

    @Test
    fun `onNameChange does not exceed max length`() = runTest {
        // GIVEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)
        val longName = "A".repeat(MAX_EXERCISE_NAME_LENGTH + 10)

        // WHEN
        viewModel.onNameChange(longName)

        // THEN
        viewModel.name.test {
            val actualName = awaitItem()
            assertTrue(actualName.length <= MAX_EXERCISE_NAME_LENGTH)
        }
    }

    @Test
    fun `toggleBodyPart adds body part when not present`() = runTest {
        // GIVEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)

        // WHEN
        viewModel.toggleBodyPart(BodyPart.CHEST)

        // THEN
        viewModel.selectedBodyParts.test {
            val bodyParts = awaitItem()
            assertTrue(bodyParts.contains(BodyPart.CHEST))
            assertTrue(bodyParts.contains(BodyPart.OTHER)) // OTHER is still there from initial state
        }
    }

    @Test
    fun `toggleBodyPart removes body part when already present and not the last one`() = runTest {
        // GIVEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)
        viewModel.toggleBodyPart(BodyPart.CHEST) // Add CHEST (now we have OTHER and CHEST)

        // WHEN
        viewModel.toggleBodyPart(BodyPart.CHEST) // Remove CHEST

        // THEN
        viewModel.selectedBodyParts.test {
            val bodyParts = awaitItem()
            assertFalse(bodyParts.contains(BodyPart.CHEST)) // CHEST should be removed
            assertTrue(bodyParts.contains(BodyPart.OTHER)) // OTHER should still be there
        }
    }

    @Test
    fun `onCategoryChange updates category state`() = runTest {
        // GIVEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)

        // WHEN
        viewModel.onCategoryChange(ExerciseCategory.BARBELL)

        // THEN
        viewModel.selectedCategory.test {
            assertEquals(ExerciseCategory.BARBELL, awaitItem())
        }
    }

    @Test
    fun `onMeasureTypeChange updates measure type state`() = runTest {
        // GIVEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)

        // WHEN
        viewModel.onMeasureTypeChange(MeasureType.TIME)

        // THEN
        viewModel.selectedMeasureType.test {
            assertEquals(MeasureType.TIME, awaitItem())
        }
    }

    @Test
    fun `saveExercise calls use case with correct data when name is valid`() = runTest {
        // GIVEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)
        viewModel.onNameChange("Bulgarian Split Squat")
        viewModel.toggleBodyPart(BodyPart.QUADRICEPS)
        viewModel.onCategoryChange(ExerciseCategory.DUMBBELL)
        viewModel.onMeasureTypeChange(MeasureType.REPS_AND_WEIGHT)

        var callbackCalled = false

        // WHEN
        viewModel.saveExercise { callbackCalled = true }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle() // Wait for the coroutine to complete

        // THEN
        val slot = slot<Exercise>()
        coVerify(exactly = 1) { saveExerciseUseCase(capture(slot)) }

        val capturedExercise = slot.captured
        assertEquals("Bulgarian Split Squat", capturedExercise.name)
        assertTrue(capturedExercise.bodyParts.contains(BodyPart.QUADRICEPS))
        assertEquals(ExerciseCategory.DUMBBELL, capturedExercise.category)
        assertEquals(MeasureType.REPS_AND_WEIGHT, capturedExercise.measureType)
        assertTrue(callbackCalled)
    }

    @Test
    fun `saveExercise does NOT call use case when name is blank`() = runTest {
        // GIVEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)
        viewModel.onNameChange("   ") // Blank name

        var callbackCalled = false

        // WHEN
        viewModel.saveExercise { callbackCalled = true }

        // THEN
        coVerify(exactly = 0) { saveExerciseUseCase(any()) }
        assertFalse(callbackCalled)
    }

    @Test
    fun `saveExercise does NOT call use case when name is empty`() = runTest {
        // GIVEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)
        // name is empty by default

        var callbackCalled = false

        // WHEN
        viewModel.saveExercise { callbackCalled = true }

        // THEN
        coVerify(exactly = 0) { saveExerciseUseCase(any()) }
        assertFalse(callbackCalled)
    }

    // NOTE: Tests for isEditing and loadExercise with exerciseId are not included
    // because toRoute() is an inline extension function that cannot be mocked with MockK.
    // These scenarios should be tested with instrumentation tests or by refactoring
    // the ViewModel to inject the exerciseId directly for better testability.

    @Test
    fun `isEditing is false when exerciseId is not provided`() = runTest {
        // GIVEN - Already set in @Before
        every { savedStateHandle.toRoute<ExerciseAddRoute>() } throws Exception("No route")

        // WHEN
        viewModel = AddExerciseViewModel(saveExerciseUseCase, getExerciseDetailUseCase, savedStateHandle)

        // THEN
        assertFalse(viewModel.isEditing)
    }
}