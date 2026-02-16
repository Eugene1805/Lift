package com.eugene.lift.ui.feature.exercises.create

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType

data class AddExerciseUiState(
    val name: String = "",
    val selectedBodyParts: Set<BodyPart> = setOf(BodyPart.OTHER),
    val category: ExerciseCategory = ExerciseCategory.MACHINE,
    val measureType: MeasureType = MeasureType.REPS_AND_WEIGHT,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isNameError: Boolean = true,
    val isSaveEnabled: Boolean = false,
    val isSaveCompleted: Boolean = false
)

sealed interface AddExerciseUiEvent {
    data class NameChanged(val value: String) : AddExerciseUiEvent
    data class BodyPartToggled(val bodyPart: BodyPart) : AddExerciseUiEvent
    data class CategoryChanged(val category: ExerciseCategory) : AddExerciseUiEvent
    data class MeasureTypeChanged(val measureType: MeasureType) : AddExerciseUiEvent
    data object SaveClicked : AddExerciseUiEvent
    data object BackClicked : AddExerciseUiEvent
    data object NavigationHandled : AddExerciseUiEvent
}
