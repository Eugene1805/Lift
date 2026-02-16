package com.eugene.lift.ui.feature.exercises.detail

import com.eugene.lift.domain.model.Exercise

data class ExerciseDetailUiState(
    val exercise: Exercise? = null,
    val isLoading: Boolean = true
)

sealed interface ExerciseDetailUiEvent {
    data object BackClicked : ExerciseDetailUiEvent
    data object EditClicked : ExerciseDetailUiEvent
}

