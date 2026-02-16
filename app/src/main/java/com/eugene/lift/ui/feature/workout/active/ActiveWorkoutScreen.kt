package com.eugene.lift.ui.feature.workout.active

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.ui.components.ExerciseSnackbar
import kotlinx.coroutines.delay

@Composable
fun ActiveWorkoutScreen(
    uiState: ActiveWorkoutUiState,
    onEvent: (ActiveWorkoutUiEvent) -> Unit
) {
    val screenState = rememberWorkoutScreenState()

    val showExerciseSnackbar: (String, String) -> Unit = rememberUpdatedState<(String, String) -> Unit> { name, weight ->
        screenState.showSnackbar(name, weight)
    }.value

    LaunchedEffect(screenState.isSnackbarVisible) {
        if (screenState.isSnackbarVisible) {
            delay(3000)
            screenState.hideSnackbar()
        }
    }

    BackHandler { screenState.requestExit() }

    ActiveSaveAsTemplateDialog(
        show = screenState.showSaveAsTemplateDialog,
        onDismiss = screenState::hideSaveAsTemplate,
        onSave = {
            screenState.hideSaveAsTemplate()
            onEvent(ActiveWorkoutUiEvent.FinishClicked(true))
        },
        onSkip = {
            screenState.hideSaveAsTemplate()
            onEvent(ActiveWorkoutUiEvent.FinishClicked(false))
        }
    )

    ActiveUpdateTemplateDialog(
        show = screenState.showTemplateUpdateDialog,
        onDismiss = screenState::hideTemplateUpdate,
        onUpdate = {
            screenState.hideTemplateUpdate()
            onEvent(ActiveWorkoutUiEvent.FinishClicked(true))
        },
        onKeep = {
            screenState.hideTemplateUpdate()
            onEvent(ActiveWorkoutUiEvent.FinishClicked(false))
        }
    )

    ActiveExitWorkoutDialog(
        show = screenState.showExitDialog,
        onDismiss = screenState::hideExit,
        onConfirmExit = {
            screenState.hideExit()
            onEvent(ActiveWorkoutUiEvent.CancelClicked)
        }
    )

    val formattedTime = remember(uiState.elapsedTime) {
        val hours = uiState.elapsedTime / 3600
        val minutes = (uiState.elapsedTime % 3600) / 60
        val seconds = uiState.elapsedTime % 60
        if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }
    val weightUnitLabel = if (uiState.userSettings.weightUnit == WeightUnit.KG) "kg" else "lbs"

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            WorkoutTopBar(
                uiState = uiState,
                formattedTime = formattedTime,
                onExit = { screenState.requestExit() },
                onMetricChange = { onEvent(ActiveWorkoutUiEvent.MetricChanged(it)) },
                onToggleAutoTimer = { onEvent(ActiveWorkoutUiEvent.ToggleAutoTimer) },
                onFinish = { updateTemplate ->
                    when {
                        uiState.hasTemplate && uiState.hasWorkoutBeenModified -> screenState.showTemplateUpdate()
                        !uiState.hasTemplate -> screenState.showSaveAsTemplate()
                        else -> onEvent(ActiveWorkoutUiEvent.FinishClicked(updateTemplate))
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = uiState.timerState.isRunning) {
                RestTimerBar(
                    state = uiState.timerState,
                    onAdd10s = { onEvent(ActiveWorkoutUiEvent.TimerAdded(10)) },
                    onStop = { onEvent(ActiveWorkoutUiEvent.TimerStopped) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            WorkoutContent(
                uiState = uiState,
                weightUnitLabel = weightUnitLabel,
                onEvent = onEvent,
                onShowExerciseSnackbar = showExerciseSnackbar,
                modifier = Modifier.padding(innerPadding)
            )

            ExerciseSnackbar(
                exerciseName = screenState.snackbarExerciseName,
                weight = screenState.snackbarWeight,
                isVisible = screenState.isSnackbarVisible,
                onDismiss = { screenState.hideSnackbar() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

class WorkoutScreenState {
    var showExitDialog by mutableStateOf(false)
    var showTemplateUpdateDialog by mutableStateOf(false)
    var showSaveAsTemplateDialog by mutableStateOf(false)

    var isSnackbarVisible by mutableStateOf(false)
    var snackbarExerciseName by mutableStateOf("")
    var snackbarWeight by mutableStateOf("")

    fun requestExit() { showExitDialog = true }
    fun hideExit() { showExitDialog = false }

    fun showTemplateUpdate() { showTemplateUpdateDialog = true }
    fun hideTemplateUpdate() { showTemplateUpdateDialog = false }

    fun showSaveAsTemplate() { showSaveAsTemplateDialog = true }
    fun hideSaveAsTemplate() { showSaveAsTemplateDialog = false }

    fun showSnackbar(exerciseName: String, weight: String) {
        snackbarExerciseName = exerciseName
        snackbarWeight = weight
        isSnackbarVisible = true
    }

    fun hideSnackbar() { isSnackbarVisible = false }
}

@Composable
fun rememberWorkoutScreenState(): WorkoutScreenState = remember { WorkoutScreenState() }
