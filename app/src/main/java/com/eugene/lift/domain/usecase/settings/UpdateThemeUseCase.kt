package com.eugene.lift.domain.usecase.settings

import android.util.Log
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateThemeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    companion object {
        private const val TAG = "UpdateThemeUseCase"
    }

    suspend operator fun invoke(theme: AppTheme) {
        Log.d(TAG, "Updating theme to: $theme")
        try {
            repository.setTheme(theme)
            Log.i(TAG, "Theme updated successfully to: $theme")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update theme", e)
            throw e
        }
    }
}
