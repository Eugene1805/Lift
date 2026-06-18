package com.eugene.lift.domain.model

import com.eugene.lift.common.localization.systemLanguageCode

enum class AppTheme {
    ORCA,
    MAKO,
    FOX,
    VIPER,
    LION;

    companion object {
        fun fromStorageValue(value: String?): AppTheme = when (value) {
            null, "", "LIGHT", "DARK", "SYSTEM" -> ORCA
            else -> entries.firstOrNull { it.name == value } ?: ORCA
        }
    }
}

enum class WeightUnit {
    KG, LBS
}

enum class DistanceUnit {
    KM, MILES
}

data class UserSettings(
    val theme: AppTheme = AppTheme.ORCA,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val languageCode: String = systemLanguageCode(),
    val effortMetric: String? = null,   // "RPE", "RIR", or null (hidden)
    val autoTimerEnabled: Boolean = true
)
