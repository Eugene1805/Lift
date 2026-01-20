package com.eugene.lift.data.repository

import android.util.Log
import com.eugene.lift.data.local.SettingsDataSource
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataSource: SettingsDataSource
) : SettingsRepository {

    companion object {
        private const val TAG = "SettingsRepository"
    }

    override fun getSettings(): Flow<UserSettings> {
        Log.d(TAG, "Getting settings flow from data source")
        return dataSource.userSettings
    }

    override suspend fun setTheme(theme: AppTheme) {
        Log.d(TAG, "Setting theme in data source: $theme")
        try {
            dataSource.setTheme(theme)
            Log.i(TAG, "Theme set successfully in data source")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set theme in data source", e)
            throw e
        }
    }

    override suspend fun setWeightUnit(unit: WeightUnit) {
        Log.d(TAG, "Setting weight unit in data source: $unit")
        try {
            dataSource.setWeightUnit(unit)
            Log.i(TAG, "Weight unit set successfully in data source")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set weight unit in data source", e)
            throw e
        }
    }

    override suspend fun setDistanceUnit(unit: DistanceUnit) {
        Log.d(TAG, "Setting distance unit in data source: $unit")
        try {
            dataSource.setDistanceUnit(unit)
            Log.i(TAG, "Distance unit set successfully in data source")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set distance unit in data source", e)
            throw e
        }
    }

    override suspend fun setLanguageCode(code: String) {
        Log.d(TAG, "Setting language code in data source: $code")
        try {
            dataSource.setLanguageCode(code)
            Log.i(TAG, "Language code set successfully in data source")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set language code in data source", e)
            throw e
        }
    }
}