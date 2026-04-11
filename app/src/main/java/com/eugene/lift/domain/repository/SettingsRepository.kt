package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing application settings and user preferences.
 */
interface SettingsRepository {
    fun getSettings(): Flow<UserSettings>
    suspend fun setTheme(theme: AppTheme)

    /**
     * Updates the preferred weight unit.
     *
     * @param unit The new weight unit (e.g., KG, LB).
     */
    suspend fun setWeightUnit(unit: WeightUnit)

    /**
     * Updates the preferred distance unit.
     *
     * @param unit The new distance unit (e.g., KM, MILES).
     */
    suspend fun setDistanceUnit(unit: DistanceUnit)

    /**
     * Updates the application language.
     *
     * @param code The ISO language code (e.g., "en", "es").
     */
    suspend fun setLanguageCode(code: String)
    fun getTrackedExerciseIds(): Flow<List<String>>
    suspend fun setTrackedExerciseIds(ids: List<String>)

    /**
     * Returns a flow indicating if the user has completed the onboarding process.
     */
    fun isOnboardingComplete(): Flow<Boolean>

    /**
     * Sets the onboarding completion status.
     *
     * @param done True if onboarding is complete, false otherwise.
     */
    suspend fun setOnboardingComplete(done: Boolean)

    /**
     * Returns a flow indicating if the user has seen the swipe hint.
     */
    fun isSwipeHintSeen(): Flow<Boolean>

    /**
     * Marks the swipe hint as seen.
     */
    suspend fun setSwipeHintSeen()

    /**
     * Updates the effort metric used for workouts.
     *
     * @param metric The metric name (e.g., "RPE", "RIR") or null to disable.
     */
    suspend fun setEffortMetric(metric: String?)
    suspend fun setAutoTimerEnabled(enabled: Boolean)
}


