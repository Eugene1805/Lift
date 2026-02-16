package com.eugene.lift.ui.feature.settings

import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.WeightUnit

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val languageCode: String = "en"
)

sealed interface SettingsUiEvent {
    data class ThemeChanged(val theme: AppTheme) : SettingsUiEvent
    data class WeightUnitChanged(val unit: WeightUnit) : SettingsUiEvent
    data class DistanceUnitChanged(val unit: DistanceUnit) : SettingsUiEvent
    data class LanguageChanged(val code: String) : SettingsUiEvent
    data object ContactUsClicked : SettingsUiEvent
}
