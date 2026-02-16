package com.eugene.lift.ui.feature.exercises.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.usecase.exercise.GetExerciseDetailUseCase
import com.eugene.lift.domain.usecase.exercise.SaveExerciseUseCase
import com.eugene.lift.ui.navigation.ExerciseAddRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

const val MAX_EXERCISE_NAME_LENGTH = 50
@HiltViewModel
class AddExerciseViewModel @Inject constructor(
    private val saveExerciseUseCase: SaveExerciseUseCase,
    private val getExerciseDetailUseCase: GetExerciseDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val routeArgs: ExerciseAddRoute? = try {
        savedStateHandle.toRoute<ExerciseAddRoute>()
    } catch (_: Exception) {
        null
    }

    private val exerciseId = routeArgs?.exerciseId
    private val _uiState = MutableStateFlow(
        AddExerciseUiState(
            isEditing = exerciseId != null
        )
    )
    val uiState: StateFlow<AddExerciseUiState> = _uiState

    init {
        if (exerciseId != null) {
            viewModelScope.launch {
                getExerciseDetailUseCase(exerciseId).collect { exercise ->
                    exercise?.let { applyLoadedExercise(it) }
                }
            }
        }
    }

    fun onEvent(event: AddExerciseUiEvent) {
        when (event) {
            is AddExerciseUiEvent.NameChanged -> updateName(event.value)
            is AddExerciseUiEvent.BodyPartToggled -> toggleBodyPart(event.bodyPart)
            is AddExerciseUiEvent.CategoryChanged -> _uiState.update { it.copy(category = event.category) }
            is AddExerciseUiEvent.MeasureTypeChanged -> _uiState.update { it.copy(measureType = event.measureType) }
            AddExerciseUiEvent.SaveClicked -> saveExercise()
            AddExerciseUiEvent.NavigationHandled -> _uiState.update { it.copy(isSaveCompleted = false) }
            AddExerciseUiEvent.BackClicked -> Unit
        }
    }

    private fun updateName(newValue: String) {
        if (newValue.length > MAX_EXERCISE_NAME_LENGTH) return
        val isNameError = newValue.isBlank()
        _uiState.update {
            it.copy(
                name = newValue,
                isNameError = isNameError,
                isSaveEnabled = newValue.isNotBlank() && !isNameError
            )
        }
    }

    private fun toggleBodyPart(part: BodyPart) {
        _uiState.update { state ->
            val updated = state.selectedBodyParts.toMutableSet()
            if (part in updated) {
                if (updated.size > 1) updated.remove(part)
            } else {
                updated.add(part)
            }
            state.copy(selectedBodyParts = updated)
        }
    }

    private fun applyLoadedExercise(exercise: Exercise) {
        val name = exercise.name
        val isNameError = name.isBlank() || name.length > MAX_EXERCISE_NAME_LENGTH
        _uiState.update {
            it.copy(
                name = name,
                selectedBodyParts = exercise.bodyParts.toSet().ifEmpty { setOf(BodyPart.OTHER) },
                category = exercise.category,
                measureType = exercise.measureType,
                isNameError = isNameError,
                isSaveEnabled = name.isNotBlank() && !isNameError
            )
        }
    }

    private fun saveExercise() {
        val state = _uiState.value
        if (state.isSaving || !state.isSaveEnabled) return

        _uiState.update { it.copy(isSaving = true, isSaveCompleted = false) }
        viewModelScope.launch {
            try {
                val idToSave = exerciseId ?: UUID.randomUUID().toString()
                saveExerciseUseCase(
                    Exercise(
                        id = idToSave,
                        name = state.name,
                        bodyParts = state.selectedBodyParts.toList(),
                        category = state.category,
                        measureType = state.measureType,
                        instructions = "",
                        imagePath = null
                    )
                )
                _uiState.update { it.copy(isSaving = false, isSaveCompleted = true) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}