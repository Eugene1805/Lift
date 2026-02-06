package com.eugene.lift

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eugene.lift.common.work.WorkInitializer
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
import com.eugene.lift.ui.LiftApp
import dagger.hilt.android.AndroidEntryPoint
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
    }

    @Inject
    lateinit var getSettingsUseCase: GetSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing MainActivity")

        enableEdgeToEdge()

        // Initialize background work (database seeding)
        WorkInitializer.enqueueDatabaseSeeding(this)

        // Set up Compose UI
        setContent {
            LiftApp(getSettingsUseCase = getSettingsUseCase)
        }

        Log.i(TAG, "onCreate: MainActivity initialized successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: MainActivity is being destroyed")
    }
}