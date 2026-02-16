package com.eugene.lift.ui.feature.workout.edit

import com.eugene.lift.domain.model.TemplateExercise

data class EditTemplateUiState(
    val name: String = "",
    val exercises: List<TemplateExercise> = emptyList(),
    val isNameError: Boolean = true,
    val isSaving: Boolean = false,
    val isSaveCompleted: Boolean = false
)

sealed interface EditTemplateUiEvent {
    data class NameChanged(val value: String) : EditTemplateUiEvent
    data class ExerciseRemoved(val exerciseId: String) : EditTemplateUiEvent
    data class ExerciseConfigChanged(val exerciseId: String, val sets: String, val reps: String) : EditTemplateUiEvent
    data class ExercisesSelected(val exerciseIds: List<String>) : EditTemplateUiEvent
    data object SaveClicked : EditTemplateUiEvent
    data object AddExerciseClicked : EditTemplateUiEvent
    data object NavigationHandled : EditTemplateUiEvent
}

