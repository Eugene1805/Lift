package com.eugene.lift.common.localization

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

/**
 * A context wrapper that provides localized resources while maintaining Hilt injection support.
 *
 * This wrapper allows overriding the locale for resource resolution without breaking
 * Hilt's dependency injection, which relies on the base context.
 *
 * @param base The base context (should be the original Hilt-injected context)
 * @param localizedConfigContext The context with the desired locale configuration
 */
class HiltSafeLocalizedContext(
    base: Context,
    private val localizedConfigContext: Context
) : ContextWrapper(base) {

    override fun getResources(): Resources = localizedConfigContext.resources

    override fun getAssets(): AssetManager = localizedConfigContext.assets
}

/**
 * Helper function to create a localized context for a given language code.
 *
 * @param languageCode The ISO 639-1 language code (e.g., "en", "es")
 * @return A HiltSafeLocalizedContext with the specified locale
 */
fun Context.createLocalizedContext(languageCode: String): HiltSafeLocalizedContext {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(resources.configuration)
    config.setLocale(locale)

    val localizedConfigContext = createConfigurationContext(config)

    return HiltSafeLocalizedContext(
        base = this,
        localizedConfigContext = localizedConfigContext
    )
}
