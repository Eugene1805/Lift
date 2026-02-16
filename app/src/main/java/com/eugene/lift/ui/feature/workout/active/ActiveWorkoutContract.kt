package com.eugene.lift.ui.feature.workout.active

import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.TimerState
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WorkoutSet

data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val sessionName: String = "",
    val exercises: List<SessionExercise> = emptyList(),
    val history: Map<String, List<WorkoutSet>> = emptyMap(),
    val effortMetric: String? = null,
    val timerState: TimerState = TimerState(),
    val elapsedTime: Long = 0L,
    val userSettings: UserSettings = UserSettings(),
    val isAutoTimerEnabled: Boolean = true,
    val hasTemplate: Boolean = false,
    val hasWorkoutBeenModified: Boolean = false,
    val sessionNote: String? = null
)

sealed interface ActiveWorkoutUiEvent {
    data class WeightChanged(val exerciseIndex: Int, val setIndex: Int, val value: String) : ActiveWorkoutUiEvent
    data class RepsChanged(val exerciseIndex: Int, val setIndex: Int, val value: String) : ActiveWorkoutUiEvent
    data class DistanceChanged(val exerciseIndex: Int, val setIndex: Int, val value: String) : ActiveWorkoutUiEvent
    data class TimeChanged(val exerciseIndex: Int, val setIndex: Int, val value: String) : ActiveWorkoutUiEvent
    data class RpeChanged(val exerciseIndex: Int, val setIndex: Int, val value: String) : ActiveWorkoutUiEvent
    data class RirChanged(val exerciseIndex: Int, val setIndex: Int, val value: String) : ActiveWorkoutUiEvent
    data class SetCompleted(val exerciseIndex: Int, val setIndex: Int) : ActiveWorkoutUiEvent
    data class AddSet(val exerciseIndex: Int) : ActiveWorkoutUiEvent
    data class RemoveSet(val exerciseIndex: Int, val setIndex: Int) : ActiveWorkoutUiEvent
    data class MetricChanged(val metric: String?) : ActiveWorkoutUiEvent
    data class TimerAdded(val seconds: Long) : ActiveWorkoutUiEvent
    data object TimerStopped : ActiveWorkoutUiEvent
    data object ToggleAutoTimer : ActiveWorkoutUiEvent
    data class FinishClicked(val updateTemplate: Boolean?) : ActiveWorkoutUiEvent
    data object CancelClicked : ActiveWorkoutUiEvent
    data object AddExerciseClicked : ActiveWorkoutUiEvent
    data class ExerciseClicked(val exerciseId: String) : ActiveWorkoutUiEvent
    data class SessionNoteChanged(val value: String) : ActiveWorkoutUiEvent
    data class ExerciseNoteChanged(val exerciseIndex: Int, val value: String) : ActiveWorkoutUiEvent
}

sealed interface ActiveWorkoutEffect {
    data object NavigateBack : ActiveWorkoutEffect
}

