package com.eugene.lift.ui

import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.repository.ExerciseRepository
import com.eugene.lift.ui.feature.exercises.AddExerciseViewModel
import com.eugene.lift.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AddExerciseViewModelTest{

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mock del repositorio (no usamos el real, simulamos su comportamiento)
    private val repository = mockk<ExerciseRepository>(relaxed = true)

    private lateinit var viewModel: AddExerciseViewModel

    @Test
    fun `saveExercise calls repository with correct data when name is valid`() = runTest {
        // 1. GIVEN
        viewModel = AddExerciseViewModel(repository)

        // Simulamos la entrada del usuario
        viewModel.onNameChange("Sentadilla Bulgara")
        viewModel.onBodyPartChange(BodyPart.LEGS)
        viewModel.onCategoryChange(ExerciseCategory.DUMBBELL)

        // 2. WHEN
        viewModel.saveExercise { } // Callback vacío

        // 3. THEN
        // Capturamos el argumento que se le pasó al repositorio para inspeccionarlo
        val slot = slot<ExerciseEntity>()

        // Verificamos que se llamó a saveExercise exactamente 1 vez
        coVerify(exactly = 1) { repository.saveExercise(capture(slot)) }

        // Verificamos que lo que se envió es lo correcto
        val capturedExercise = slot.captured
        assertEquals("Sentadilla Bulgara", capturedExercise.name)
        assertEquals(BodyPart.LEGS, capturedExercise.bodyPart)
        assertEquals(ExerciseCategory.DUMBBELL, capturedExercise.category)
    }

    @Test
    fun `saveExercise does NOT call repository if name is empty`() = runTest {
        // 1. GIVEN
        viewModel = AddExerciseViewModel(repository)
        viewModel.onNameChange("   ") // Nombre invalido

        // 2. WHEN
        viewModel.saveExercise { }

        // 3. THEN
        // Verificamos que NUNCA se llamó al repositorio
        coVerify(exactly = 0) { repository.saveExercise(any()) }
    }
}