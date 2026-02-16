package com.eugene.lift.ui.feature.workout.detail

import com.eugene.lift.domain.model.WorkoutTemplate

data class TemplateDetailUiState(
    val template: WorkoutTemplate? = null,
    val isLoading: Boolean = true
)

sealed interface TemplateDetailUiEvent {
    data object BackClicked : TemplateDetailUiEvent
    data object StartWorkoutClicked : TemplateDetailUiEvent
    data object EditTemplateClicked : TemplateDetailUiEvent
    data class ExerciseClicked(val exerciseId: String) : TemplateDetailUiEvent
}

