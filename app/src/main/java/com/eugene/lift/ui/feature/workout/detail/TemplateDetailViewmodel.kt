package com.eugene.lift.ui.feature.workout.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eugene.lift.domain.usecase.template.GetTemplateDetailUseCase
import com.eugene.lift.ui.navigation.TemplateDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TemplateDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getTemplateDetailUseCase: GetTemplateDetailUseCase
) : ViewModel() {

    private val routeArgs = savedStateHandle.toRoute<TemplateDetailRoute>()

    val uiState: StateFlow<TemplateDetailUiState> = getTemplateDetailUseCase(routeArgs.templateId)
        .map { template -> TemplateDetailUiState(template = template, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TemplateDetailUiState()
        )

    fun onEvent(event: TemplateDetailUiEvent) {
        when (event) {
            TemplateDetailUiEvent.BackClicked,
            TemplateDetailUiEvent.EditTemplateClicked,
            is TemplateDetailUiEvent.ExerciseClicked,
            TemplateDetailUiEvent.StartWorkoutClicked -> Unit
        }
    }
}