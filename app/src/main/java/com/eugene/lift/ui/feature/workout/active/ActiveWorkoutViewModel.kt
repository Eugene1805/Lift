package com.eugene.lift.ui.feature.workout.active

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.manager.RestTimerManager
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.usecase.GetExerciseDetailUseCase
import com.eugene.lift.domain.usecase.workout.StartEmptyWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.StartWorkoutFromTemplateUseCase
import com.eugene.lift.domain.usecase.workout.FinishWorkoutUseCase
import com.eugene.lift.domain.usecase.workout.GetLastHistoryForExerciseUseCase
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
    private val getLastHistoryForExerciseUseCase: GetLastHistoryForExerciseUseCase
) : ViewModel() {

    private val templateId: String? = savedStateHandle["templateId"]

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)
    val activeSession = _activeSession.asStateFlow()

    val timerState = restTimerManager.timerState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), restTimerManager.timerState.value)

    private val _historyState = MutableStateFlow<Map<String, List<WorkoutSet>>>(emptyMap())
    val historyState = _historyState.asStateFlow()

    private val _effortMetric = MutableStateFlow<String?>("RPE") // "RPE", "RIR" o null
    val effortMetric = _effortMetric.asStateFlow()

    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    val elapsedTimeSeconds = _elapsedTimeSeconds.asStateFlow()

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
            // Calculamos el inicio real
            val startTime = System.currentTimeMillis()

            while (isActive) {
                val now = System.currentTimeMillis()
                // Calculamos diferencia para ser precisos y no depender del lag del delay
                val elapsed = (now - startTime) / 1000
                _elapsedTimeSeconds.value = elapsed
                delay(1000)
            }
        }
    }

    private suspend fun loadHistoryFor(exerciseId: String) {
        val lastSession = getLastHistoryForExerciseUseCase(exerciseId) // <--- Llamada limpia
        if (lastSession != null) {
            val oldExercise = lastSession.exercises.find { it.exercise.id == exerciseId }
            if (oldExercise != null) {
                val currentMap = _historyState.value.toMutableMap()
                currentMap[exerciseId] = oldExercise.sets
                _historyState.value = currentMap
            }
        }
    }

    // --- Lógica de Actualización de Sets (Deep Copy) ---

    /**
     * Función auxiliar genérica para modificar un Set específico.
     * Reconstruye toda la jerarquía (Session -> Exercise -> Sets) para mantener inmutabilidad.
     */
    private fun updateSetState(exerciseIndex: Int, setIndex: Int, update: (WorkoutSet) -> WorkoutSet) {
        val currentSession = _activeSession.value ?: return
        val exercises = currentSession.exercises.toMutableList()
        val targetExercise = exercises[exerciseIndex]
        val sets = targetExercise.sets.toMutableList()

        // Aplicamos la actualización al set específico
        sets[setIndex] = update(sets[setIndex])

        // Reconstruimos el árbol
        exercises[exerciseIndex] = targetExercise.copy(sets = sets)
        _activeSession.value = currentSession.copy(exercises = exercises)
    }

    fun onWeightChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        // Permitimos input vacío temporalmente para UX, pero guardamos 0.0 si falla el parseo
        val weight = newValue.toDoubleOrNull() ?: 0.0
        updateSetState(exerciseIndex, setIndex) { it.copy(weight = weight) }
    }

    fun onRepsChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val reps = newValue.toIntOrNull() ?: 0
        updateSetState(exerciseIndex, setIndex) { it.copy(reps = reps) }
    }

    fun onRpeChange(exerciseIndex: Int, setIndex: Int, newValue: String) {
        val rpe = newValue.toDoubleOrNull()
        updateSetState(exerciseIndex, setIndex) { it.copy(rpe = rpe) }
    }

    fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) {
        var wasCompleted = false
        updateSetState(exerciseIndex, setIndex) {
            wasCompleted = !it.completed // Guardamos el nuevo estado para usarlo abajo
            it.copy(completed = wasCompleted)
        }

        // Si se marcó como completado, iniciamos el timer
        if (wasCompleted) {
            // TODO: Podríamos sacar el tiempo de descanso del ejercicio (restTimerSeconds)
            // Por ahora usamos 90s por defecto
            restTimerManager.startTimer(90)
        } else {
            // Opcional: Si desmarcas, ¿quieres cancelar el timer?
            // restTimerManager.stopTimer()
        }
    }

    // --- Acciones de Sesión ---

    fun finishWorkout(onSuccess: () -> Unit) {
        val session = _activeSession.value ?: return
        viewModelScope.launch {
            try {
                val finalSession = session.copy(
                    durationSeconds = _elapsedTimeSeconds.value
                )
                finishWorkoutUseCase(finalSession)
                restTimerManager.stopTimer()
                onSuccess()
            } catch (e: Exception) {
                // Manejar error (ej: sesión vacía)
            }
        }
    }

    // Métodos del Timer (Delegados)
    fun addTime(seconds: Long) = restTimerManager.addTime(seconds)
    fun stopTimer() = restTimerManager.stopTimer()

    fun addSet(exerciseIndex: Int) {
        val currentSession = _activeSession.value ?: return
        val exercises = currentSession.exercises.toMutableList()
        val targetExercise = exercises[exerciseIndex]

        // Copiamos el peso/reps del set anterior para facilitar la entrada de datos (UX)
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

        exercises[exerciseIndex] = targetExercise.copy(sets = newSets)
        _activeSession.value = currentSession.copy(exercises = exercises)
    }
    fun onAddExercisesToSession(exerciseIds: List<String>) {
        viewModelScope.launch {
            val currentSession = _activeSession.value ?: return@launch
            val newExercises = currentSession.exercises.toMutableList()

            exerciseIds.forEach { id ->
                val exerciseDef = getExerciseDetailUseCase(id).firstOrNull()
                if (exerciseDef != null) {
                    // Creamos el ejercicio de sesión con 3 sets vacíos por defecto
                    val initialSets = (1..3).map {
                        WorkoutSet(
                            id = UUID.randomUUID().toString(),
                            weight = 0.0,
                            reps = 0
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

    fun setEffortMetric(metric: String?) {
        _effortMetric.value = metric
    }
}