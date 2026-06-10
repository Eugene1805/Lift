package com.eugene.lift.common.localization

import java.util.Locale

private val supportedLanguageCodes = setOf("en", "es")

fun resolveSupportedLanguageCode(languageCode: String?): String {
    val normalized = languageCode
        ?.trim()
        ?.lowercase(Locale.ROOT)
        ?.takeIf { it.isNotBlank() }
        ?: Locale.getDefault().language.lowercase(Locale.ROOT)

    return normalized.takeIf { it in supportedLanguageCodes } ?: "en"
}

fun systemLanguageCode(): String = resolveSupportedLanguageCode(Locale.getDefault().language)
