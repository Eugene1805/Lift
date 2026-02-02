package com.eugene.lift.ui.feature.workout.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.usecase.GetExerciseDetailUseCase
import com.eugene.lift.domain.usecase.template.GetTemplateDetailUseCase
import com.eugene.lift.domain.usecase.template.SaveTemplateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

const val MAX_TEMPLATE_NAME_LENGTH = 50

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTemplateDetailUseCase: GetTemplateDetailUseCase,
    private val saveTemplateUseCase: SaveTemplateUseCase,
    private val getExerciseDetailUseCase: GetExerciseDetailUseCase
) : ViewModel() {

    private val templateId: String? = savedStateHandle["templateId"]

    // Estado del Formulario
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _exercises = MutableStateFlow<List<TemplateExercise>>(emptyList())
    val exercises = _exercises.asStateFlow()

    init {
        // Si hay ID, cargamos los datos existentes
        if (templateId != null) {
            viewModelScope.launch {
                getTemplateDetailUseCase(templateId).collect { template ->
                    template?.let {
                        _name.value = it.name
                        _exercises.value = it.exercises
                    }
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        if (newName.length <= MAX_TEMPLATE_NAME_LENGTH) {
            _name.value = newName
        }
    }

    // Llamado cuando el usuario selecciona un ejercicio en el Picker
    fun addExercise(exercise: Exercise) {
        val newTemplateExercise = TemplateExercise(
            id = UUID.randomUUID().toString(),
            exercise = exercise,
            orderIndex = _exercises.value.size, // Al final
            targetSets = 3, // Default
            targetReps = "8-12",
            restTimerSeconds = 60
        )
        _exercises.update { it + newTemplateExercise }
    }

    fun removeExercise(item: TemplateExercise) {
        _exercises.update { list -> list.filter { it.id != item.id } }
    }

    fun updateExerciseConfig(item: TemplateExercise, sets: String, reps: String) {
        _exercises.update { list ->
            list.map {
                if (it.id == item.id) it.copy(
                    targetSets = sets.toIntOrNull() ?: it.targetSets,
                    targetReps = reps
                ) else it
            }
        }
    }

    fun onExercisesSelected(exerciseIds: List<String>) {
        if (exerciseIds.isEmpty()) return

        viewModelScope.launch {
            exerciseIds.forEach { id ->
                val exercise = getExerciseDetailUseCase(id).firstOrNull()
                if (exercise != null) {
                    addExercise(exercise)
                }
            }
        }
    }

    fun saveTemplate(onSuccess: () -> Unit) {
        if (_name.value.isBlank()) return

        viewModelScope.launch {
            val template = WorkoutTemplate(
                id = templateId ?: UUID.randomUUID().toString(),
                name = _name.value,
                exercises = _exercises.value.mapIndexed { index, ex ->
                    ex.copy(orderIndex = index) // Re-indexamos por seguridad
                }
            )
            saveTemplateUseCase(template)
            onSuccess()
        }
    }
}