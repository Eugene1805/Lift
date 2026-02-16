package com.eugene.lift.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.usecase.settings.GetCurrentLanguageUseCase
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
import com.eugene.lift.domain.usecase.settings.UpdateDistanceUnitUseCase
import com.eugene.lift.domain.usecase.settings.UpdateLanguageUseCase
import com.eugene.lift.domain.usecase.settings.UpdateThemeUseCase
import com.eugene.lift.domain.usecase.settings.UpdateWeightUnitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateWeightUnitUseCase: UpdateWeightUnitUseCase,
    private val updateDistanceUnitUseCase: UpdateDistanceUnitUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    getCurrentLanguageUseCase: GetCurrentLanguageUseCase
) : ViewModel() {

    private val settings: StateFlow<UserSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    private val languageCode = MutableStateFlow(getCurrentLanguageUseCase())

    val uiState: StateFlow<SettingsUiState> = combine(settings, languageCode) { current, lang ->
        SettingsUiState(
            theme = current.theme,
            weightUnit = current.weightUnit,
            distanceUnit = current.distanceUnit,
            languageCode = lang
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(languageCode = languageCode.value)
    )

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ThemeChanged -> updateTheme(event.theme)
            is SettingsUiEvent.WeightUnitChanged -> updateWeightUnit(event.unit)
            is SettingsUiEvent.DistanceUnitChanged -> updateDistanceUnit(event.unit)
            is SettingsUiEvent.LanguageChanged -> updateLanguage(event.code)
            SettingsUiEvent.ContactUsClicked -> Unit
        }
    }

    private fun updateTheme(theme: AppTheme) {
        viewModelScope.launch { updateThemeUseCase(theme) }
    }

    private fun updateWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { updateWeightUnitUseCase(unit) }
    }

    private fun updateDistanceUnit(unit: DistanceUnit) {
        viewModelScope.launch { updateDistanceUnitUseCase(unit) }
    }

    private fun updateLanguage(languageCode: String) {
        languageCode.also { code ->
            viewModelScope.launch { updateLanguageUseCase(code) }
            this.languageCode.value = code
        }
    }
}