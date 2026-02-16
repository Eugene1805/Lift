package com.eugene.lift.ui.feature.workout.active

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.manager.RestTimerManager
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.usecase.exercise.GetExerciseDetailUseCase
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
import com.eugene.lift.domain.usecase.template.CreateTemplateFromWorkoutUseCase
import com.eugene.lift.domain.usecase.template.UpdateTemplateFromWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.StartEmptyWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.StartWorkoutFromTemplateUseCase
import com.eugene.lift.domain.usecase.workout.FinishWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.GetLastHistoryForExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startWorkoutFromTemplateUseCase: StartWorkoutFromTemplateUseCase,
    private val startEmptyWorkoutUseCase: StartEmptyWorkoutUseCase,
    private val finishWorkoutUseCase: FinishWorkoutUseCase,
    private val updateTemplateFromWorkoutUseCase: UpdateTemplateFromWorkoutUseCase,
    private val createTemplateFromWorkoutUseCase: CreateTemplateFromWorkoutUseCase,
    private val restTimerManager: RestTimerManager,
    private val getExerciseDetailUseCase: GetExerciseDetailUseCase,
    private val getLastHistoryForExerciseUseCase: GetLastHistoryForExerciseUseCase,
    getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    private val templateId: String? = savedStateHandle["templateId"]

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)

    // Store original template exercises for comparison
    private var originalTemplateExercises: List<SessionExercise> = emptyList()

    private val timerState = restTimerManager.timerState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), restTimerManager.timerState.value)

    private val _historyState = MutableStateFlow<Map<String, List<WorkoutSet>>>(emptyMap())
    private val _effortMetric = MutableStateFlow<String?>("RIR") // "RPE", "RIR" o null
    private val _elapsedTimeSeconds = MutableStateFlow(0L)

    private val userSettings: StateFlow<UserSettings> = getSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _isAutoTimerEnabled = MutableStateFlow(true)

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ActiveWorkoutEffect>()
    val effects: SharedFlow<ActiveWorkoutEffect> = _effects

    init {

        initializeSession()
        startSessionTicker()

        val sessionSnapshot = combine(
            _activeSession,
            _historyState,
            _effortMetric,
            timerState,
            _elapsedTimeSeconds
        ) { session, history, effort, timer, elapsed ->
            SessionSnapshot(session, history, effort, timer, elapsed)
        }

        combine(sessionSnapshot, userSettings, _isAutoTimerEnabled) { snapshot, settings, autoTimer ->
            val session = snapshot.session
            if (session == null) {
                ActiveWorkoutUiState(isLoading = true)
            } else {
                ActiveWorkoutUiState(
                    isLoading = false,
                    sessionName = session.name,
                    exercises = session.exercises,
                    history = snapshot.history,
                    effortMetric = snapshot.effortMetric,
                    timerState = snapshot.timerState,
                    elapsedTime = snapshot.elapsedTime,
                    userSettings = settings,
                    isAutoTimerEnabled = autoTimer,
                    hasTemplate = session.templateId != null,
                    hasWorkoutBeenModified = hasWorkoutBeenModified(session),
                    sessionNote = session.note
                )
            }
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }

    fun onEvent(event: ActiveWorkoutUiEvent) {
        when (event) {
            is ActiveWorkoutUiEvent.WeightChanged -> onWeightChange(event.exerciseIndex, event.setIndex, event.value)
            is ActiveWorkoutUiEvent.RepsChanged -> onRepsChange(event.exerciseIndex, event.setIndex, event.value)
            is ActiveWorkoutUiEvent.DistanceChanged -> onDistanceChange(event.exerciseIndex, event.setIndex, event.value)
            is ActiveWorkoutUiEvent.TimeChanged -> onTimeChange(event.exerciseIndex, event.setIndex, event.value)
            is ActiveWorkoutUiEvent.RpeChanged -> onRpeChange(event.exerciseIndex, event.setIndex, event.value)
            is ActiveWorkoutUiEvent.RirChanged -> onRirChange(event.exerciseIndex, event.setIndex, event.value)
            is ActiveWorkoutUiEvent.SetCompleted -> toggleSetCompleted(event.exerciseIndex, event.setIndex)
            is ActiveWorkoutUiEvent.AddSet -> addSet(event.exerciseIndex)
            is ActiveWorkoutUiEvent.RemoveSet -> removeSet(event.exerciseIndex, event.setIndex)
            is ActiveWorkoutUiEvent.MetricChanged -> setEffortMetric(event.metric)
            is ActiveWorkoutUiEvent.TimerAdded -> addTime(event.seconds)
            ActiveWorkoutUiEvent.TimerStopped -> stopTimer()
            ActiveWorkoutUiEvent.ToggleAutoTimer -> toggleAutoTimer()
            is ActiveWorkoutUiEvent.FinishClicked -> finishWorkout(event.updateTemplate)
            ActiveWorkoutUiEvent.CancelClicked -> cancelWorkout()
            is ActiveWorkoutUiEvent.AddExerciseClicked -> Unit
            is ActiveWorkoutUiEvent.ExerciseClicked -> Unit
            is ActiveWorkoutUiEvent.SessionNoteChanged -> onSessionNoteChange(event.value)
            is ActiveWorkoutUiEvent.ExerciseNoteChanged -> onExerciseNoteChange(event.exerciseIndex, event.value)
        }
    }

    private fun initializeSession() {
        viewModelScope.launch {
            val session = createSession() ?: run {
                _activeSession.value = null
                return@launch
            }

            // Store original exercises for change detection
            originalTemplateExercises = session.exercises.map { it.copy() }

            loadHistoryForSession(session)
            _activeSession.value = updatedSessionWithHistory(session)
        }
    }

    private suspend fun createSession(): WorkoutSession? {
        return if (templateId != null) {
            startWorkoutFromTemplateUseCase(templateId)
        } else {
            startEmptyWorkoutUseCase()
        }
    }

    private suspend fun loadHistoryForSession(session: WorkoutSession) {
        for (sessionExercise in session.exercises) {
            loadHistoryFor(sessionExercise.exercise.id)
        }
    }

    private fun updatedSessionWithHistory(session: WorkoutSession): WorkoutSession {
        val updatedExercises = session.exercises.map { updateExerciseWithHistory(it) }
        return session.copy(exercises = updatedExercises)
    }

    private fun updateExerciseWithHistory(sessionExercise: SessionExercise): SessionExercise {
        val historySets = _historyState.value[sessionExercise.exercise.id]
        val lastHistorySet = historySets?.lastOrNull()

        return if (lastHistorySet == null) {
            sessionExercise // No history, leave as is
        } else {
            // History is already in display units (converted in loadHistoryFor)
            val weightFromHistory = lastHistorySet.weight
            val repsFromHistory = lastHistorySet.reps

            val updatedSets = sessionExercise.sets.map { currentSet ->
                // If the set is "empty" (0/0), fill with history (in display units)
                if (currentSet.weight == 0.0 && currentSet.reps == 0) {
                    currentSet.copy(weight = weightFromHistory, reps = repsFromHistory)
                } else {
                    currentSet
                }
            }
            sessionExercise.copy(sets = updatedSets)
        }
    }

    private fun startSessionTicker() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                val elapsed = (now - startTime) / 1000
                _elapsedTimeSeconds.value = elapsed
                delay(1000)
            }
        }
    }

    private suspend fun loadHistoryFor(exerciseId: String) {
        val lastSession = getLastHistoryForExerciseUseCase(exerciseId)
        // Los pesos de lastSession YA vienen en la unidad de preferencia desde el repositorio
        if (lastSession != null) {
            val oldExercise = lastSession.exercises.find { it.exercise.id == exerciseId }
            if (oldExercise != null) {
                val displaySets = oldExercise.sets
                val currentMap = _historyState.value.toMutableMap()
                currentMap[exerciseId] = displaySets
                _historyState.value = currentMap
            }
        }
    }

    private fun updateSetState(exerciseIndex: Int, setIndex: Int, update: (WorkoutSet) -> WorkoutSet) {
        val currentSession = _activeSession.value ?: return
        val exercises = currentSession.exercises.toMutableList()
        val targetExercise = exercises[exerciseIndex]
        val sets = targetExercise.sets.toMutableList()

        sets[setIndex] = update(sets[setIndex])

        exercises[exerciseIndex] = targetExercise.copy(sets = sets)
        _activeSession.value = currentSession.copy(exercises = exercises)
    }

    private fun onWeightChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        // Store weight directly as entered (in display units)
        // Conversion to kg happens only when finishing the workout
        val weight = newValue.toDoubleOrNull() ?: 0.0
        updateSetState(exerciseIndex, setIndex) { it.copy(weight = weight) }
    }

    private fun onRepsChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val reps = newValue.toIntOrNull() ?: 0
        updateSetState(exerciseIndex, setIndex) { it.copy(reps = reps) }
    }

    private fun onDistanceChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val dist = newValue.toDoubleOrNull()
        updateSetState(exerciseIndex, setIndex) { it.copy(distance = dist) }
    }

    private fun onTimeChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val seconds = newValue.toLongOrNull()
        updateSetState(exerciseIndex, setIndex) { it.copy(timeSeconds = seconds) }
    }


    private fun onRpeChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        if (newValue.isEmpty()) {
            updateSetState(exerciseIndex, setIndex) { it.copy(rpe = null) }
            return
        }
        newValue.toDoubleOrNull()?.let {
            if (it in 1.0..10.0) {
                updateSetState(exerciseIndex, setIndex) { set -> set.copy(rpe = it) }
            }
        }
    }

    private fun onRirChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        if (newValue.isEmpty()) {
            updateSetState(exerciseIndex, setIndex) { it.copy(rir = null) }
            return
        }
        newValue.toIntOrNull()?.let {
            if (it in 0..10) {
                updateSetState(exerciseIndex, setIndex) { set -> set.copy(rir = it) }
            }
        }
    }

    private fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) {
        var isNowCompleted = false
        updateSetState(exerciseIndex, setIndex) {
            isNowCompleted = !it.completed
            it.copy(completed = isNowCompleted)
        }

        if (isNowCompleted && _isAutoTimerEnabled.value) {
            restTimerManager.startTimer(90)
        }
    }

    private fun finishWorkout(updateTemplate: Boolean?) {
        val session = _activeSession.value ?: return

        viewModelScope.launch {
            try {
                val finalSession = session.copy(
                    durationSeconds = _elapsedTimeSeconds.value
                )

                if (updateTemplate == true && finalSession.templateId != null) {
                    updateTemplateFromWorkoutUseCase(finalSession)
                }

                if (updateTemplate == true && finalSession.templateId == null) {
                    createTemplateFromWorkoutUseCase(finalSession)
                }

                finishWorkoutUseCase(finalSession)
                restTimerManager.stopTimer()
                _effects.emit(ActiveWorkoutEffect.NavigateBack)
            } catch (e: Exception) {
                Log.e("ActiveWorkoutVM", "Error al guardar", e)
            }
        }
    }

    private fun cancelWorkout() {
        viewModelScope.launch {
            restTimerManager.stopTimer()
            _effects.emit(ActiveWorkoutEffect.NavigateBack)
        }
    }

    private fun updateExerciseNote(exerciseIndex: Int, note: String?) {
        val currentSession = _activeSession.value ?: return
        val exercises = currentSession.exercises.toMutableList()
        val targetExercise = exercises[exerciseIndex]
        exercises[exerciseIndex] = targetExercise.copy(note = note)
        _activeSession.value = currentSession.copy(exercises = exercises)
    }

    private fun onExerciseNoteChange(exerciseIndex: Int, newValue: String) {
        updateExerciseNote(exerciseIndex, newValue.ifEmpty { null })
    }

    private fun onSessionNoteChange(newValue: String) {
        val currentSession = _activeSession.value ?: return
        _activeSession.value = currentSession.copy(note = newValue.ifEmpty { null })
    }

    private fun hasWorkoutBeenModified(session: WorkoutSession): Boolean {
        if (session.templateId == null) return false

        val currentExercises = session.exercises
        if (originalTemplateExercises.size != currentExercises.size) return true

        for (i in originalTemplateExercises.indices) {
            val originalEx = originalTemplateExercises[i]
            val currentEx = currentExercises.getOrNull(i) ?: return true

            if (originalEx.exercise.id != currentEx.exercise.id) return true
            if (originalEx.sets.size != currentEx.sets.size) return true
        }

        return false
    }

    fun addTime(seconds: Long) = restTimerManager.addTime(seconds)
    fun stopTimer() = restTimerManager.stopTimer()

    private fun addSet(exerciseIndex: Int) {
        val currentSession = _activeSession.value ?: return
        val exercises = currentSession.exercises.toMutableList()
        val targetExercise = exercises[exerciseIndex]
        val previousSet = targetExercise.sets.lastOrNull()

        val newSet = WorkoutSet(
            id = UUID.randomUUID().toString(),
            weight = previousSet?.weight ?: 0.0,
            reps = previousSet?.reps ?: 0,
            completed = false
        )

        val newSets = targetExercise.sets + newSet
        exercises[exerciseIndex] = targetExercise.copy(sets = newSets)
        _activeSession.value = currentSession.copy(exercises = exercises)
    }

    private fun removeSet(exerciseIndex: Int, setIndex: Int) {
        val currentSession = _activeSession.value ?: return
        val exercises = currentSession.exercises.toMutableList()
        val targetExercise = exercises[exerciseIndex]
        val newSets = targetExercise.sets.toMutableList()
        if (setIndex in newSets.indices) {
            newSets.removeAt(setIndex)
        }

        if (newSets.isEmpty()) {
            exercises.removeAt(exerciseIndex)
        } else {
            exercises[exerciseIndex] = targetExercise.copy(sets = newSets)
        }

        _activeSession.value = currentSession.copy(exercises = exercises)
    }

    fun onAddExercisesToSession(exerciseIds: List<String>) {
        viewModelScope.launch {
            val currentSession = _activeSession.value ?: return@launch

            val newExercises = exerciseIds.mapNotNull { exerciseId ->
                createSessionExerciseFromId(exerciseId)
            }

            _activeSession.value = currentSession.copy(
                exercises = currentSession.exercises + newExercises
            )
        }
    }

    private suspend fun createSessionExerciseFromId(exerciseId: String): SessionExercise? {
        val exerciseDef = getExerciseDetailUseCase(exerciseId).firstOrNull() ?: return null

        loadHistoryFor(exerciseId)

        val (initialWeightKg, initialReps) = getInitialSetDataFromHistory(exerciseId)

        val initialSets = (1..3).map {
            WorkoutSet(
                id = UUID.randomUUID().toString(),
                weight = initialWeightKg,
                reps = initialReps,
                completed = false
            )
        }

        return SessionExercise(
            id = UUID.randomUUID().toString(),
            exercise = exerciseDef,
            sets = initialSets
        )
    }

    private fun getInitialSetDataFromHistory(exerciseId: String): Pair<Double, Int> {
        val lastHistorySet = _historyState.value[exerciseId]?.lastOrNull()

        return if (lastHistorySet != null) {
            // History ya está en unidades de presentación; mantener tal cual
            lastHistorySet.weight to lastHistorySet.reps
        } else {
            0.0 to 0
        }
    }

    private fun toggleAutoTimer() {
        _isAutoTimerEnabled.value = !_isAutoTimerEnabled.value
    }

    private fun setEffortMetric(metric: String?) {
        _effortMetric.value = metric
    }
}

private data class SessionSnapshot(
    val session: WorkoutSession?,
    val history: Map<String, List<WorkoutSet>>,
    val effortMetric: String?,
    val timerState: com.eugene.lift.domain.model.TimerState,
    val elapsedTime: Long
)
