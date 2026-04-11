package com.eugene.lift

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eugene.lift.common.localization.createLocalizedContext
import com.eugene.lift.common.work.WorkInitializer
import com.eugene.lift.data.local.dataStore
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
import com.eugene.lift.ui.LiftApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Main activity for the Lift app.
 *
 * Responsibilities:
 * - Initialize WorkManager for background tasks (database seeding)
 * - Set up the Compose UI with theming and localization
 * - Serve as the entry point for Hilt dependency injection
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private val LANGUAGE_KEY = stringPreferencesKey("language_code")
    }

    @Inject
    lateinit var getSettingsUseCase: GetSettingsUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    /**
     * Apply the user's chosen language at the Activity level so that ALL Android windows —
     * including DropdownMenus, AlertDialogs and other popups not owned by Compose —
     * also render in the correct locale rather than falling back to the OS locale.
     *
     * Hilt is NOT yet initialized here, so we read DataStore directly.
     * runBlocking is safe: it is a single small file read (< 1 ms).
     */
    override fun attachBaseContext(newBase: Context) {
        val languageCode = try {
            runBlocking {
                newBase.applicationContext.dataStore.data.first()[LANGUAGE_KEY] ?: "en"
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read language preference, defaulting to 'en'", e)
            "en"
        }
        super.attachBaseContext(newBase.createLocalizedContext(languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing MainActivity")

        enableEdgeToEdge()

        // Initialize background work (database seeding)
        WorkInitializer.enqueueDatabaseSeeding(this)

        // Set up Compose UI
        setContent {
            LiftApp(
                getSettingsUseCase = getSettingsUseCase,
                settingsRepository = settingsRepository
            )
        }

        Log.i(TAG, "onCreate: MainActivity initialized successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: MainActivity is being destroyed")
    }
}