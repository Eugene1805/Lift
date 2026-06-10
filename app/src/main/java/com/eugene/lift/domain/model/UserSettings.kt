package com.eugene.lift.domain.model

import com.eugene.lift.common.localization.systemLanguageCode

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

enum class WeightUnit {
    KG, LBS
}

enum class DistanceUnit {
    KM, MILES
}

data class UserSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val languageCode: String = systemLanguageCode(),
    val effortMetric: String? = null,   // "RPE", "RIR", or null (hidden)
    val autoTimerEnabled: Boolean = true
)
