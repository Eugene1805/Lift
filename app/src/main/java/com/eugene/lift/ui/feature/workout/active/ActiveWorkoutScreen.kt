package com.eugene.lift.ui.feature.workout.active

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.TimerState
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.ui.DeleteConfirmationRow
import com.eugene.lift.ui.SwipeableSetRowWrapper
import androidx.compose.ui.res.stringResource
import com.eugene.lift.R
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.util.WeightConverter

@Composable
fun ActiveWorkoutRoute(
    templateId: String? = null,
    onNavigateBack: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onExerciseClick: (String) -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val history by viewModel.historyState.collectAsStateWithLifecycle()
    val effortMetric by viewModel.effortMetric.collectAsStateWithLifecycle()
    val elapsedTime by viewModel.elapsedTimeSeconds.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val isAutoTimerEnabled by viewModel.isAutoTimerEnabled.collectAsStateWithLifecycle()

    if (activeSession != null) {
        ActiveWorkoutScreen(
            sessionName = activeSession!!.name,
            exercises = activeSession!!.exercises,
            history = history,
            effortMetric = effortMetric,
            timerState = timerState,
            elapsedTime = elapsedTime,
            userSettings = userSettings,
            isAutoTimerEnabled = isAutoTimerEnabled,
            hasTemplate = viewModel.hasTemplate(),
            hasWorkoutBeenModified = viewModel.hasWorkoutBeenModified(),
            onToggleAutoTimer = viewModel::toggleAutoTimer,
            onWeightChange = viewModel::onWeightChange,
            onRepsChange = viewModel::onRepsChange,
            onDistanceChange = viewModel::onDistanceChange,
            onTimeChange = viewModel::onTimeChange,
            onRpeChange = viewModel::onRpeChange,
            onRirChange = viewModel::onRirChange,
            onSetCompleted = viewModel::toggleSetCompleted,
            onFinishClick = { updateTemplate ->
                viewModel.finishWorkout(updateTemplate, onSuccess = onNavigateBack)
            },
            onCancelClick = onNavigateBack,
            onTimerAdd = viewModel::addTime,
            onTimerStop = viewModel::stopTimer,
            onAddSet = viewModel::addSet,
            onRemoveSet = viewModel::removeSet,
            onAddExercise = onAddExerciseClick,
            onExerciseClick = onExerciseClick,
            onMetricChange = viewModel::setEffortMetric
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    sessionName: String,
    exercises: List<SessionExercise>,
    history: Map<String, List<WorkoutSet>>,
    effortMetric: String?,
    timerState: TimerState,
    elapsedTime: Long,
    userSettings: UserSettings,
    isAutoTimerEnabled: Boolean,
    hasTemplate: Boolean,
    hasWorkoutBeenModified: Boolean,
    onToggleAutoTimer: () -> Unit,
    onWeightChange: (Int, Int, String) -> Unit,
    onRepsChange: (Int, Int, String) -> Unit,
    onDistanceChange: (Int, Int, String) -> Unit,
    onTimeChange: (Int, Int, String) -> Unit,
    onRpeChange: (Int, Int, String) -> Unit,
    onRirChange: (Int, Int, String) -> Unit,
    onSetCompleted: (Int, Int) -> Unit,
    onFinishClick: (Boolean?) -> Unit,
    onCancelClick: () -> Unit,
    onTimerAdd: (Long) -> Unit,
    onTimerStop: () -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onAddExercise: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onMetricChange: (String?) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showTemplateUpdateDialog by remember { mutableStateOf(false) }
    var showSaveAsTemplateDialog by remember { mutableStateOf(false) }

    // Handle back button press
    BackHandler {
        showExitDialog = true
    }

    // Save As Template Dialog (for Quick Start workouts)
    if (showSaveAsTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showSaveAsTemplateDialog = false },
            title = { Text(stringResource(R.string.save_as_template_title)) },
            text = { Text(stringResource(R.string.save_as_template_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSaveAsTemplateDialog = false
                    onFinishClick(true)
                }) {
                    Text(stringResource(R.string.save_as_template_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveAsTemplateDialog = false
                    onFinishClick(false)
                }) {
                    Text(stringResource(R.string.save_as_template_decline))
                }
            }
        )
    }

    // Template Update Dialog
    if (showTemplateUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateUpdateDialog = false },
            title = { Text(stringResource(R.string.update_template_title)) },
            text = { Text(stringResource(R.string.update_template_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showTemplateUpdateDialog = false
                    onFinishClick(true)
                }) {
                    Text(stringResource(R.string.update_template_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTemplateUpdateDialog = false
                    onFinishClick(false)
                }) {
                    Text(stringResource(R.string.update_template_decline))
                }
            }
        )
    }

    // Exit Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_workout_title)) },
            text = { Text(stringResource(R.string.exit_workout_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onCancelClick()
                }) {
                    Text(stringResource(R.string.exit_workout_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.exit_workout_cancel))
                }
            }
        )
    }

    val formattedTime = remember(elapsedTime) {
        val hours = elapsedTime / 3600
        val minutes = (elapsedTime % 3600) / 60
        val seconds = elapsedTime % 60
        if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.active_workout_training), style = MaterialTheme.typography.labelSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sessionName,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) { Icon(Icons.Default.Close, null) }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.active_workout_configuration))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.active_workout_use_rpe)) },
                            onClick = { onMetricChange("RPE"); showMenu = false },
                            trailingIcon = { if (effortMetric == "RPE") Icon(Icons.Default.Check, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.active_workout_use_rir)) },
                            onClick = { onMetricChange("RIR"); showMenu = false },
                            trailingIcon = { if (effortMetric == "RIR") Icon(Icons.Default.Check, null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.active_workout_hide_metric)) },
                            onClick = { onMetricChange(null); showMenu = false },
                            trailingIcon = { if (effortMetric == null) Icon(Icons.Default.Check, null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.active_workout_auto_timer)) },
                            onClick = { onToggleAutoTimer() },
                            trailingIcon = { Switch(checked = isAutoTimerEnabled, onCheckedChange = null) }
                        )
                    }
                    Button(onClick = {
                        when {
                            // Has template and modified -> ask to update template
                            hasTemplate && hasWorkoutBeenModified -> {
                                showTemplateUpdateDialog = true
                            }
                            // No template (Quick Start) -> ask to save as template
                            !hasTemplate -> {
                                showSaveAsTemplateDialog = true
                            }
                            // Has template but not modified -> just finish
                            else -> {
                                onFinishClick(null)
                            }
                        }
                    }) { Text(stringResource(R.string.active_workout_finish)) }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = timerState.isRunning,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                RestTimerBar(
                    state = timerState,
                    onAdd10s = { onTimerAdd(10) },
                    onStop = onTimerStop
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(exercises, key = { _, item -> item.id }) { exIndex, exercise ->
                val exerciseHistory = history[exercise.exercise.id] ?: emptyList()
                ActiveExerciseCard(
                    exercise = exercise,
                    exerciseHistory = exerciseHistory,
                    effortMetric = effortMetric,
                    exIndex = exIndex,
                    userSettings = userSettings,
                    weightUnitLabel = if (userSettings.weightUnit == WeightUnit.KG) "kg" else "lbs",
                    onWeightChange = onWeightChange,
                    onRepsChange = onRepsChange,
                    onRpeChange = onRpeChange,
                    onRirChange = onRirChange,
                    onDistanceChange = onDistanceChange,
                    onTimeChange = onTimeChange,
                    onSetCompleted = onSetCompleted,
                    onAddSet = { onAddSet(exIndex) },
                    onRemoveSet = { setIndex -> onRemoveSet(exIndex, setIndex) },
                    onExerciseClick = { onExerciseClick(exercise.exercise.id) }
                )
            }

            item {
                OutlinedButton(
                    onClick = onAddExercise,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.active_workout_add_exercise))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ActiveExerciseCard(
    exercise: SessionExercise,
    exerciseHistory: List<WorkoutSet>,
    effortMetric: String?,
    exIndex: Int,
    weightUnitLabel: String,
    userSettings: UserSettings,
    onWeightChange: (Int, Int, String) -> Unit,
    onRepsChange: (Int, Int, String) -> Unit,
    onDistanceChange: (Int, Int, String) -> Unit,
    onTimeChange: (Int, Int, String) -> Unit,
    onRpeChange: (Int, Int, String) -> Unit,
    onRirChange: (Int, Int, String) -> Unit,
    onSetCompleted: (Int, Int) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onExerciseClick: () -> Unit
) {
    val setsInDeleteMode = remember { mutableStateListOf<String>() }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExerciseClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = exercise.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(stringResource(R.string.active_workout_set), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)

                when (exercise.exercise.measureType) {
                    MeasureType.REPS_AND_WEIGHT -> {
                        Text(weightUnitLabel.uppercase(), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(R.string.active_workout_reps), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                    }
                    MeasureType.REPS_ONLY -> {
                        Text(stringResource(R.string.active_workout_reps), modifier = Modifier.weight(2f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                    }
                    MeasureType.DISTANCE_TIME -> {
                        val distUnit = if (userSettings.weightUnit == WeightUnit.KG) "KM" else "MI"
                        Text(distUnit, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(R.string.active_workout_time_label), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                    }
                    MeasureType.TIME -> {
                        Text(stringResource(R.string.active_workout_time_label), modifier = Modifier.weight(2f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (effortMetric != null) {
                    Text(
                        text = effortMetric,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
            }

            exercise.sets.forEachIndexed { setIndex, set ->
                val historySet = exerciseHistory.getOrNull(setIndex)

                key(set.id) {
                    if (set.id in setsInDeleteMode) {
                        DeleteConfirmationRow(
                            setNumber = setIndex + 1,
                            onConfirm = {
                                onRemoveSet(setIndex)
                                setsInDeleteMode.remove(set.id)
                            },
                            onCancel = { setsInDeleteMode.remove(set.id) }
                        )
                    } else {
                        SwipeableSetRowWrapper(
                            itemKey = set.id,
                            onSwipeTriggered = { setsInDeleteMode.add(set.id) }
                        ) {
                            SetRowItem(
                                set = set,
                                measureType = exercise.exercise.measureType,
                                setNumber = setIndex + 1,
                                historySet = historySet,
                                effortMetric = effortMetric,
                                weightUnitLabel = weightUnitLabel,
                                userSettings = userSettings,
                                onWeightChange = { onWeightChange(exIndex, setIndex, it) },
                                onRepsChange = { onRepsChange(exIndex, setIndex, it) },
                                onRpeChange = { onRpeChange(exIndex, setIndex, it) },
                                onRirChange = { onRirChange(exIndex, setIndex, it) },
                                onDistanceChange = { onDistanceChange(exIndex, setIndex, it) },
                                onTimeChange = { onTimeChange(exIndex, setIndex, it) },
                                onCompleted = { onSetCompleted(exIndex, setIndex) }
                            )
                        }
                    }
                }
            }

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.active_workout_add_set))
            }
        }
    }
}

@Composable
fun SetRowItem(
    set: WorkoutSet,
    measureType: MeasureType,
    setNumber: Int,
    historySet: WorkoutSet?,
    effortMetric: String?,
    weightUnitLabel: String,
    userSettings: UserSettings,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onRpeChange: (String) -> Unit,
    onRirChange: (String) -> Unit,
    onCompleted: () -> Unit
) {
    val rowBackground = if (set.completed)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surface

    // Weight is stored in display units during workout, no conversion needed
    val displayWeight = set.weight

    val historyDisplayWeight = historySet?.let {
        // History is stored in kg, so convert to display units
        if (userSettings.weightUnit == WeightUnit.LBS) {
            WeightConverter.kgToLbs(it.weight)
        } else {
            it.weight
        }
    }

    // Format weight: show as integer if whole number, otherwise show decimal
    fun formatWeight(weight: Double): String {
        return if (weight == weight.toLong().toDouble()) {
            weight.toLong().toString()
        } else {
            weight.toString()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = setNumber.toString(), modifier = Modifier.width(32.dp).padding(top = 10.dp), textAlign = TextAlign.Center)

        when(measureType) {
            MeasureType.REPS_AND_WEIGHT -> {
                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    CompactDecimalInput(
                        value = if (displayWeight > 0) formatWeight(displayWeight) else "",
                        onValueChange = onWeightChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (historyDisplayWeight != null) HistoryText("${formatWeight(historyDisplayWeight)} $weightUnitLabel")
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    CompactNumberInput(
                        value = if (set.reps > 0) set.reps.toString() else "",
                        onValueChange = onRepsChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (historySet != null) HistoryText("${historySet.reps} reps")
                }
            }
            MeasureType.REPS_ONLY -> {
                Column(modifier = Modifier.weight(2f).padding(horizontal = 4.dp)) {
                    CompactNumberInput(
                        value = if (set.reps > 0) set.reps.toString() else "",
                        onValueChange = onRepsChange,
                        modifier = Modifier.fillMaxWidth(0.6f).align(Alignment.CenterHorizontally)
                    )
                    if (historySet != null) HistoryText("${historySet.reps} reps", Modifier.align(Alignment.CenterHorizontally))
                }
            }
            MeasureType.DISTANCE_TIME -> {
                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    val distanceValue = set.distance ?: 0.0
                    CompactDecimalInput(
                        value = if (distanceValue > 0) formatWeight(distanceValue) else "",
                        onValueChange = onDistanceChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (historySet != null) {
                        val histDist = historySet.distance
                        HistoryText("${if (histDist != null) formatWeight(histDist) else "-"} km")
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    CompactNumberInput(
                        value = if ((set.timeSeconds ?: 0) > 0) set.timeSeconds.toString() else "",
                        onValueChange = onTimeChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (historySet != null) HistoryText("${historySet.timeSeconds ?: "-"} s")
                }
            }
            MeasureType.TIME -> {
                Column(modifier = Modifier.weight(2f).padding(horizontal = 4.dp)) {
                    CompactNumberInput(
                        value = if ((set.timeSeconds ?: 0) > 0) set.timeSeconds.toString() else "",
                        onValueChange = onTimeChange,
                        modifier = Modifier.fillMaxWidth(0.6f).align(Alignment.CenterHorizontally)
                    )
                    if (historySet != null) HistoryText("${historySet.timeSeconds ?: "-"} ${stringResource(R.string.active_workout_time_s)}", Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }

        if (effortMetric != null) {
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                if (effortMetric == "RPE") {
                    CompactDecimalInput(
                        value = set.rpe?.toString() ?: "",
                        onValueChange = onRpeChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CompactNumberInput(
                        value = set.rir?.toString() ?: "",
                        onValueChange = onRirChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (historySet != null) {
                    val histVal = if (effortMetric == "RPE") historySet.rpe else historySet.rir
                    if (histVal != null) HistoryText("$histVal")
                }
            }
        }

        IconButton(onClick = onCompleted, modifier = Modifier.width(48.dp).padding(top = 0.dp)) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (set.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun HistoryText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}
@Composable
fun RestTimerBar(
    state: TimerState,
    onAdd10s: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, null)
                Spacer(modifier = Modifier.width(16.dp))
                val min = state.timeRemainingSeconds / 60
                val sec = state.timeRemainingSeconds % 60
                Text(
                    text = "%02d:%02d".format(min, sec),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                TextButton(onClick = onAdd10s) { Text(stringResource(R.string.active_workout_add_10s)) }
                IconButton(onClick = onStop) { Icon(Icons.Default.Close, null) }
            }
        }
    }
}

@Composable
fun CompactNumberInput(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    CompactTextInput(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        keyboardType = KeyboardType.Number,
        filterInput = { input -> input.filter { it.isDigit() } }
    )
}

@Composable
fun CompactDecimalInput(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    CompactTextInput(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        keyboardType = KeyboardType.Decimal,
        filterInput = { input ->
            // Allow digits and at most one decimal point
            buildString {
                var decimalAdded = false
                for (char in input) {
                    when {
                        char.isDigit() -> append(char)
                        char == '.' && !decimalAdded -> {
                            append(char)
                            decimalAdded = true
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CompactTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    keyboardType: KeyboardType,
    filterInput: (String) -> String
) {
    val shape = MaterialTheme.shapes.small
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    // Use TextFieldValue to control selection
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var isFocused by remember { mutableStateOf(false) }

    // Sync when external value changes (from ViewModel)
    // But ONLY when not focused - this prevents round-trip conversion issues during typing
    LaunchedEffect(value, isFocused) {
        if (!isFocused && textFieldValue.text != value) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            val filtered = filterInput(newTextFieldValue.text)
            val newSelection = if (filtered.length < newTextFieldValue.text.length) {
                TextRange(minOf(newTextFieldValue.selection.start, filtered.length))
            } else {
                newTextFieldValue.selection
            }
            textFieldValue = TextFieldValue(text = filtered, selection = newSelection)
            onValueChange(filtered)
        },
        modifier = modifier
            .background(containerColor, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), shape)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .onFocusChanged { focusState ->
                val wasFocused = isFocused
                isFocused = focusState.isFocused

                if (focusState.isFocused && textFieldValue.text.isNotEmpty()) {
                    // Select all text when gaining focus
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                } else if (wasFocused && !focusState.isFocused) {
                    // When losing focus, sync with the external value
                    if (textFieldValue.text != value) {
                        textFieldValue = TextFieldValue(
                            text = value,
                            selection = TextRange(value.length)
                        )
                    }
                }
            },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
    )
}