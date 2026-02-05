package com.eugene.lift.ui.feature.history.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.usecase.GetSettingsUseCase
import com.eugene.lift.domain.usecase.history.GetWorkoutSessionDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getWorkoutSessionDetailsUseCase: GetWorkoutSessionDetailsUseCase,
    getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    private val sessionId: String? = savedStateHandle["sessionId"]

    private val sessionFlow: Flow<WorkoutSession?> = sessionId?.let {
        getWorkoutSessionDetailsUseCase(it)
    } ?: flowOf(null)

    val session: StateFlow<WorkoutSession?> = sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userSettings: StateFlow<UserSettings> = getSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())
}
