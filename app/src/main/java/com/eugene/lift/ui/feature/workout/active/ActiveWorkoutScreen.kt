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
import androidx.compose.ui.graphics.Color
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
    val history by viewModel.historyState.collectAsStateWithLifecycle() // <--- NUEVO
    val effortMetric by viewModel.effortMetric.collectAsStateWithLifecycle() // <--- NUEVO
    val elapsedTime by viewModel.elapsedTimeSeconds.collectAsStateWithLifecycle() // <--- NUEVO

    if (activeSession != null) {
        ActiveWorkoutScreen(
            sessionName = activeSession!!.name,
            exercises = activeSession!!.exercises,
            history = history, // Pasar historial
            effortMetric = effortMetric, // Pasar config RPE
            timerState = timerState,
            elapsedTime = elapsedTime,
            onWeightChange = viewModel::onWeightChange,
            onRepsChange = viewModel::onRepsChange,
            onRpeChange = viewModel::onRpeChange,
            onSetCompleted = viewModel::toggleSetCompleted,
            onFinishClick = { viewModel.finishWorkout(onSuccess = onNavigateBack) },
            onCancelClick = onNavigateBack, // TODO: Preguntar confirmación antes de salir
            onTimerAdd = viewModel::addTime,
            onTimerStop = viewModel::stopTimer,
            onAddSet = viewModel::addSet,
            onRemoveSet = viewModel::removeSet,
            onAddExercise = onAddExerciseClick,
            onExerciseClick = onExerciseClick,
            onMetricChange = viewModel::setEffortMetric
        )
    } else {
        // Loading state
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
    onWeightChange: (Int, Int, String) -> Unit,
    onRepsChange: (Int, Int, String) -> Unit,
    onRpeChange: (Int, Int, String) -> Unit,
    onSetCompleted: (Int, Int) -> Unit,
    onFinishClick: () -> Unit,
    onCancelClick: () -> Unit,
    onTimerAdd: (Long) -> Unit,
    onTimerStop: () -> Unit,
    onAddSet: (Int) -> Unit,        // Nuevo
    onRemoveSet: (Int, Int) -> Unit, // Nuevo
    onAddExercise: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onMetricChange: (String?) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val formattedTime = remember(elapsedTime) {
        val hours = elapsedTime / 3600
        val minutes = (elapsedTime % 3600) / 60
        val seconds = elapsedTime % 60
        if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
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
                            // CHIP DE TIEMPO
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
                    IconButton(onClick = onCancelClick) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.active_workout_configuration))
                    }

                    // MENÚ DESPLEGABLE
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.active_workout_use_rpe)) },
                            onClick = {
                                onMetricChange("RPE")
                                showMenu = false
                            },
                            trailingIcon = {
                                if (effortMetric == "RPE") Icon(Icons.Default.Check, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.active_workout_use_rir)) },
                            onClick = {
                                onMetricChange("RIR")
                                showMenu = false
                            },
                            trailingIcon = {
                                if (effortMetric == "RIR") Icon(Icons.Default.Check, null)
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.active_workout_hide_metric)) },
                            onClick = {
                                onMetricChange(null)
                                showMenu = false
                            },
                            trailingIcon = {
                                if (effortMetric == null) Icon(Icons.Default.Check, null)
                            }
                        )
                    }
                    Button(onClick = onFinishClick) {
                        Text(stringResource(R.string.active_workout_finish))
                    }
                }
            )
        },
        bottomBar = {
            // TIMER FLOTANTE
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
            itemsIndexed(exercises) { exIndex, exercise ->
                val exerciseHistory = history[exercise.exercise.id] ?: emptyList()
                ActiveExerciseCard(
                    exercise = exercise,
                    exerciseHistory = exerciseHistory, // Pasamos la lista histórica
                    effortMetric = effortMetric,
                    exIndex = exIndex,
                    onWeightChange = onWeightChange,
                    onRepsChange = onRepsChange,
                    onRpeChange = onRpeChange,
                    onSetCompleted = onSetCompleted,
                    onAddSet = { onAddSet(exIndex) },
                    onRemoveSet = { setIndex -> onRemoveSet(exIndex, setIndex)},
                    onExerciseClick = { onExerciseClick(exercise.exercise.id) } // <--- 4. PASAR ID
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

            item { Spacer(modifier = Modifier.height(80.dp)) } // Espacio para el timer
        }
    }
}

@Composable
fun ActiveExerciseCard(
    exercise: SessionExercise,
    exerciseHistory: List<WorkoutSet>, // Historial
    effortMetric: String?,
    exIndex: Int,
    onWeightChange: (Int, Int, String) -> Unit,
    onRepsChange: (Int, Int, String) -> Unit,
    onRpeChange: (Int, Int, String) -> Unit,
    onSetCompleted: (Int, Int) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onExerciseClick: () -> Unit
) {
    val setsInDeleteMode = remember { mutableStateListOf<String>() }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExerciseClick) // <--- 6. CLICKABLE EN TODO EL HEADER
                    .padding(8.dp), // Padding interno para mejor touch target
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
                    contentDescription = stringResource(R.string.active_workout_view_details),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Cabecera Tabla
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(stringResource(R.string.active_workout_set), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                Text(stringResource(R.string.active_workout_kg), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                Text(stringResource(R.string.active_workout_reps), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                if (effortMetric != null) {
                    Text(
                        text = effortMetric, // "RPE" o "RIR"
                        modifier = Modifier.weight(1f).clickable { /* onToggleMetric() si quieres */ },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(48.dp)) // Checkbox area
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
                            onCancel = {
                                setsInDeleteMode.remove(set.id)
                            }
                        )
                    }else {
                        SwipeableSetRowWrapper(
                            itemKey = set.id,
                            onSwipeTriggered = { setsInDeleteMode.add(set.id) }
                        ) {
                            SetRowItem(
                                set = set,
                                setNumber = setIndex + 1,
                                historySet = historySet,
                                effortMetric = effortMetric,
                                onWeightChange = { onWeightChange(exIndex, setIndex, it) },
                                onRepsChange = { onRepsChange(exIndex, setIndex, it) },
                                onRpeChange = { onRpeChange(exIndex, setIndex, it) },
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
    setNumber: Int,
    historySet: WorkoutSet?, // Puede ser null si es un set nuevo
    effortMetric: String?,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
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

        Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
            CompactDecimalInput(
                value = if (set.weight > 0) set.weight.toString() else "",
                onValueChange = onWeightChange,
                modifier = Modifier.fillMaxWidth()
            )
            if (historySet != null) {
                Text(
                    text = "${historySet.weight} ${stringResource(R.string.active_workout_kg_lowercase)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
            CompactNumberInput(
                value = if (set.reps > 0) set.reps.toString() else "",
                onValueChange = onRepsChange,
                modifier = Modifier.fillMaxWidth()
            )
            if (historySet != null) {
                Text(
                    text = "${historySet.reps} ${stringResource(R.string.active_workout_reps_lowercase)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- RPE/RIR OPCIONAL ---
        if (effortMetric != null) {
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                CompactDecimalInput(
                    value = if (effortMetric == "RPE") (set.rpe?.toString() ?: "") else (set.rir?.toString() ?: ""),
                    onValueChange = {
                        // Aquí deberías decidir si guardas en rpe o rir según el modo
                        onRpeChange(it)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (historySet != null) {
                    val histVal = if (effortMetric == "RPE") historySet.rpe else historySet.rir
                    if (histVal != null) {
                        Text(
                            text = "$histVal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        IconButton(onClick = onCompleted, modifier = Modifier.width(48.dp).padding(top = 0.dp)) {
            Icon(
                imageVector = if (set.completed) Icons.Default.Check else Icons.Default.Check, // Mismo icono, cambia color
                contentDescription = stringResource(R.string.active_workout_complete),
                tint = if (set.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
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

// Helpers Inputs
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