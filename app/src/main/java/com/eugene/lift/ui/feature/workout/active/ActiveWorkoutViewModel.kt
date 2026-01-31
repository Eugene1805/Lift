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
    private val restTimerManager: RestTimerManager,
    private val getExerciseDetailUseCase: GetExerciseDetailUseCase,
    private val getLastHistoryForExerciseUseCase: GetLastHistoryForExerciseUseCase,
    private val getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    private val templateId: String? = savedStateHandle["templateId"]

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)
    val activeSession = _activeSession.asStateFlow()

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
            val session = if (templateId != null) {
                startWorkoutFromTemplateUseCase(templateId)
            } else {
                startEmptyWorkoutUseCase()
            }
            _activeSession.value = session

            // Cargar historial usando el Use Case
            session?.exercises?.forEach { sessionExercise ->
                loadHistoryFor(sessionExercise.exercise.id)
            }
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
        val weightInDisplayUnit = newValue.toDoubleOrNull() ?: 0.0
        // Convert to kg for storage if user preference is lbs
        val weightInKg = if (userSettings.value.weightUnit == WeightUnit.LBS) {
            WeightConverter.lbsToKg(weightInDisplayUnit)
        } else {
            weightInDisplayUnit
        }
        updateSetState(exerciseIndex, setIndex) { it.copy(weight = weightInKg) }
    }

    fun onRepsChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val reps = newValue.toIntOrNull() ?: 0
        updateSetState(exerciseIndex, setIndex) { it.copy(reps = reps) }
    }

    // --- NUEVAS FUNCIONES ---
    fun onDistanceChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val dist = newValue.toDoubleOrNull()
        updateSetState(exerciseIndex, setIndex) { it.copy(distance = dist) }
    }

    fun onTimeChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val seconds = newValue.toLongOrNull()
        updateSetState(exerciseIndex, setIndex) { it.copy(timeSeconds = seconds) }
    }
    // ------------------------

    fun onRpeChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val rpe = newValue.toDoubleOrNull()
        updateSetState(exerciseIndex, setIndex) { it.copy(rpe = rpe) }
    }

    fun onRirChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val rir = newValue.toIntOrNull()
        updateSetState(exerciseIndex, setIndex) { it.copy(rir = rir) }
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

    fun finishWorkout(onSuccess: () -> Unit) {
        android.util.Log.d("DEBUG_LIFT", "Click en Finish detectado")

        val session = _activeSession.value

        // Log 2: Ver si tenemos sesión
        if (session == null) {
            android.util.Log.e("DEBUG_LIFT", "¡ERROR CRÍTICO! La sesión es NULL. Abortando.")
            return
        }
        viewModelScope.launch {
            try {
                android.util.Log.d("DEBUG_LIFT", "Intentando guardar sesión...")
                val finalSession = session.copy(
                    durationSeconds = _elapsedTimeSeconds.value
                )
                finishWorkoutUseCase(finalSession)
                android.util.Log.d("DEBUG_LIFT", "¡Guardado exitoso!")
                restTimerManager.stopTimer()
                onSuccess()
            } catch (e: Exception) {
                Log.e("ActiveWorkoutVM", "Error al guardar", e)
            }
        }
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

        // Si no quedan series, eliminar el ejercicio completo
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
            val newExercises = currentSession.exercises.toMutableList()

            exerciseIds.forEach { id ->
                val exerciseDef = getExerciseDetailUseCase(id).firstOrNull()
                if (exerciseDef != null) {
                    val initialSets = (1..3).map {
                        WorkoutSet(
                            id = UUID.randomUUID().toString(),
                            weight = 0.0,
                            reps = 0,
                            completed = false
                        )
                    }
                    newExercises.add(
                        SessionExercise(
                            id = UUID.randomUUID().toString(),
                            exercise = exerciseDef,
                            sets = initialSets
                        )
                    )
                }
            }
            _activeSession.value = currentSession.copy(exercises = newExercises)
        }
    }

    fun toggleAutoTimer() {
        _isAutoTimerEnabled.value = !_isAutoTimerEnabled.value
    }

    fun setEffortMetric(metric: String?) {
        _effortMetric.value = metric
    }
}