package com.eugene.lift.domain.usecase.settings

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale
import javax.inject.Inject

class GetCurrentLanguageUseCase @Inject constructor() {
    companion object {
        private const val TAG = "GetCurrentLanguageUseCase"
    }

    operator fun invoke(): String {
        Log.d(TAG, "Getting current language code")
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val languageCode = if (!currentLocales.isEmpty) {
            currentLocales.get(0)?.language ?: "en"
        } else {
            Locale.getDefault().language
        }
        Log.d(TAG, "Current language code: $languageCode")
        return languageCode
    }
}
