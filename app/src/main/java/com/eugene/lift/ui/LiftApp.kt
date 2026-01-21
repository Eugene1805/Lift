package com.eugene.lift.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.eugene.lift.common.localization.createLocalizedContext
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.usecase.GetSettingsUseCase
import com.eugene.lift.ui.theme.LiftTheme

/**
 * Root composable for the Lift app that sets up theming and localization.
 *
 * This composable:
 * - Observes user settings (theme, language)
 * - Applies localization by creating a localized context
 * - Applies the selected theme
 * - Renders the main app shell
 *
 * @param getSettingsUseCase Use case for retrieving user settings
 */
@Composable
fun LiftApp(getSettingsUseCase: GetSettingsUseCase) {
    // Observe user settings
    val settingsState by getSettingsUseCase()
        .collectAsState(
            initial = UserSettings(
                theme = AppTheme.SYSTEM,
                weightUnit = WeightUnit.KG,
                distanceUnit = DistanceUnit.KM,
                languageCode = "en"
            )
        )

    // Get current context and create localized version
    val context = LocalContext.current
    val localizedContext = remember(settingsState.languageCode) {
        context.createLocalizedContext(settingsState.languageCode)
    }

    // Provide localized context to all composables
    CompositionLocalProvider(LocalContext provides localizedContext) {
        // Determine theme based on settings
        val useDarkTheme = when (settingsState.theme) {
            AppTheme.LIGHT -> false
            AppTheme.DARK -> true
            AppTheme.SYSTEM -> isSystemInDarkTheme()
        }

        // Apply theme and render app
        LiftTheme(darkTheme = useDarkTheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainAppShell()
            }
        }
    }
}
