package com.eugene.lift.ui.feature.workout.active

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            onToggleAutoTimer = viewModel::toggleAutoTimer,
            onWeightChange = viewModel::onWeightChange,
            onRepsChange = viewModel::onRepsChange,
            onDistanceChange = viewModel::onDistanceChange,
            onTimeChange = viewModel::onTimeChange,
            onRpeChange = viewModel::onRpeChange,
            onSetCompleted = viewModel::toggleSetCompleted,
            onFinishClick = { viewModel.finishWorkout(onSuccess = onNavigateBack) },
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
    onToggleAutoTimer: () -> Unit,
    onWeightChange: (Int, Int, String) -> Unit,
    onRepsChange: (Int, Int, String) -> Unit,
    onDistanceChange: (Int, Int, String) -> Unit, // <--- RECIBIR
    onTimeChange: (Int, Int, String) -> Unit,     // <--- RECIBIR
    onRpeChange: (Int, Int, String) -> Unit,
    onSetCompleted: (Int, Int) -> Unit,
    onFinishClick: () -> Unit,
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

    val formattedTime = remember(elapsedTime) {
        val hours = elapsedTime / 3600
        val minutes = (elapsedTime % 3600) / 60
        val seconds = elapsedTime % 60
        if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }

    Scaffold(
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
                    IconButton(onClick = onCancelClick) { Icon(Icons.Default.Close, null) }
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
                            text = { Text("Auto-timer al finalizar serie") },
                            onClick = { onToggleAutoTimer() },
                            trailingIcon = { Switch(checked = isAutoTimerEnabled, onCheckedChange = null) }
                        )
                    }
                    Button(onClick = onFinishClick) { Text(stringResource(R.string.active_workout_finish)) }
                }
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
            contentPadding = PaddingValues(16.dp),
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
    onDistanceChange: (Int, Int, String) -> Unit, // <--- RECIBIR
    onTimeChange: (Int, Int, String) -> Unit,     // <--- RECIBIR
    onRpeChange: (Int, Int, String) -> Unit,
    onSetCompleted: (Int, Int) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onExerciseClick: () -> Unit
) {
    val setsInDeleteMode = remember { mutableStateListOf<String>() }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header del Ejercicio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExerciseClick)
                    .padding(8.dp),
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

            // Cabeceras Dinámicas (Según MeasureType)
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
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
                        Text("TIEMPO", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                    }
                    MeasureType.TIME -> {
                        Text("TIEMPO", modifier = Modifier.weight(2f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
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

                // Key fundamental para evitar bugs visuales
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
                                onWeightChange = { onWeightChange(exIndex, setIndex, it) },
                                onRepsChange = { onRepsChange(exIndex, setIndex, it) },
                                onRpeChange = { onRpeChange(exIndex, setIndex, it) },
                                // --- AHORA SÍ PASAMOS LOS CALLBACKS ---
                                onDistanceChange = { onDistanceChange(exIndex, setIndex, it) },
                                onTimeChange = { onTimeChange(exIndex, setIndex, it) },
                                // --------------------------------------
                                onCompleted = { onSetCompleted(exIndex, setIndex) }
                            )
                        }
                    }
                }
            }

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth()
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
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onRpeChange: (String) -> Unit,
    onCompleted: () -> Unit
) {
    val rowBackground = if (set.completed)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = setNumber.toString(), modifier = Modifier.width(32.dp).padding(top = 10.dp), textAlign = TextAlign.Center)

        // LÓGICA POLIMÓRFICA DE INPUTS
        when(measureType) {
            MeasureType.REPS_AND_WEIGHT -> {
                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    CompactDecimalInput(
                        value = if (set.weight > 0) set.weight.toString() else "",
                        onValueChange = onWeightChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (historySet != null) HistoryText("${historySet.weight} $weightUnitLabel")
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
                    CompactDecimalInput(
                        value = if ((set.distance ?: 0.0) > 0) set.distance.toString() else "",
                        onValueChange = onDistanceChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (historySet != null) HistoryText("${historySet.distance ?: "-"} km")
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
                    if (historySet != null) HistoryText("${historySet.timeSeconds ?: "-"} s", Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }

        // RPE/RIR (Común)
        if (effortMetric != null) {
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                CompactDecimalInput(
                    value = if (effortMetric == "RPE") (set.rpe?.toString() ?: "") else (set.rir?.toString() ?: ""),
                    onValueChange = onRpeChange,
                    modifier = Modifier.fillMaxWidth()
                )
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
                // Formato MM:SS
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
    BasicTextFieldStyle(value, onValueChange, modifier, KeyboardType.Number)
}

@Composable
fun CompactDecimalInput(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    BasicTextFieldStyle(value, onValueChange, modifier, KeyboardType.Decimal)
}

@Composable
fun BasicTextFieldStyle(value: String, onValueChange: (String) -> Unit, modifier: Modifier, keyboardType: KeyboardType) {
    val shape = MaterialTheme.shapes.small
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(containerColor, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), shape)
            .padding(horizontal = 8.dp, vertical = 8.dp), // <--- AQUÍ SI PUEDES PONER PADDING A TU GUSTO
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
    )
}