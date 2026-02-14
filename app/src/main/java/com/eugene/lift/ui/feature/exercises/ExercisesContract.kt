package com.eugene.lift.ui.feature.exercises

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.usecase.exercise.SortOrder

data class ExercisesUiState(
    val exercises: List<Exercise> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.RECENT,
    val selectedBodyParts: Set<BodyPart> = emptySet(),
    val selectedCategories: Set<ExerciseCategory> = emptySet(),
    val totalExerciseCount: Int = 0,
    val isFilterSheetVisible: Boolean = false,
    val isSortMenuVisible: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedExerciseIds: Set<String> = emptySet()
)

sealed interface ExercisesUiEvent {
    data class SearchQueryChanged(val query: String) : ExercisesUiEvent
    data class SortOrderChanged(val sortOrder: SortOrder) : ExercisesUiEvent
    data class BodyPartToggled(val bodyPart: BodyPart) : ExercisesUiEvent
    data class CategoryToggled(val category: ExerciseCategory) : ExercisesUiEvent
    data object ClearFilters : ExercisesUiEvent
    data class FilterSheetVisibilityChanged(val isVisible: Boolean) : ExercisesUiEvent
    data class SortMenuVisibilityChanged(val isVisible: Boolean) : ExercisesUiEvent
    data class ExerciseSelectionToggled(val exerciseId: String) : ExercisesUiEvent
    data class SelectionModeChanged(val enabled: Boolean) : ExercisesUiEvent
    data object ClearSelection : ExercisesUiEvent
    data object SelectionConfirmed : ExercisesUiEvent
    data class ExerciseClicked(val exerciseId: String) : ExercisesUiEvent
    data object AddClicked : ExercisesUiEvent
}

