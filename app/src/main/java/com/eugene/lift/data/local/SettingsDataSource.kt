package com.eugene.lift.data.local

import android.content.Context
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


    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
    }

    // Leemos y convertimos los Strings guardados a Enums
    val userSettings: Flow<UserSettings> = dataStore.data.map { preferences ->
        val themeName = preferences[Keys.THEME] ?: AppTheme.SYSTEM.name
        val weightName = preferences[Keys.WEIGHT_UNIT] ?: WeightUnit.KG.name
        val distanceName = preferences[Keys.DISTANCE_UNIT] ?: DistanceUnit.KM.name

        UserSettings(
            theme = runCatching { AppTheme.valueOf(themeName) }.getOrDefault(AppTheme.SYSTEM),
            weightUnit = runCatching { WeightUnit.valueOf(weightName) }.getOrDefault(WeightUnit.KG),
            distanceUnit = runCatching { DistanceUnit.valueOf(distanceName) }.getOrDefault(DistanceUnit.KM)
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { prefs -> prefs[Keys.THEME] = theme.name }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        dataStore.edit { prefs -> prefs[Keys.WEIGHT_UNIT] = unit.name }
    }

    suspend fun setDistanceUnit(unit: DistanceUnit) {
        dataStore.edit { prefs -> prefs[Keys.DISTANCE_UNIT] = unit.name }
    }
}