package com.eugene.lift.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.WorkoutRepository
import com.eugene.lift.domain.usecase.profile.GetCurrentProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Time range for histogram data
 */
enum class TimeRange {
    WEEK, MONTH, THREE_MONTHS, SIX_MONTHS, YEAR, ALL_TIME
}

/**
 * Data point for histogram charts
 */
data class HistogramDataPoint(
    val label: String,
    val value: Double
)

/**
 * Stats data for the dashboard
 */
data class ProfileStats(
    val durationData: List<HistogramDataPoint> = emptyList(),
    val volumeData: List<HistogramDataPoint> = emptyList(),
    val repsData: List<HistogramDataPoint> = emptyList(),
    val workoutsPerWeek: List<HistogramDataPoint> = emptyList(),
    val averageWorkoutsPerWeek: Double = 0.0
)

/**
 * UI state for the profile screen
 */
data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val selectedTimeRange: TimeRange = TimeRange.MONTH,
    val stats: ProfileStats = ProfileStats()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var allSessions: List<WorkoutSession> = emptyList()

    init {
        loadProfile()
        loadWorkoutHistory()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            // Get or create profile on first launch
            val profile = getCurrentProfileUseCase.getOrCreate()
            _uiState.value = _uiState.value.copy(
                profile = profile,
                isLoading = false
            )

            // Observe profile changes
            getCurrentProfileUseCase().collect { updatedProfile ->
                if (updatedProfile != null) {
                    _uiState.value = _uiState.value.copy(profile = updatedProfile)
                }
            }
        }
    }

    private fun loadWorkoutHistory() {
        viewModelScope.launch {
            workoutRepository.getHistory(from = null, to = null).collect { sessions ->
                allSessions = sessions
                calculateStats()
            }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = range)
        calculateStats()
    }

    private fun calculateStats() {
        val range = _uiState.value.selectedTimeRange
        val now = LocalDate.now()

        val sessions = when (range) {
            TimeRange.WEEK -> {
                val start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                allSessions.filter { it.date.toLocalDate() >= start }
            }
            TimeRange.MONTH -> {
                val start = now.minusMonths(1)
                allSessions.filter { it.date.toLocalDate() >= start }
            }
            TimeRange.THREE_MONTHS -> {
                val start = now.minusMonths(3)
                allSessions.filter { it.date.toLocalDate() >= start }
            }
            TimeRange.SIX_MONTHS -> {
                val start = now.minusMonths(6)
                allSessions.filter { it.date.toLocalDate() >= start }
            }
            TimeRange.YEAR -> {
                val start = now.minusYears(1)
                allSessions.filter { it.date.toLocalDate() >= start }
            }
            TimeRange.ALL_TIME -> {
                allSessions
            }
        }

        val groupedByWeek = sessions.groupBy { session ->
            session.date.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        // Duration data (in minutes)
        val durationData = groupedByWeek.map { (weekStart, weekSessions) ->
            val totalMinutes = weekSessions.sumOf { it.durationSeconds } / 60.0
            HistogramDataPoint(
                label = "${weekStart.dayOfMonth}/${weekStart.monthValue}",
                value = totalMinutes
            )
        }.sortedBy { it.label }

        // Volume data (in kg)
        val volumeData = groupedByWeek.map { (weekStart, weekSessions) ->
            val totalVolume = weekSessions.sumOf { session ->
                session.exercises.flatMap { it.sets }
                    .filter { it.completed }
                    .sumOf { it.weight * it.reps }
            }
            HistogramDataPoint(
                label = "${weekStart.dayOfMonth}/${weekStart.monthValue}",
                value = totalVolume
            )
        }.sortedBy { it.label }

        // Reps data
        val repsData = groupedByWeek.map { (weekStart, weekSessions) ->
            val totalReps = weekSessions.sumOf { session ->
                session.exercises.flatMap { it.sets }
                    .filter { it.completed }
                    .sumOf { it.reps }
            }
            HistogramDataPoint(
                label = "${weekStart.dayOfMonth}/${weekStart.monthValue}",
                value = totalReps.toDouble()
            )
        }.sortedBy { it.label }

        // Workouts per week
        val workoutsPerWeek = groupedByWeek.map { (weekStart, weekSessions) ->
            HistogramDataPoint(
                label = "${weekStart.dayOfMonth}/${weekStart.monthValue}",
                value = weekSessions.size.toDouble()
            )
        }.sortedBy { it.label }

        val avgWorkoutsPerWeek = if (groupedByWeek.isNotEmpty()) {
            sessions.size.toDouble() / groupedByWeek.size
        } else 0.0

        _uiState.value = _uiState.value.copy(
            stats = ProfileStats(
                durationData = durationData,
                volumeData = volumeData,
                repsData = repsData,
                workoutsPerWeek = workoutsPerWeek,
                averageWorkoutsPerWeek = avgWorkoutsPerWeek
            )
        )
    }
}
