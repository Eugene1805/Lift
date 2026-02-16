package com.eugene.lift.ui.feature.workout.active

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eugene.lift.R
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.ui.components.DeleteConfirmationRow
import com.eugene.lift.ui.components.SwipeableSetRowWrapper

@Composable
fun ActiveExerciseCard(
    exercise: SessionExercise,
    exerciseHistory: List<WorkoutSet>,
    effortMetric: String?,
    weightUnitLabel: String,
    userSettings: UserSettings,
    callbacks: ExerciseCallbacks
) {
    val setsInDeleteMode = remember { mutableStateListOf<String>() }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ExerciseHeader(title = exercise.exercise.name, onClick = callbacks.onExerciseClick)
            SetHeaders(measureType = exercise.exercise.measureType, weightUnitLabel = weightUnitLabel, effortMetric = effortMetric, userSettings = userSettings)

            exercise.sets.forEachIndexed { setIndex, set ->
                val historySet = exerciseHistory.getOrNull(setIndex)

                key(set.id) {
                    if (set.id in setsInDeleteMode) {
                        DeleteConfirmationRow(
                            setNumber = setIndex + 1,
                            onConfirm = {
                                callbacks.onRemoveSet(setIndex)
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
                                callbacks = callbacks.asSetRowCallbacks(setIndex),
                                context = SetRowContext(
                                    measureType = exercise.exercise.measureType,
                                    setNumber = setIndex + 1,
                                    historySet = historySet,
                                    effortMetric = effortMetric,
                                    weightUnitLabel = weightUnitLabel,
                                    userSettings = userSettings
                                )
                            )
                        }
                    }
                }
            }

            ExerciseFooter(onAddSet = callbacks.onAddSet, note = exercise.note ?: "", onNoteChange = callbacks.onExerciseNoteChange)
        }
    }
}

@Composable
private fun ExerciseHeader(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
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
}

@Composable
private fun SetHeaders(measureType: MeasureType, weightUnitLabel: String, effortMetric: String?, userSettings: UserSettings) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(stringResource(R.string.active_workout_set), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)

        when (measureType) {
            MeasureType.REPS_AND_WEIGHT -> {
                Text(weightUnitLabel.uppercase(), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                Text(stringResource(R.string.active_workout_reps), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
            }
            MeasureType.REPS_ONLY -> {
                Text(stringResource(R.string.active_workout_reps), modifier = Modifier.weight(2f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
            }
            MeasureType.DISTANCE_TIME -> {
                val distUnit = if (userSettings.distanceUnit == DistanceUnit.KM) "KM" else "MI"
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
}

@Composable
private fun ExerciseFooter(onAddSet: () -> Unit, note: String, onNoteChange: (String) -> Unit) {
    TextButton(
        onClick = onAddSet,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.active_workout_add_set))
    }

    Text(stringResource(R.string.active_workout_exercise_notes_label), modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelMedium)
    CompactTextInput(
        value = note,
        onValueChange = onNoteChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
        filterInput = { it }
    )
}

private fun ExerciseCallbacks.asSetRowCallbacks(setIndex: Int): SetRowCallbacks =
    SetRowCallbacks(
        onWeightChange = { onWeightChange(setIndex, it) },
        onRepsChange = { onRepsChange(setIndex, it) },
        onDistanceChange = { onDistanceChange(setIndex, it) },
        onTimeChange = { onTimeChange(setIndex, it) },
        onRpeChange = { onRpeChange(setIndex, it) },
        onRirChange = { onRirChange(setIndex, it) },
        onCompleted = { onSetCompleted(setIndex) }
    )
