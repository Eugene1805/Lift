package com.eugene.lift.ui.feature.workout.edit

import com.eugene.lift.domain.model.TemplateExercise

data class ReorderUiState(
    val isReorderMode: Boolean = false,
    val draggingExerciseId: String? = null
)

data class EditTemplateUiState(
    val name: String = "",
    val exercises: List<TemplateExercise> = emptyList(),
    val isNameError: Boolean = true,
    val isSaving: Boolean = false,
    val isSaveCompleted: Boolean = false,
    val reorderState: ReorderUiState = ReorderUiState()
)

sealed interface EditTemplateUiEvent {
    data class NameChanged(val value: String) : EditTemplateUiEvent
    data class ExerciseRemoved(val exerciseId: String) : EditTemplateUiEvent
    data class ExerciseConfigChanged(val exerciseId: String, val sets: String, val reps: String) : EditTemplateUiEvent
    data class ExercisesSelected(val exerciseIds: List<String>) : EditTemplateUiEvent
    data class ExercisesReordered(val fromIndex: Int, val toIndex: Int) : EditTemplateUiEvent
    data object ToggleReorderMode : EditTemplateUiEvent
    data object SaveClicked : EditTemplateUiEvent
    data object AddExerciseClicked : EditTemplateUiEvent
    data object NavigationHandled : EditTemplateUiEvent
}

