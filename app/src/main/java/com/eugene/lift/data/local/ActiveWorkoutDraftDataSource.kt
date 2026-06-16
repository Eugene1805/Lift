package com.eugene.lift.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.activeWorkoutDraftStore: DataStore<Preferences> by preferencesDataStore(
    name = "active_workout_draft"
)

@Singleton
class ActiveWorkoutDraftDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DRAFT_JSON = stringPreferencesKey("draft_json")
    }

    val draftJson: Flow<String?> = context.activeWorkoutDraftStore.data.map { prefs ->
        prefs[Keys.DRAFT_JSON]
    }

    suspend fun writeDraftJson(value: String) {
        context.activeWorkoutDraftStore.edit { prefs ->
            prefs[Keys.DRAFT_JSON] = value
        }
    }

    suspend fun clearDraft() {
        context.activeWorkoutDraftStore.edit { prefs ->
            prefs.remove(Keys.DRAFT_JSON)
        }
    }
}
