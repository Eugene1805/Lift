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
    fun getTrackedExerciseIds(): Flow<List<String>>
    suspend fun setTrackedExerciseIds(ids: List<String>)
    fun isOnboardingComplete(): Flow<Boolean>
    suspend fun setOnboardingComplete(done: Boolean)
    fun isSwipeHintSeen(): Flow<Boolean>
    suspend fun setSwipeHintSeen()
}


