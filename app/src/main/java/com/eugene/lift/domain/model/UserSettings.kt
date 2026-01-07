package com.eugene.lift.domain.model

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
    val distanceUnit: DistanceUnit = DistanceUnit.KM
)