package com.eugene.lift.ui.feature.workout.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eugene.lift.domain.usecase.template.GetTemplateDetailUseCase
import com.eugene.lift.ui.navigation.TemplateDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TemplateDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getTemplateDetailUseCase: GetTemplateDetailUseCase
) : ViewModel() {

    private val routeArgs = savedStateHandle.toRoute<TemplateDetailRoute>()

    val template = getTemplateDetailUseCase(routeArgs.templateId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}