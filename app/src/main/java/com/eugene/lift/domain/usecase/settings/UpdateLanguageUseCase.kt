package com.eugene.lift.domain.usecase.settings

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.eugene.lift.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateLanguageUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    companion object {
        private const val TAG = "UpdateLanguageUseCase"
    }

    suspend operator fun invoke(languageCode: String) {
        Log.d(TAG, "Updating language to: $languageCode")
        try {
            // Update app locale
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)

            // Persist language preference
            repository.setLanguageCode(languageCode)
            Log.i(TAG, "Language updated successfully to: $languageCode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update language", e)
            throw e
        }
    }
}
