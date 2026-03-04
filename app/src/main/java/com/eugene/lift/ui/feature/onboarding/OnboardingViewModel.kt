package com.eugene.lift.ui.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingEvent {
    data object NavigateToHome : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedWeightUnit = MutableStateFlow(WeightUnit.KG)
    val selectedWeightUnit: StateFlow<WeightUnit> = _selectedWeightUnit.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setWeightUnit(unit: WeightUnit) {
        _selectedWeightUnit.value = unit
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setWeightUnit(_selectedWeightUnit.value)
            settingsRepository.setOnboardingComplete(true)
            _events.send(OnboardingEvent.NavigateToHome)
        }
    }
}
