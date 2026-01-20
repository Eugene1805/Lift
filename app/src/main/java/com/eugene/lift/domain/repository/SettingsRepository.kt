package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<UserSettings>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setWeightUnit(unit: WeightUnit)
    suspend fun setDistanceUnit(unit: DistanceUnit)
    suspend fun setLanguageCode(code: String)
}


