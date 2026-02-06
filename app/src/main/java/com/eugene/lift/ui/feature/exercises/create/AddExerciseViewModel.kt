package com.eugene.lift.ui.feature.exercises.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.usecase.exercise.GetExerciseDetailUseCase
import com.eugene.lift.domain.usecase.exercise.SaveExerciseUseCase
import com.eugene.lift.ui.navigation.ExerciseAddRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val routeArgs : ExerciseAddRoute? = try {
        savedStateHandle.toRoute<ExerciseAddRoute>()
    } catch (_: Exception) {
        null // Handle case where route parsing fails
    }

    private val exerciseId = routeArgs?.exerciseId

    val isEditing = exerciseId != null

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _selectedBodyParts = MutableStateFlow(setOf(BodyPart.OTHER))
    val selectedBodyParts = _selectedBodyParts.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ExerciseCategory.MACHINE)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedMeasureType = MutableStateFlow(MeasureType.REPS_AND_WEIGHT)
    val selectedMeasureType = _selectedMeasureType.asStateFlow()

    init {
        if (!isEditing) {
            _selectedBodyParts.value = setOf(BodyPart.OTHER)
        }
        if (exerciseId != null) {
            viewModelScope.launch {
                getExerciseDetailUseCase(exerciseId).collect { exercise ->
                    exercise?.let {
                        _name.value = it.name
                        _selectedCategory.value = it.category
                        _selectedMeasureType.value = it.measureType
                        _selectedBodyParts.value = it.bodyParts.toSet()
                        // Si tuvieras instrucciones o imagen, también aquí
                    }
                }
            }
        }
    }
    fun onNameChange(newValue: String) {
        if (newValue.length <= MAX_EXERCISE_NAME_LENGTH) {
            _name.value = newValue
        }
    }

    fun toggleBodyPart(part: BodyPart) {
        val current = _selectedBodyParts.value.toMutableSet()
        if (part in current) {
            if (current.size > 1) current.remove(part)
        } else {
            current.add(part)
        }
        _selectedBodyParts.value = current
    }

    fun onCategoryChange(newValue: ExerciseCategory) { _selectedCategory.value = newValue }
    fun onMeasureTypeChange(newValue: MeasureType) { _selectedMeasureType.value = newValue }

    fun saveExercise(onSuccess: () -> Unit) {
        if (_name.value.isBlank()) return

        viewModelScope.launch {
            try {
                val idToSave = exerciseId ?: UUID.randomUUID().toString()
                saveExerciseUseCase(
                    Exercise(
                        id = idToSave,
                        name = _name.value,
                        bodyParts = _selectedBodyParts.value.toList(),
                        category = _selectedCategory.value,
                        measureType = _selectedMeasureType.value,
                        instructions = "",
                        imagePath = null
                    )
                )
                onSuccess()
            } catch (_: Exception) {
                // TODO : Handle validation error (e.g., show Snackbar)
            }
        }
    }
}