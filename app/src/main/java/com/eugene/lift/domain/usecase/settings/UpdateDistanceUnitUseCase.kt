package com.eugene.lift.domain.usecase.settings

import android.util.Log
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateDistanceUnitUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    companion object {
        private const val TAG = "UpdateDistanceUnitUseCase"
    }

    suspend operator fun invoke(unit: DistanceUnit) {
        Log.d(TAG, "Updating distance unit to: $unit")
        try {
            repository.setDistanceUnit(unit)
            Log.i(TAG, "Distance unit updated successfully to: $unit")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update distance unit", e)
            throw e
        }
    }
}
