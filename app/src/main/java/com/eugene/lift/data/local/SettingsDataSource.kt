package com.eugene.lift.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class SettingsDataSource @Inject constructor(
    @get:ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private const val TAG = "SettingsDataSource"
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    // Leemos y convertimos los Strings guardados a Enums
    val userSettings: Flow<UserSettings> = dataStore.data.map { preferences ->
        val themeName = preferences[Keys.THEME] ?: AppTheme.SYSTEM.name
        val weightName = preferences[Keys.WEIGHT_UNIT] ?: WeightUnit.KG.name
        val distanceName = preferences[Keys.DISTANCE_UNIT] ?: DistanceUnit.KM.name
        val langCode = preferences[Keys.LANGUAGE_CODE] ?: "en"

        Log.d(TAG, "Reading settings from DataStore - theme: $themeName, weight: $weightName, distance: $distanceName, language: $langCode")

        UserSettings(
            theme = runCatching { AppTheme.valueOf(themeName) }.getOrDefault(AppTheme.SYSTEM),
            weightUnit = runCatching { WeightUnit.valueOf(weightName) }.getOrDefault(WeightUnit.KG),
            distanceUnit = runCatching { DistanceUnit.valueOf(distanceName) }.getOrDefault(DistanceUnit.KM),
            languageCode = langCode
        )
    }

    suspend fun setLanguageCode(code: String) {
        Log.d(TAG, "Writing language code to DataStore: $code")
        try {
            dataStore.edit { prefs -> prefs[Keys.LANGUAGE_CODE] = code }
            Log.i(TAG, "Language code saved successfully to DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save language code to DataStore", e)
            throw e
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        Log.d(TAG, "Writing theme to DataStore: ${theme.name}")
        try {
            dataStore.edit { prefs -> prefs[Keys.THEME] = theme.name }
            Log.i(TAG, "Theme saved successfully to DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save theme to DataStore", e)
            throw e
        }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        Log.d(TAG, "Writing weight unit to DataStore: ${unit.name}")
        try {
            dataStore.edit { prefs -> prefs[Keys.WEIGHT_UNIT] = unit.name }
            Log.i(TAG, "Weight unit saved successfully to DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save weight unit to DataStore", e)
            throw e
        }
    }

    suspend fun setDistanceUnit(unit: DistanceUnit) {
        Log.d(TAG, "Writing distance unit to DataStore: ${unit.name}")
        try {
            dataStore.edit { prefs -> prefs[Keys.DISTANCE_UNIT] = unit.name }
            Log.i(TAG, "Distance unit saved successfully to DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save distance unit to DataStore", e)
            throw e
        }
    }
}