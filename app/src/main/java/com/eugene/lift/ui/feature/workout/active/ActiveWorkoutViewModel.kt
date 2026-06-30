package com.eugene.lift.ui.feature.workout.active

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eugene.lift.common.work.ActiveWorkoutReminderScheduler
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.manager.RestTimerManager
import com.eugene.lift.domain.model.ActiveWorkoutDraft
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.ActiveWorkoutDraftRepository
import com.eugene.lift.domain.usecase.exercise.GetExerciseDetailUseCase
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
import com.eugene.lift.domain.usecase.settings.UpdateAutoTimerUseCase
import com.eugene.lift.domain.usecase.settings.UpdateEffortMetricUseCase
import com.eugene.lift.ui.navigation.ActiveWorkoutRoute
import com.eugene.lift.domain.usecase.template.CreateTemplateFromWorkoutUseCase
import com.eugene.lift.domain.usecase.template.UpdateTemplateFromWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.StartEmptyWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.StartWorkoutFromTemplateUseCase
import com.eugene.lift.domain.usecase.workout.FinishWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.GetPersonalRecordUseCase
import com.eugene.lift.domain.usecase.workout.ResolveExerciseHistoryUseCase
import com.eugene.lift.domain.util.ExercisePerformanceEvaluator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
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
    private val resolveExerciseHistoryUseCase: ResolveExerciseHistoryUseCase,
    private val getPersonalRecordUseCase: GetPersonalRecordUseCase,
    private val exercisePerformanceEvaluator: ExercisePerformanceEvaluator,
    private val getSettingsUseCase: GetSettingsUseCase,  // kept private to call cold flow on init
    private val updateEffortMetricUseCase: UpdateEffortMetricUseCase,
    private val updateAutoTimerUseCase: UpdateAutoTimerUseCase,
    private val activeWorkoutDraftRepository: ActiveWorkoutDraftRepository,
    private val activeWorkoutReminderScheduler: ActiveWorkoutReminderScheduler
) : ViewModel() {

    private val routeArgs: ActiveWorkoutRoute? = try {
        savedStateHandle.toRoute<ActiveWorkoutRoute>()
    } catch (_: Exception) {
        null
    }
    private val templateId: String? = routeArgs?.templateId
    private val shouldResumeDraft: Boolean = routeArgs?.resumeDraft == true

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)

    // Store original template exercises for comparison
    private var originalTemplateExercises: List<SessionExercise> = emptyList()
    private var startedAtEpochMillis = System.currentTimeMillis()
    private var lastInteractedAtEpochMillis = startedAtEpochMillis
    private var persistDraftJob: Job? = null

    private val timerState = restTimerManager.timerState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), restTimerManager.timerState.value)

    private val _historyState = MutableStateFlow<Map<String, List<WorkoutSet>>>(emptyMap())
    private val _prefillHistoryState = MutableStateFlow<Map<String, List<WorkoutSet>>>(emptyMap())
    private val _effortMetric = MutableStateFlow<String?>(null) // seeded from persisted settings in init
    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    private val _reorderState = MutableStateFlow(ReorderUiState())

    // Hot StateFlow used by the combine below — the initial value is only a placeholder;
    // actual persisted prefs are eagerly loaded in init via the cold flow.
    private val userSettings: StateFlow<UserSettings> = getSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _isAutoTimerEnabled = MutableStateFlow(true)

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ActiveWorkoutEffect>()
    val effects: SharedFlow<ActiveWorkoutEffect> = _effects.asSharedFlow()

    init {
        // Seed preferences from the COLD upstream flow so we actually suspend
        // until DataStore emits the real persisted value (not the StateFlow initialValue).
        viewModelScope.launch {
            val saved = getSettingsUseCase().firstOrNull()
            if (saved != null) {
                _effortMetric.value = saved.effortMetric
                _isAutoTimerEnabled.value = saved.autoTimerEnabled
            }
        }

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

        combine(sessionSnapshot, userSettings, _isAutoTimerEnabled, _reorderState) { snapshot, settings, autoTimer, reorder ->
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
                    sessionNote = session.note,
                    reorderState = reorder
                )
            }
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)

        userSettings
            .map { it.languageCode }
            .distinctUntilChanged()
            .onEach { refreshLocalizedActiveSession() }
            .launchIn(viewModelScope)
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
            ActiveWorkoutUiEvent.SaveDraftAndExitClicked -> saveDraftAndExit()
            ActiveWorkoutUiEvent.CancelClicked -> cancelWorkout()
            is ActiveWorkoutUiEvent.AddExerciseClicked -> Unit
            is ActiveWorkoutUiEvent.ExerciseClicked -> Unit
            is ActiveWorkoutUiEvent.SessionNoteChanged -> onSessionNoteChange(event.value)
            is ActiveWorkoutUiEvent.ExerciseNoteChanged -> onExerciseNoteChange(event.exerciseIndex, event.value)
            is ActiveWorkoutUiEvent.ExercisesReordered -> reorderExercise(event.fromIndex, event.toIndex)
            is ActiveWorkoutUiEvent.ToggleReorderMode -> toggleReorderMode()
            is ActiveWorkoutUiEvent.RemoveExercise -> removeExercise(event.exerciseIndex)
            is ActiveWorkoutUiEvent.ReplaceExercise -> Unit // Handled by Route via navigation
        }
    }

    private fun toggleReorderMode() {
        val current = _reorderState.value
        _reorderState.value = current.copy(isReorderMode = !current.isReorderMode)
    }

    private fun removeExercise(exerciseIndex: Int) {
        val currentSession = _activeSession.value ?: return
        val exercises = currentSession.exercises.toMutableList()
        if (exerciseIndex in exercises.indices) {
            exercises.removeAt(exerciseIndex)
            setActiveSession(currentSession.copy(exercises = exercises))
        }
    }

    private fun reorderExercise(fromIndex: Int, toIndex: Int) {
        val session = _activeSession.value ?: return
        val exercises = session.exercises.toMutableList()
        if (fromIndex !in exercises.indices || toIndex !in exercises.indices) return
        exercises.add(toIndex, exercises.removeAt(fromIndex))
        setActiveSession(session.copy(exercises = exercises))
    }

    private fun initializeSession() {
        viewModelScope.launch {
            _historyState.value = emptyMap()
            _prefillHistoryState.value = emptyMap()

            val draft = activeWorkoutDraftRepository.getDraft()
            val session = restoreOrCreateSession(draft) ?: run {
                _activeSession.value = null
                return@launch
            }

            loadHistoryForSession(session)
            val localizedSession = localizeSession(session)
            val sessionWithHistory = if (draft != null && shouldUseDraft(draft)) {
                localizedSession
            } else {
                updatedSessionWithHistory(localizedSession)
            }
            setActiveSession(sessionWithHistory)
        }
    }

    private suspend fun restoreOrCreateSession(draft: ActiveWorkoutDraft?): WorkoutSession? {
        if (draft != null && shouldUseDraft(draft)) {
            originalTemplateExercises = draft.originalTemplateExercises.map { it.copy() }
            startedAtEpochMillis = draft.startedAtEpochMillis
            lastInteractedAtEpochMillis = draft.lastInteractedAtEpochMillis
            return draft.session
        }

        val session = createSession() ?: return null
        originalTemplateExercises = session.exercises.map { it.copy() }
        startedAtEpochMillis = System.currentTimeMillis()
        lastInteractedAtEpochMillis = startedAtEpochMillis
        return session
    }

    private fun shouldUseDraft(draft: ActiveWorkoutDraft): Boolean {
        if (shouldResumeDraft) return true
        return draft.session.templateId == templateId
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
        val historySets = _prefillHistoryState.value[sessionExercise.exercise.id]

        // Prefill requirement:
        // - History screen shows "your last time" (last performed session).
        // - Prefill uses the last time for the same template/session, set-by-set,
        //   preserving the original order and allowing different values per set.
        if (historySets.isNullOrEmpty()) return sessionExercise

        val updatedSets = sessionExercise.sets.mapIndexed { index, currentSet ->
            val historySet = historySets.getOrNull(index)

            // Prefill empty fields independently so weight and reps can be restored even
            // when one of them was already initialized upstream.
            if (historySet != null) {
                currentSet.copy(
                    weight = if (currentSet.weight == 0.0) historySet.weight else currentSet.weight,
                    reps = if (currentSet.reps == 0) historySet.reps else currentSet.reps,
                    distance = currentSet.distance ?: historySet.distance,
                    timeSeconds = currentSet.timeSeconds ?: historySet.timeSeconds,
                    rpe = currentSet.rpe ?: historySet.rpe,
                    rir = currentSet.rir ?: historySet.rir
                )
            } else {
                currentSet
            }
        }

        return sessionExercise.copy(sets = updatedSets)
    }

    private fun startSessionTicker() {
        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val elapsed = (now - startedAtEpochMillis) / 1000
                _elapsedTimeSeconds.value = elapsed
                delay(1000)
            }
        }
    }

    private suspend fun localizeSession(session: WorkoutSession): WorkoutSession {
        val localizedExercises = session.exercises.map { sessionExercise ->
            val localizedExercise = getExerciseDetailUseCase(sessionExercise.exercise.id).firstOrNull()
            if (localizedExercise != null) {
                sessionExercise.copy(exercise = localizedExercise)
            } else {
                sessionExercise
            }
        }
        return session.copy(exercises = localizedExercises)
    }

    private suspend fun refreshLocalizedActiveSession() {
        val session = _activeSession.value ?: return
        val localizedSession = localizeSession(session)
        if (localizedSession != session) {
            setActiveSession(localizedSession, markInteraction = false)
        }
    }

    private suspend fun loadHistoryFor(exerciseId: String) {
        val snapshot = resolveExerciseHistoryUseCase(exerciseId, templateId)

        _historyState.value = _historyState.value.toMutableMap().apply {
            this[exerciseId] = snapshot.displaySets
        }
        _prefillHistoryState.value = _prefillHistoryState.value.toMutableMap().apply {
            this[exerciseId] = snapshot.prefillSets
        }
    }

    private fun updateSetState(exerciseIndex: Int, setIndex: Int, update: (WorkoutSet) -> WorkoutSet) {
        val currentSession = _activeSession.value ?: return
        val exercises = currentSession.exercises.toMutableList()
        val targetExercise = exercises[exerciseIndex]
        val sets = targetExercise.sets.toMutableList()

        sets[setIndex] = update(sets[setIndex])

        exercises[exerciseIndex] = targetExercise.copy(sets = sets)
        setActiveSession(currentSession.copy(exercises = exercises))
    }

    private fun onWeightChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
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
        var completedSetSnapshot: WorkoutSet? = null
        updateSetState(exerciseIndex, setIndex) {
            val updatedSet = it.copy(completed = !it.completed)
            isNowCompleted = updatedSet.completed
            if (updatedSet.completed) {
                completedSetSnapshot = updatedSet
            }
            updatedSet
        }

        if (isNowCompleted) {
            val session = _activeSession.value
            val exercise = session?.exercises?.getOrNull(exerciseIndex)
            val completedSet = completedSetSnapshot
            if (exercise != null && completedSet != null) {
                viewModelScope.launch {
                    val measureType = exercise.exercise.measureType
                    val currentPerformance = exercisePerformanceEvaluator.performanceValue(
                        completedSet,
                        measureType
                    )
                    if (currentPerformance <= 0.0) {
                        return@launch
                    }

                    val prSet = getPersonalRecordUseCase(exercise.exercise.id, measureType)
                    val previousPerformance = prSet?.let { previousSet ->
                        exercisePerformanceEvaluator.performanceValue(previousSet, measureType)
                    } ?: 0.0
                    val isPr = currentPerformance > previousPerformance
                    _effects.emit(
                        ActiveWorkoutEffect.ShowExerciseSnackbar(
                            name = exercise.exercise.name,
                            weight = completedSet.weight,
                            weightUnit = userSettings.value.weightUnit,
                            isPr = isPr
                        )
                    )
                }
            }
        }

        if (isNowCompleted && _isAutoTimerEnabled.value) {
            restTimerManager.startTimer(90)
        }
    }

    private fun finishWorkout(updateTemplate: Boolean?) {
        val session = _activeSession.value ?: return

        viewModelScope.launch {
            persistDraftJob?.cancel()
            val finalSession = session.copy(
                durationSeconds = _elapsedTimeSeconds.value
            )

            if (updateTemplate == true && finalSession.templateId != null) {
                updateTemplateFromWorkoutUseCase(finalSession)
            }

            if (updateTemplate == true && finalSession.templateId == null) {
                createTemplateFromWorkoutUseCase(finalSession)
            }

            when (val result = finishWorkoutUseCase(finalSession)) {
                is AppResult.Success -> {
                    restTimerManager.stopTimer()
                    clearDraftArtifacts()
                    _effects.emit(ActiveWorkoutEffect.NavigateBack)
                }
                is AppResult.Error -> {
                    if (result.error == AppError.Validation) {
                        _effects.emit(ActiveWorkoutEffect.ShowSnackbarMessage(com.eugene.lift.R.string.workout_finish_needs_completed_set))
                    } else {
                        _effects.emit(ActiveWorkoutEffect.ShowSnackbar(result.error))
                    }
                }
            }
        }
    }

    private fun saveDraftAndExit() {
        viewModelScope.launch {
            persistDraftJob?.cancel()
            restTimerManager.stopTimer()
            lastInteractedAtEpochMillis = System.currentTimeMillis()
            persistCurrentDraft()
            _effects.emit(ActiveWorkoutEffect.NavigateBack)
        }
    }

    private fun cancelWorkout() {
        viewModelScope.launch {
            persistDraftJob?.cancel()
            restTimerManager.stopTimer()
            clearDraftArtifacts()
            _effects.emit(ActiveWorkoutEffect.NavigateBack)
        }
    }

    private fun updateExerciseNote(exerciseIndex: Int, note: String?) {
        val currentSession = _activeSession.value ?: return
        val exercises = currentSession.exercises.toMutableList()
        val targetExercise = exercises[exerciseIndex]
        exercises[exerciseIndex] = targetExercise.copy(note = note)
        setActiveSession(currentSession.copy(exercises = exercises))
    }

    private fun onExerciseNoteChange(exerciseIndex: Int, newValue: String) {
        updateExerciseNote(exerciseIndex, newValue.ifEmpty { null })
    }

    private fun onSessionNoteChange(newValue: String) {
        val currentSession = _activeSession.value ?: return
        setActiveSession(currentSession.copy(note = newValue.ifEmpty { null }))
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

    private fun setEffortMetric(metric: String?) {
        _effortMetric.value = metric
        viewModelScope.launch { updateEffortMetricUseCase(metric) }
    }

    private fun toggleAutoTimer() {
        val newValue = !_isAutoTimerEnabled.value
        _isAutoTimerEnabled.value = newValue
        viewModelScope.launch { updateAutoTimerUseCase(newValue) }
    }

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
        setActiveSession(currentSession.copy(exercises = exercises))
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

        setActiveSession(currentSession.copy(exercises = exercises))
    }

    fun onAddExercisesToSession(exerciseIds: List<String>) {
        viewModelScope.launch {
            val currentSession = _activeSession.value ?: return@launch

            val newExercises = exerciseIds.mapNotNull { exerciseId ->
                createSessionExerciseFromId(exerciseId)
            }

            setActiveSession(currentSession.copy(
                exercises = currentSession.exercises + newExercises
            ))
        }
    }

    fun replaceExerciseInSession(exerciseIndex: Int, newExerciseId: String) {
        viewModelScope.launch {
            val currentSession = _activeSession.value ?: return@launch
            val exercises = currentSession.exercises.toMutableList()
            if (exerciseIndex !in exercises.indices) return@launch

            val oldExercise = exercises[exerciseIndex]
            val newExerciseDef = getExerciseDetailUseCase(newExerciseId).firstOrNull() ?: return@launch

            loadHistoryFor(newExerciseId)
            val historySets = _prefillHistoryState.value[newExerciseId].orEmpty()

            val newSets = List(oldExercise.sets.size) { idx ->
                val historicalSet = historySets.getOrNull(idx)
                WorkoutSet(
                    id = UUID.randomUUID().toString(),
                    weight = historicalSet?.weight ?: 0.0,
                    reps = historicalSet?.reps ?: 0,
                    completed = false
                )
            }

            exercises[exerciseIndex] = oldExercise.copy(
                id = UUID.randomUUID().toString(),
                exercise = newExerciseDef,
                sets = newSets
            )
            setActiveSession(currentSession.copy(exercises = exercises))
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
        val historySets = _prefillHistoryState.value[exerciseId]
        val first = historySets?.firstOrNull()
        return if (first != null) first.weight to first.reps else 0.0 to 0
    }

    private fun setActiveSession(
        session: WorkoutSession,
        markInteraction: Boolean = true
    ) {
        _activeSession.value = session
        if (markInteraction) {
            lastInteractedAtEpochMillis = System.currentTimeMillis()
        }
        scheduleDraftPersistence()
        activeWorkoutReminderScheduler.schedule()
    }

    private fun scheduleDraftPersistence() {
        persistDraftJob?.cancel()
        persistDraftJob = viewModelScope.launch {
            delay(300)
            persistCurrentDraft()
        }
    }

    private suspend fun persistCurrentDraft() {
        val session = _activeSession.value ?: return
        activeWorkoutDraftRepository.saveDraft(
            ActiveWorkoutDraft(
                session = session,
                originalTemplateExercises = originalTemplateExercises,
                startedAtEpochMillis = startedAtEpochMillis,
                lastInteractedAtEpochMillis = lastInteractedAtEpochMillis
            )
        )
    }

    private suspend fun clearDraftArtifacts() {
        activeWorkoutDraftRepository.clearDraft()
        activeWorkoutReminderScheduler.cancel()
    }
}

private data class SessionSnapshot(
    val session: WorkoutSession?,
    val history: Map<String, List<WorkoutSet>>,
    val effortMetric: String?,
    val timerState: com.eugene.lift.domain.model.TimerState,
    val elapsedTime: Long
)
