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

@HiltViewModel
class AddExerciseViewModel @Inject constructor(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _selectedBodyPart = MutableStateFlow(BodyPart.CHEST)
    val selectedBodyPart = _selectedBodyPart.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ExerciseCategory.BARBELL)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedMeasureType = MutableStateFlow(MeasureType.REPS_AND_WEIGHT)
    val selectedMeasureType = _selectedMeasureType.asStateFlow()
    fun onNameChange(newValue: String) { _name.value = newValue }

    fun onBodyPartChange(newValue: BodyPart) { _selectedBodyPart.value = newValue }

    fun onCategoryChange(newValue: ExerciseCategory) { _selectedCategory.value = newValue }
    fun onMeasureTypeChange(newValue: MeasureType) { _selectedMeasureType.value = newValue }

    fun saveExercise(onSuccess: () -> Unit) {
        if (_name.value.isBlank()) return

        viewModelScope.launch {
            repository.saveExercise(
                ExerciseEntity(
                    name = _name.value,
                    bodyPart = _selectedBodyPart.value,
                    category = _selectedCategory.value,
                    measureType = _selectedMeasureType.value
                )
            )
            onSuccess()
        }
    }
}