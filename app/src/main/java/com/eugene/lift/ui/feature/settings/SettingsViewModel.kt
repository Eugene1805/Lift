package com.eugene.lift.ui.feature.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
import com.eugene.lift.domain.usecase.settings.GetCurrentLanguageUseCase
import com.eugene.lift.domain.usecase.settings.UpdateDistanceUnitUseCase
import com.eugene.lift.domain.usecase.settings.UpdateLanguageUseCase
import com.eugene.lift.domain.usecase.settings.UpdateThemeUseCase
import com.eugene.lift.domain.usecase.settings.UpdateWeightUnitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateWeightUnitUseCase: UpdateWeightUnitUseCase,
    private val updateDistanceUnitUseCase: UpdateDistanceUnitUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val getCurrentLanguageUseCase: GetCurrentLanguageUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    val settings: StateFlow<UserSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    fun updateTheme(theme: AppTheme) {
        Log.d(TAG, "User requested theme update to: $theme")
        viewModelScope.launch {
            try {
                updateThemeUseCase(theme)
                Log.i(TAG, "Theme update completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update theme in ViewModel", e)
            }
        }
    }

    fun updateWeightUnit(unit: WeightUnit) {
        Log.d(TAG, "User requested weight unit update to: $unit")
        viewModelScope.launch {
            try {
                updateWeightUnitUseCase(unit)
                Log.i(TAG, "Weight unit update completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update weight unit in ViewModel", e)
            }
        }
    }

    fun updateDistanceUnit(unit: DistanceUnit) {
        Log.d(TAG, "User requested distance unit update to: $unit")
        viewModelScope.launch {
            try {
                updateDistanceUnitUseCase(unit)
                Log.i(TAG, "Distance unit update completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update distance unit in ViewModel", e)
            }
        }
    }

    fun updateLanguage(languageCode: String) {
        Log.d(TAG, "User requested language update to: $languageCode")
        viewModelScope.launch {
            try {
                updateLanguageUseCase(languageCode)
                Log.i(TAG, "Language update completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update language in ViewModel", e)
            }
        }
    }

    fun getCurrentLanguageCode(): String {
        Log.d(TAG, "Getting current language code")
        return getCurrentLanguageUseCase()
    }
}