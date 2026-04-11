package com.eugene.lift.ui.feature.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.R
import com.eugene.lift.common.localization.createLocalizedContext
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
import com.eugene.lift.domain.usecase.history.GetWorkoutHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

// Modelo para la lista heterogénea
sealed interface HistoryUiItem {
    data class Header(val title: String) : HistoryUiItem
    data class SessionItem(val session: WorkoutSession) : HistoryUiItem
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
    getSettingsUseCase: GetSettingsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val userSettings = getSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    // React to the app's in-app language setting (DataStore) instead of relying on the injected
    // ApplicationContext configuration, which may remain in the startup locale.
    private val languageCode: StateFlow<String> = userSettings
        .map { it.languageCode.ifBlank { "en" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val historyItems: StateFlow<List<HistoryUiItem>> = combine(
        getWorkoutHistoryUseCase(),
        languageCode
    ) { sessions, lang ->
        // Force recomputation of dynamic headers when language changes.
        sessions to lang
    }
        .map { (sessions, lang) -> groupSessionsByDate(sessions, lang) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun groupSessionsByDate(sessions: List<WorkoutSession>, languageCode: String): List<HistoryUiItem> {
        if (sessions.isEmpty()) return emptyList()

        // Use a localized context that matches the app-selected language.
        val localizedContext = context.createLocalizedContext(languageCode)

        val grouped = sessions.groupBy { session ->
            session.date.toLocalDate()
        }

        val uiList = mutableListOf<HistoryUiItem>()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        grouped.forEach { (date, sessionsInDate) ->
            // 1. Agregar Cabecera
            val title = when (date) {
                today -> localizedContext.getString(R.string.history_today)
                yesterday -> localizedContext.getString(R.string.history_yesterday)
                else -> {
                    // Locale-aware long date, avoids hardcoded Spanish fragments like "de".
                    // Example (ES): "viernes, 10 de abril de 2026"  |  (EN): "Friday, April 10, 2026"
                    val locale = localizedContext.resources.configuration.locales[0] ?: Locale.getDefault()
                    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
                        .replaceFirstChar { it.titlecase(locale) }
                }
            }
            uiList.add(HistoryUiItem.Header(title))

            // 2. Agregar Sesiones de esa fecha
            sessionsInDate.forEach { session ->
                uiList.add(HistoryUiItem.SessionItem(session))
            }
        }
        return uiList
    }
}