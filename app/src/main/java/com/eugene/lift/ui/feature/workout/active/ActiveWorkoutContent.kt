package com.eugene.lift.ui.feature.workout.active

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eugene.lift.R
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.ui.util.rememberDragDropState

@Composable
fun WorkoutContent(
    uiState: ActiveWorkoutUiState,
    weightUnitLabel: String,
    onEvent: (ActiveWorkoutUiEvent) -> Unit,
    onShowExerciseSnackbar: (name: String, weight: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val dragDropState = rememberDragDropState(lazyListState)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
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
                val isDragging = dragDropState.draggingItemIndex == exIndex
                val alpha by animateFloatAsState(if (isDragging) 0.4f else 1f, label = "drag_alpha")

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

                @OptIn(ExperimentalFoundationApi::class)
                Box(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 8.dp)
                        .alpha(alpha)
                ) {
                    val dragHandle: @Composable () -> Unit = {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = stringResource(R.string.exercise_drag_handle),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(20.dp)
                                    .pointerInput(exIndex) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                dragDropState.onDragStart(exIndex, offset.y)
                                            },
                                            onDrag = { _, dragAmount ->
                                                dragDropState.onDragBy(dragAmount.y)
                                            },
                                            onDragEnd = {
                                                dragDropState.onDragEnd { from, to ->
                                                    onEvent(ActiveWorkoutUiEvent.ExercisesReordered(from, to))
                                                }
                                            },
                                            onDragCancel = { dragDropState.onDragCancelled() }
                                        )
                                    }
                            )
                        }
                    
                    if (uiState.reorderState.isReorderMode) {
                        CompactExerciseRow(
                            exerciseName = exercise.exercise.name,
                            dragHandle = dragHandle
                        )
                    } else {
                        ActiveExerciseCard(
                            exercise = exercise,
                            exerciseHistory = exerciseHistory,
                            effortMetric = uiState.effortMetric,
                            weightUnitLabel = weightUnitLabel,
                            userSettings = uiState.userSettings,
                            callbacks = callbacks
                        )
                    }
                }
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

        // Floating drag shadow overlay
        if (dragDropState.isDragging && dragDropState.draggingItemIndex != null) {
            val draggedExercise = uiState.exercises.getOrNull(dragDropState.draggingItemIndex!!)
            if (draggedExercise != null) {
                val top = dragDropState.itemTopY(dragDropState.draggingItemIndex!!) +
                        dragDropState.dragOffset
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .graphicsLayer { translationY = top }
                        .shadow(8.dp)
                        .alpha(0.85f),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = draggedExercise.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompactExerciseRow(
    exerciseName: String,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            dragHandle()
        }
    }
}
