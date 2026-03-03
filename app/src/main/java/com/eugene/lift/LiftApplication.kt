package com.eugene.lift

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LiftApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Only re-apply if the user has explicitly overridden the locale inside the app.
        // If the list is empty, AppCompat automatically uses the system locale — so we
        // should NOT override it, otherwise Spanish (or any other OS language) won't be
        // picked up on a fresh install.
        val savedLocales = AppCompatDelegate.getApplicationLocales()
        if (!savedLocales.isEmpty) {
            AppCompatDelegate.setApplicationLocales(savedLocales)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}