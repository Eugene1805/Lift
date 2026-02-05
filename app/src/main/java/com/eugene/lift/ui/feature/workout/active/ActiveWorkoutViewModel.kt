package com.eugene.lift.ui.feature.workout.active

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.manager.RestTimerManager
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.usecase.GetExerciseDetailUseCase
import com.eugene.lift.domain.usecase.GetSettingsUseCase
import com.eugene.lift.domain.usecase.template.CreateTemplateFromWorkoutUseCase
import com.eugene.lift.domain.usecase.template.UpdateTemplateFromWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.StartEmptyWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.StartWorkoutFromTemplateUseCase
import com.eugene.lift.domain.usecase.workout.FinishWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.GetLastHistoryForExerciseUseCase
import com.eugene.lift.domain.util.WeightConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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
    private val getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    private val templateId: String? = savedStateHandle["templateId"]

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)
    val activeSession = _activeSession.asStateFlow()

    // Store original template exercises for comparison
    private var originalTemplateExercises: List<SessionExercise> = emptyList()

    val timerState = restTimerManager.timerState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), restTimerManager.timerState.value)

    private val _historyState = MutableStateFlow<Map<String, List<WorkoutSet>>>(emptyMap())
    val historyState = _historyState.asStateFlow()

    private val _effortMetric = MutableStateFlow<String?>("RIR") // "RPE", "RIR" o null
    val effortMetric = _effortMetric.asStateFlow()

    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    val elapsedTimeSeconds = _elapsedTimeSeconds.asStateFlow()

    val userSettings = getSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _isAutoTimerEnabled = MutableStateFlow(true)
    val isAutoTimerEnabled = _isAutoTimerEnabled.asStateFlow()

    init {
        initializeSession()
        startSessionTicker()
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
        val unit = userSettings.value.weightUnit

        if (lastSession != null) {
            val oldExercise = lastSession.exercises.find { it.exercise.id == exerciseId }
            if (oldExercise != null) {
                val displaySets = oldExercise.sets.map { set ->
                    if (unit == WeightUnit.LBS) {
                        set.copy(weight = WeightConverter.kgToLbs(set.weight))
                    } else {
                        set
                    }
                }
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

    fun onWeightChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        // Store weight directly as entered (in display units)
        // Conversion to kg happens only when finishing the workout
        val weight = newValue.toDoubleOrNull() ?: 0.0
        updateSetState(exerciseIndex, setIndex) { it.copy(weight = weight) }
    }

    fun onRepsChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val reps = newValue.toIntOrNull() ?: 0
        updateSetState(exerciseIndex, setIndex) { it.copy(reps = reps) }
    }

    fun onDistanceChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val dist = newValue.toDoubleOrNull()
        updateSetState(exerciseIndex, setIndex) { it.copy(distance = dist) }
    }

    fun onTimeChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val seconds = newValue.toLongOrNull()
        updateSetState(exerciseIndex, setIndex) { it.copy(timeSeconds = seconds) }
    }


    fun onRpeChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
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

    fun onRirChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
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

    fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) {
        var isNowCompleted = false
        updateSetState(exerciseIndex, setIndex) {
            isNowCompleted = !it.completed
            it.copy(completed = isNowCompleted)
        }

        if (isNowCompleted && _isAutoTimerEnabled.value) {
            restTimerManager.startTimer(90)
        }
    }

fun finishWorkout(updateTemplate: Boolean?, onSuccess: () -> Unit) {
        val session = _activeSession.value

        if (session == null) {
            return
        }

        val currentSettings = userSettings.value

        viewModelScope.launch {
            try {
                // Convert weights from display units to kg for storage
                val convertedExercises = session.exercises.map { sessionExercise ->
                    val convertedSets = sessionExercise.sets.map { set ->
                        val weightInKg = if (currentSettings.weightUnit == WeightUnit.LBS) {
                            WeightConverter.lbsToKg(set.weight)
                        } else {
                            set.weight
                        }
                        set.copy(weight = weightInKg)
                    }
                    sessionExercise.copy(sets = convertedSets)
                }

                val finalSession = session.copy(
                    durationSeconds = _elapsedTimeSeconds.value,
                    exercises = convertedExercises
                )

                // Update existing template if requested
                if (updateTemplate == true && finalSession.templateId != null) {
                    updateTemplateFromWorkoutUseCase(finalSession)
                }

                // Create new template from Quick Start workout if requested
                if (updateTemplate == true && finalSession.templateId == null) {
                    createTemplateFromWorkoutUseCase(finalSession)
                }

                finishWorkoutUseCase(finalSession)
                restTimerManager.stopTimer()
                onSuccess()
            } catch (e: Exception) {
                Log.e("ActiveWorkoutVM", "Error al guardar", e)
            }
        }
    }

    fun hasTemplate(): Boolean {
        return _activeSession.value?.templateId != null
    }

    fun hasWorkoutBeenModified(): Boolean {
        val currentSession = _activeSession.value ?: return false

        // If there's no template, no modification to check
        if (currentSession.templateId == null) return false

        // Get current exercises
        val currentExercises = currentSession.exercises

        // Check if number of exercises changed
        if (originalTemplateExercises.size != currentExercises.size) return true

        // Check if exercise order or exercises changed
        for (i in originalTemplateExercises.indices) {
            val originalEx = originalTemplateExercises[i]
            val currentEx = currentExercises.getOrNull(i) ?: return true

            // Check if exercise ID changed (different exercise in this position)
            if (originalEx.exercise.id != currentEx.exercise.id) return true

            // Check if number of sets changed
            if (originalEx.sets.size != currentEx.sets.size) return true
        }

        return false
    }

    fun addTime(seconds: Long) = restTimerManager.addTime(seconds)
    fun stopTimer() = restTimerManager.stopTimer()

    fun addSet(exerciseIndex: Int) {
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

    fun removeSet(exerciseIndex: Int, setIndex: Int) {
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
            // History is already in display units (converted in loadHistoryFor)
            // We store in display units during the workout, so no conversion needed
            lastHistorySet.weight to lastHistorySet.reps
        } else {
            0.0 to 0
        }
    }

    fun toggleAutoTimer() {
        _isAutoTimerEnabled.value = !_isAutoTimerEnabled.value
    }

    fun setEffortMetric(metric: String?) {
        _effortMetric.value = metric
    }
}
