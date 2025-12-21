package com.eugene.lift.ui.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MAX_EXERCISE_NAME_LENGTH = 50
@HiltViewModel
class AddExerciseViewModel @Inject constructor(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _selectedBodyParts = MutableStateFlow<Set<BodyPart>>(setOf(BodyPart.CHEST))
    val selectedBodyParts = _selectedBodyParts.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ExerciseCategory.BARBELL)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedMeasureType = MutableStateFlow(MeasureType.REPS_AND_WEIGHT)
    val selectedMeasureType = _selectedMeasureType.asStateFlow()
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
            repository.saveExercise(
                ExerciseEntity(
                    name = _name.value,
                    bodyParts = _selectedBodyParts.value.toList(),
                    category = _selectedCategory.value,
                    measureType = _selectedMeasureType.value
                )
            )
            onSuccess()
        }
    }
}