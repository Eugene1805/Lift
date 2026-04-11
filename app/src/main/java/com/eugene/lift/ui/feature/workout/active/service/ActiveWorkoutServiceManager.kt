package com.eugene.lift.ui.feature.workout.active.service

import com.eugene.lift.domain.model.WeightUnit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class WorkoutNotificationState(
    val exerciseName: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val isBodyweight: Boolean,
    val weightUnit: WeightUnit
)

sealed interface WorkoutNotificationAction {
    data object CompleteCurrentSet : WorkoutNotificationAction
}

@Singleton
class ActiveWorkoutServiceManager @Inject constructor() {
    private val _notificationState = MutableStateFlow<WorkoutNotificationState?>(null)
    val notificationState: StateFlow<WorkoutNotificationState?> = _notificationState.asStateFlow()

    private val _actions = MutableSharedFlow<WorkoutNotificationAction>(extraBufferCapacity = 1)
    val actions: SharedFlow<WorkoutNotificationAction> = _actions.asSharedFlow()

    fun updateState(state: WorkoutNotificationState?) {
        _notificationState.value = state
    }

    fun dispatchAction(action: WorkoutNotificationAction) {
        _actions.tryEmit(action)
    }
}
