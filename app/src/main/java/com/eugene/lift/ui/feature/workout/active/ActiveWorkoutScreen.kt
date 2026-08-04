package com.eugene.lift.ui.feature.workout.active

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.model.WorkoutCompletionSummary
import com.eugene.lift.ui.components.ExerciseSnackbar
import com.eugene.lift.ui.util.toMessage
import com.eugene.lift.ui.util.WeightFormatters
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ActiveWorkoutScreen(
    uiState: ActiveWorkoutUiState,
    effects: Flow<ActiveWorkoutEffect>,
    onEvent: (ActiveWorkoutUiEvent) -> Unit
) {
    val screenState = rememberWorkoutScreenState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val kgLabel = stringResource(com.eugene.lift.R.string.unit_kg)
    val lbsLabel = stringResource(com.eugene.lift.R.string.unit_lbs)

    CollectActiveWorkoutEffects(
        effects = effects,
        screenState = screenState,
        snackbarHostState = snackbarHostState,
        context = context,
        kgLabel = kgLabel,
        lbsLabel = lbsLabel
    )
    AutoDismissExerciseSnackbarEffect(screenState = screenState)

    BackHandler {
        if (screenState.completionSummary != null) {
            screenState.hideCompletionSummary()
            onEvent(ActiveWorkoutUiEvent.CompletionSummaryDismissed)
        } else {
            screenState.requestExit()
        }
    }

    ActiveWorkoutDialogsHost(screenState = screenState, onEvent = onEvent)

    val formattedTime = rememberFormattedElapsedTime(uiState.elapsedTime)
    val weightUnitLabel = rememberWeightUnitLabel(uiState.userSettings.weightUnit)

    ActiveWorkoutLayout(
        uiState = uiState,
        onEvent = onEvent,
        snackbarHostState = snackbarHostState,
        formattedTime = formattedTime,
        weightUnitLabel = weightUnitLabel,
        screenState = screenState
    )
}

@Composable
private fun ActiveWorkoutLayout(
    uiState: ActiveWorkoutUiState,
    onEvent: (ActiveWorkoutUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    formattedTime: String,
    weightUnitLabel: String,
    screenState: WorkoutScreenState
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ActiveWorkoutTopBarSection(
                uiState = uiState,
                formattedTime = formattedTime,
                onEvent = onEvent,
                screenState = screenState,
                onRequestExit = screenState::requestExit,
                onShowTemplateUpdate = screenState::showTemplateUpdate,
                onShowSaveAsTemplate = screenState::showSaveAsTemplate
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
        ActiveWorkoutScaffoldContent(
            uiState = uiState,
            weightUnitLabel = weightUnitLabel,
            onEvent = onEvent,
            screenState = screenState,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun CollectActiveWorkoutEffects(
    effects: Flow<ActiveWorkoutEffect>,
    screenState: WorkoutScreenState,
    snackbarHostState: SnackbarHostState,
    context: Context,
    kgLabel: String,
    lbsLabel: String
) {
    LaunchedEffect(effects) {
        effects.collectLatest { effect ->
            when (effect) {
                is ActiveWorkoutEffect.ShowExerciseSnackbar -> {
                    val unitLabel = if (effect.weightUnit == WeightUnit.KG) kgLabel else lbsLabel
                    val weightText = "${WeightFormatters.formatWeight(effect.weight, effect.weightUnit)} $unitLabel"
                    screenState.showSnackbar(effect.name, weightText, effect.isPr)
                }
                is ActiveWorkoutEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.error.toMessage(context))
                is ActiveWorkoutEffect.ShowSnackbarMessage -> snackbarHostState.showSnackbar(context.getString(effect.messageResId))
                is ActiveWorkoutEffect.WorkoutCompleted -> screenState.showCompletionSummary(effect.summary)
                ActiveWorkoutEffect.NavigateBack -> Unit
            }
        }
    }
}

@Composable
private fun AutoDismissExerciseSnackbarEffect(screenState: WorkoutScreenState) {
    LaunchedEffect(screenState.isSnackbarVisible) {
        if (screenState.isSnackbarVisible) {
            delay(3000)
            screenState.hideSnackbar()
        }
    }
}

@Composable
private fun ActiveWorkoutDialogsHost(
    screenState: WorkoutScreenState,
    onEvent: (ActiveWorkoutUiEvent) -> Unit
) {
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

    ActiveFinishBlockedDialog(
        show = screenState.showFinishBlockedDialog,
        titleResId = screenState.finishBlockedTitleResId,
        messageResId = screenState.finishBlockedMessageResId,
        onDismiss = screenState::hideFinishBlocked,
        onKeepEditing = screenState::hideFinishBlocked,
        onSaveDraft = {
            screenState.hideFinishBlocked()
            onEvent(ActiveWorkoutUiEvent.SaveDraftAndExitClicked)
        }
    )

    screenState.completionSummary?.let { summary ->
        WorkoutCompletionDialog(
            summary = summary,
            onDone = {
                screenState.hideCompletionSummary()
                onEvent(ActiveWorkoutUiEvent.CompletionSummaryDismissed)
            }
        )
    }
}

@Composable
private fun rememberFormattedElapsedTime(elapsedTime: Long): String {
    return remember(elapsedTime) {
        val hours = elapsedTime / 3600
        val minutes = (elapsedTime % 3600) / 60
        val seconds = elapsedTime % 60
        if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun rememberWeightUnitLabel(weightUnit: WeightUnit): String {
    val kgLabel = stringResource(com.eugene.lift.R.string.unit_kg)
    val lbsLabel = stringResource(com.eugene.lift.R.string.unit_lbs)
    return if (weightUnit == WeightUnit.KG) kgLabel else lbsLabel
}

@Composable
private fun ActiveWorkoutTopBarSection(
    uiState: ActiveWorkoutUiState,
    formattedTime: String,
    onEvent: (ActiveWorkoutUiEvent) -> Unit,
    screenState: WorkoutScreenState,
    onRequestExit: () -> Unit,
    onShowTemplateUpdate: () -> Unit,
    onShowSaveAsTemplate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WorkoutTopBar(
            uiState = uiState,
            formattedTime = formattedTime,
            onExit = onRequestExit,
            onMetricChange = { onEvent(ActiveWorkoutUiEvent.MetricChanged(it)) },
            onToggleAutoTimer = { onEvent(ActiveWorkoutUiEvent.ToggleAutoTimer) },
            onToggleReorderMode = { onEvent(ActiveWorkoutUiEvent.ToggleReorderMode) },
            showSessionNote = screenState.showSessionNote,
            onToggleSessionNote = screenState::toggleSessionNote,
            onFinish = { updateTemplate ->
                handleFinishAction(
                    uiState = uiState,
                    updateTemplate = updateTemplate,
                    screenState = screenState,
                    onEvent = onEvent,
                    onShowTemplateUpdate = onShowTemplateUpdate,
                    onShowSaveAsTemplate = onShowSaveAsTemplate
                )
            }
        )

        WorkoutProgressBar(exercises = uiState.exercises)
    }
}

private fun handleFinishAction(
    uiState: ActiveWorkoutUiState,
    updateTemplate: Boolean?,
    screenState: WorkoutScreenState,
    onEvent: (ActiveWorkoutUiEvent) -> Unit,
    onShowTemplateUpdate: () -> Unit,
    onShowSaveAsTemplate: () -> Unit
) {
    val allSets = uiState.exercises.flatMap { it.sets }
    val hasCompletedSets = allSets.any { it.completed }
    val hasStartedButIncompleteSets = allSets.any { set ->
        !set.completed && (
            set.weight > 0.0 ||
                set.reps > 0 ||
                (set.timeSeconds ?: 0L) > 0L ||
                (set.distance ?: 0.0) > 0.0 ||
                set.rpe != null ||
                set.rir != null
            )
    }

    when {
        !hasCompletedSets && hasStartedButIncompleteSets -> screenState.showFinishBlocked(
            titleResId = com.eugene.lift.R.string.workout_finish_incomplete_title,
            messageResId = com.eugene.lift.R.string.workout_finish_incomplete_message
        )
        !hasCompletedSets -> screenState.showFinishBlocked(
            titleResId = com.eugene.lift.R.string.workout_finish_empty_title,
            messageResId = com.eugene.lift.R.string.workout_finish_empty_message
        )
        hasStartedButIncompleteSets -> screenState.showFinishBlocked(
            titleResId = com.eugene.lift.R.string.workout_finish_incomplete_title,
            messageResId = com.eugene.lift.R.string.workout_finish_incomplete_message
        )
        uiState.hasTemplate && uiState.hasWorkoutBeenModified -> onShowTemplateUpdate()
        !uiState.hasTemplate -> onShowSaveAsTemplate()
        else -> onEvent(ActiveWorkoutUiEvent.FinishClicked(updateTemplate))
    }
}

@Composable
private fun WorkoutProgressBar(exercises: List<com.eugene.lift.domain.model.SessionExercise>) {
    val totalSets = remember(exercises) { exercises.sumOf { it.sets.size } }
    val completedSets = remember(exercises) { exercises.sumOf { it.sets.count { set -> set.completed } } }
    val progress = if (totalSets > 0) completedSets.toFloat() / totalSets.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "workout_progress")

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ActiveWorkoutScaffoldContent(
    uiState: ActiveWorkoutUiState,
    weightUnitLabel: String,
    onEvent: (ActiveWorkoutUiEvent) -> Unit,
    screenState: WorkoutScreenState,
    innerPadding: PaddingValues
) {
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        WorkoutContent(
            uiState = uiState,
            weightUnitLabel = weightUnitLabel,
            onEvent = onEvent,
            showSessionNote = screenState.showSessionNote,
            modifier = Modifier
        )

        ExerciseSnackbar(
            exerciseName = screenState.snackbarExerciseName,
            weight = screenState.snackbarWeight,
            isVisible = screenState.isSnackbarVisible,
            onDismiss = { screenState.hideSnackbar() },
            isPr = screenState.snackbarIsPr,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

class WorkoutScreenState {
    var showExitDialog by mutableStateOf(false)
    var showTemplateUpdateDialog by mutableStateOf(false)
    var showSaveAsTemplateDialog by mutableStateOf(false)
    var showFinishBlockedDialog by mutableStateOf(false)
    var finishBlockedTitleResId by mutableStateOf(com.eugene.lift.R.string.workout_finish_empty_title)
    var finishBlockedMessageResId by mutableStateOf(com.eugene.lift.R.string.workout_finish_empty_message)
    var showSessionNote by mutableStateOf(false)
        private set
    var completionSummary by mutableStateOf<WorkoutCompletionSummary?>(null)
        private set

    var isSnackbarVisible by mutableStateOf(false)
    var snackbarExerciseName by mutableStateOf("")
    var snackbarWeight by mutableStateOf("")
    var snackbarIsPr by mutableStateOf(false)

    fun requestExit() { showExitDialog = true }
    fun hideExit() { showExitDialog = false }

    fun showTemplateUpdate() { showTemplateUpdateDialog = true }
    fun hideTemplateUpdate() { showTemplateUpdateDialog = false }

    fun showSaveAsTemplate() { showSaveAsTemplateDialog = true }
    fun hideSaveAsTemplate() { showSaveAsTemplateDialog = false }

    fun showFinishBlocked(titleResId: Int, messageResId: Int) {
        finishBlockedTitleResId = titleResId
        finishBlockedMessageResId = messageResId
        showFinishBlockedDialog = true
    }

    fun hideFinishBlocked() { showFinishBlockedDialog = false }

    fun toggleSessionNote() { showSessionNote = !showSessionNote }

    fun showCompletionSummary(summary: WorkoutCompletionSummary) {
        completionSummary = summary
    }

    fun hideCompletionSummary() { completionSummary = null }

    fun showSnackbar(exerciseName: String, weight: String, isPr: Boolean = false) {
        snackbarExerciseName = exerciseName
        snackbarWeight = weight
        snackbarIsPr = isPr
        isSnackbarVisible = true
    }

    fun hideSnackbar() { isSnackbarVisible = false }
}

@Composable
fun rememberWorkoutScreenState(): WorkoutScreenState = remember { WorkoutScreenState() }
