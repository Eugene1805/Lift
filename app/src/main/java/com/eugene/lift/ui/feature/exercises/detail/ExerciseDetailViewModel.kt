package com.eugene.lift.ui.feature.exercises.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.repository.ExerciseRepository
import com.eugene.lift.domain.usecase.GetExerciseDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getExerciseDetailUseCase: GetExerciseDetailUseCase
) : ViewModel() {
    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])
    val exercise = getExerciseDetailUseCase(exerciseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}