package com.eugene.lift.ui.feature.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.R
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.usecase.history.GetWorkoutHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    val historyItems: StateFlow<List<HistoryUiItem>> = getWorkoutHistoryUseCase()
        .map { sessions ->
            groupSessionsByDate(sessions)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun groupSessionsByDate(sessions: List<WorkoutSession>): List<HistoryUiItem> {
        if (sessions.isEmpty()) return emptyList()

        val grouped = sessions.groupBy { session ->
            session.date.toLocalDate()
        }

        val uiList = mutableListOf<HistoryUiItem>()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        grouped.forEach { (date, sessionsInDate) ->
            // 1. Agregar Cabecera
            val title = when (date) {
                today -> context.getString(R.string.history_today)
                yesterday -> context.getString(R.string.history_yesterday)
                else -> date.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.getDefault()))
                    .replaceFirstChar { it.uppercase() } // Capitalizar
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