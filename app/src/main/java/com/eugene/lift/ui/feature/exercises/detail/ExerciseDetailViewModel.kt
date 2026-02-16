package com.eugene.lift.ui.feature.exercises.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.usecase.exercise.GetExerciseDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getExerciseDetailUseCase: GetExerciseDetailUseCase
) : ViewModel() {
    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    val uiState: StateFlow<ExerciseDetailUiState> = getExerciseDetailUseCase(exerciseId)
        .map { exercise -> ExerciseDetailUiState(exercise = exercise, isLoading = false) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ExerciseDetailUiState()
        )

    fun onEvent(event: ExerciseDetailUiEvent) {
        when (event) {
            ExerciseDetailUiEvent.BackClicked,
            ExerciseDetailUiEvent.EditClicked -> Unit
        }
    }
}