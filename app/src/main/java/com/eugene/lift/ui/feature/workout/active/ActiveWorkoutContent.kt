package com.eugene.lift.ui.feature.workout.active

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eugene.lift.R
import com.eugene.lift.domain.model.MeasureType

@Composable
fun WorkoutContent(
    uiState: ActiveWorkoutUiState,
    weightUnitLabel: String,
    onEvent: (ActiveWorkoutUiEvent) -> Unit,
    onShowExerciseSnackbar: (name: String, weight: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(text = stringResource(R.string.active_workout_notes_label), style = MaterialTheme.typography.labelMedium)
                CompactTextInput(
                    value = uiState.sessionNote ?: "",
                    onValueChange = { onEvent(ActiveWorkoutUiEvent.SessionNoteChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Text,
                    filterInput = { it }
                )
            }
        }

        itemsIndexed(uiState.exercises, key = { _, item -> item.id }) { exIndex, exercise ->
            val exerciseHistory = uiState.history[exercise.exercise.id] ?: emptyList()
            val callbacks = ExerciseCallbacks(
                onWeightChange = { setIdx, value -> onEvent(ActiveWorkoutUiEvent.WeightChanged(exIndex, setIdx, value)) },
                onRepsChange = { setIdx, value -> onEvent(ActiveWorkoutUiEvent.RepsChanged(exIndex, setIdx, value)) },
                onDistanceChange = { setIdx, value -> onEvent(ActiveWorkoutUiEvent.DistanceChanged(exIndex, setIdx, value)) },
                onTimeChange = { setIdx, value -> onEvent(ActiveWorkoutUiEvent.TimeChanged(exIndex, setIdx, value)) },
                onRpeChange = { setIdx, value -> onEvent(ActiveWorkoutUiEvent.RpeChanged(exIndex, setIdx, value)) },
                onRirChange = { setIdx, value -> onEvent(ActiveWorkoutUiEvent.RirChanged(exIndex, setIdx, value)) },
                onSetCompleted = { setIdx ->
                    onEvent(ActiveWorkoutUiEvent.SetCompleted(exIndex, setIdx))
                    val set = exercise.sets.getOrNull(setIdx)
                    if (set?.completed == true && exercise.exercise.measureType == MeasureType.REPS_AND_WEIGHT) {
                        val weightText = "${set.weight} $weightUnitLabel"
                        onShowExerciseSnackbar(exercise.exercise.name, weightText)
                    }
                },
                onAddSet = { onEvent(ActiveWorkoutUiEvent.AddSet(exIndex)) },
                onRemoveSet = { setIdx -> onEvent(ActiveWorkoutUiEvent.RemoveSet(exIndex, setIdx)) },
                onExerciseClick = { onEvent(ActiveWorkoutUiEvent.ExerciseClicked(exercise.exercise.id)) },
                onExerciseNoteChange = { onEvent(ActiveWorkoutUiEvent.ExerciseNoteChanged(exIndex, it)) }
            )
            ActiveExerciseCard(
                exercise = exercise,
                exerciseHistory = exerciseHistory,
                effortMetric = uiState.effortMetric,
                weightUnitLabel = weightUnitLabel,
                userSettings = uiState.userSettings,
                callbacks = callbacks
            )
        }

        item {
            OutlinedButton(
                onClick = { onEvent(ActiveWorkoutUiEvent.AddExerciseClicked) },
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
