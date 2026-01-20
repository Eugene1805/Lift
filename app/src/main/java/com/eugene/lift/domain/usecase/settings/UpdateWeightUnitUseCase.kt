package com.eugene.lift.domain.usecase.settings

import android.util.Log
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateWeightUnitUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    companion object {
        private const val TAG = "UpdateWeightUnitUseCase"
    }

    suspend operator fun invoke(unit: WeightUnit) {
        Log.d(TAG, "Updating weight unit to: $unit")
        try {
            repository.setWeightUnit(unit)
            Log.i(TAG, "Weight unit updated successfully to: $unit")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update weight unit", e)
            throw e
        }
    }
}
