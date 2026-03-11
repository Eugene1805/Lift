package com.eugene.lift.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseProgression
import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import com.eugene.lift.domain.usecase.exercise.GetExerciseProgressionUseCase
import com.eugene.lift.domain.usecase.exercise.GetExercisesUseCase
import com.eugene.lift.domain.usecase.profile.GetCurrentProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
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
    val stats: ProfileStats = ProfileStats(),
    // Exercise progression
    val progressions: List<ExerciseProgression> = emptyList(),
    val allExercises: List<Exercise> = emptyList(),
    val isProgressionLoading: Boolean = false,
    val showExercisePickerDialog: Boolean = false,
    val weightUnit: String = "kg"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val workoutRepository: WorkoutRepository,
    private val getExercisesUseCase: GetExercisesUseCase,
    private val getExerciseProgressionUseCase: GetExerciseProgressionUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var allSessions: List<WorkoutSession> = emptyList()

    // Active progression observation jobs, keyed by exerciseId
    private val progressionJobs = mutableMapOf<String, Job>()

    init {
        loadSettings()
        loadProfile()
        loadWorkoutHistory()
        loadExercises()
        observeTrackedExercises()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(
                    weightUnit = if (settings.weightUnit == com.eugene.lift.domain.model.WeightUnit.LBS) "lbs" else "kg"
                )
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = getCurrentProfileUseCase.getOrCreate()
            _uiState.value = _uiState.value.copy(profile = profile, isLoading = false)

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

    private fun loadExercises() {
        viewModelScope.launch {
            getExercisesUseCase(com.eugene.lift.domain.usecase.exercise.ExerciseFilter())
                .collect { exercises ->
                    _uiState.value = _uiState.value.copy(allExercises = exercises)
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTrackedExercises() {
        viewModelScope.launch {
            settingsRepository.getTrackedExerciseIds().collect { trackedIds ->
                refreshProgressionObservers(trackedIds)
            }
        }
    }

    /**
     * Starts/stops flow-collection jobs so only exercises that are currently
     * tracked are being observed. Cancelled jobs clean up automatically.
     */
    private fun refreshProgressionObservers(trackedIds: List<String>) {
        // Cancel jobs for exercises that have been un-tracked
        val toRemove = progressionJobs.keys - trackedIds.toSet()
        toRemove.forEach { id ->
            progressionJobs.remove(id)?.cancel()
        }
        // Remove progressions for un-tracked exercises from state
        if (toRemove.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                progressions = _uiState.value.progressions.filter { it.exerciseId in trackedIds }
            )
        }

        // Start new jobs for newly tracked exercises
        trackedIds.forEach { exerciseId ->
            if (progressionJobs.containsKey(exerciseId)) return@forEach
            progressionJobs[exerciseId] = viewModelScope.launch {
                val exercises = _uiState.value.allExercises.ifEmpty {
                    // Wait for exercises to load if not yet available
                    getExercisesUseCase(com.eugene.lift.domain.usecase.exercise.ExerciseFilter()).first()
                }
                val exercise = exercises.firstOrNull { it.id == exerciseId } ?: return@launch

                getExerciseProgressionUseCase(
                    exerciseId = exerciseId,
                    exerciseName = exercise.name,
                    measureType = exercise.measureType
                ).collect { progression ->
                    val current = _uiState.value.progressions.toMutableList()
                    val existingIndex = current.indexOfFirst { it.exerciseId == exerciseId }
                    if (existingIndex >= 0) {
                        current[existingIndex] = progression
                    } else {
                        current.add(progression)
                    }
                    _uiState.value = _uiState.value.copy(progressions = current.toList())
                }
            }
        }
    }

    // ── Public events ────────────────────────────────────────────────────────

    fun setTimeRange(range: TimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = range)
        calculateStats()
    }

    fun showExercisePicker() {
        _uiState.value = _uiState.value.copy(showExercisePickerDialog = true)
    }

    fun hideExercisePicker() {
        _uiState.value = _uiState.value.copy(showExercisePickerDialog = false)
    }

    fun toggleTrackedExercise(exerciseId: String) {
        viewModelScope.launch {
            val current = settingsRepository.getTrackedExerciseIds().first().toMutableList()
            if (exerciseId in current) {
                current.remove(exerciseId)
            } else if (current.size < GetExerciseProgressionUseCase.MAX_TRACKED) {
                current.add(exerciseId)
            }
            settingsRepository.setTrackedExerciseIds(current)
        }
    }

    fun removeTrackedExercise(exerciseId: String) {
        viewModelScope.launch {
            val current = settingsRepository.getTrackedExerciseIds().first().toMutableList()
            current.remove(exerciseId)
            settingsRepository.setTrackedExerciseIds(current)
        }
    }

    // ── Stats calculation ────────────────────────────────────────────────────

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
            TimeRange.ALL_TIME -> allSessions
        }

        val groupedByWeek = sessions.groupBy { session ->
            session.date.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        val sortedWeeks = groupedByWeek.entries.sortedBy { it.key }
        val weekLabelFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

        val durationData = sortedWeeks.map { (weekStart, weekSessions) ->
            val totalMinutes = weekSessions.sumOf { it.durationSeconds } / 60.0
            HistogramDataPoint(label = weekStart.format(weekLabelFmt), value = totalMinutes)
        }

        val volumeData = sortedWeeks.map { (weekStart, weekSessions) ->
            val totalVolume = weekSessions.sumOf { session ->
                session.exercises.flatMap { it.sets }
                    .filter { it.completed }
                    .sumOf { it.weight * it.reps }
            }
            HistogramDataPoint(label = weekStart.format(weekLabelFmt), value = totalVolume)
        }

        val repsData = sortedWeeks.map { (weekStart, weekSessions) ->
            val totalReps = weekSessions.sumOf { session ->
                session.exercises.flatMap { it.sets }
                    .filter { it.completed }
                    .sumOf { it.reps }
            }
            HistogramDataPoint(label = weekStart.format(weekLabelFmt), value = totalReps.toDouble())
        }

        val workoutsPerWeek = sortedWeeks.map { (weekStart, weekSessions) ->
            HistogramDataPoint(label = weekStart.format(weekLabelFmt), value = weekSessions.size.toDouble())
        }

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
