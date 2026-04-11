package com.eugene.lift.ui.feature.workout.active

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eugene.lift.R
import com.eugene.lift.ui.util.rememberDragDropState

@Composable
fun WorkoutContent(
    uiState: ActiveWorkoutUiState,
    weightUnitLabel: String,
    onEvent: (ActiveWorkoutUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val dragDropState = rememberDragDropState(lazyListState)

    Box(modifier = modifier.fillMaxSize()) {
        WorkoutContentList(
            uiState = uiState,
            weightUnitLabel = weightUnitLabel,
            onEvent = onEvent,
            lazyListState = lazyListState,
            dragDropState = dragDropState
        )

        DraggedExerciseOverlay(uiState = uiState, dragDropState = dragDropState)
    }
}

@Composable
private fun WorkoutContentList(
    uiState: ActiveWorkoutUiState,
    weightUnitLabel: String,
    onEvent: (ActiveWorkoutUiEvent) -> Unit,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    dragDropState: com.eugene.lift.ui.util.DragDropState
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SessionNoteSection(uiState = uiState, onEvent = onEvent) }
        workoutExerciseItems(uiState = uiState, weightUnitLabel = weightUnitLabel, onEvent = onEvent, dragDropState = dragDropState)
        item { AddExerciseButton(onEvent = onEvent) }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SessionNoteSection(
    uiState: ActiveWorkoutUiState,
    onEvent: (ActiveWorkoutUiEvent) -> Unit
) {
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

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.workoutExerciseItems(
    uiState: ActiveWorkoutUiState,
    weightUnitLabel: String,
    onEvent: (ActiveWorkoutUiEvent) -> Unit,
    dragDropState: com.eugene.lift.ui.util.DragDropState
) {
    itemsIndexed(uiState.exercises, key = { _, item -> item.id }) { exIndex, exercise ->
        WorkoutExerciseItem(
            uiState = uiState,
            exercise = exercise,
            exIndex = exIndex,
            weightUnitLabel = weightUnitLabel,
            dragDropState = dragDropState,
            onEvent = onEvent
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutExerciseItem(
    uiState: ActiveWorkoutUiState,
    exercise: com.eugene.lift.domain.model.SessionExercise,
    exIndex: Int,
    weightUnitLabel: String,
    dragDropState: com.eugene.lift.ui.util.DragDropState,
    onEvent: (ActiveWorkoutUiEvent) -> Unit
) {
    val exerciseHistory = uiState.history[exercise.exercise.id] ?: emptyList()
    val isDragging = dragDropState.draggingItemIndex == exIndex
    val alpha by animateFloatAsState(if (isDragging) 0.4f else 1f, label = "drag_alpha")
    val callbacks = buildExerciseCallbacks(exIndex = exIndex, exerciseId = exercise.exercise.id, onEvent = onEvent)

    Box(
        modifier = Modifier
            .animateContentSize()
            .padding(horizontal = 8.dp)
            .alpha(alpha)
    ) {
        WorkoutExerciseCardBody(
            uiState = uiState,
            exercise = exercise,
            exerciseHistory = exerciseHistory,
            weightUnitLabel = weightUnitLabel,
            callbacks = callbacks,
            exIndex = exIndex,
            dragDropState = dragDropState,
            onEvent = onEvent
        )
    }
}

@Composable
private fun WorkoutExerciseCardBody(
    uiState: ActiveWorkoutUiState,
    exercise: com.eugene.lift.domain.model.SessionExercise,
    exerciseHistory: List<com.eugene.lift.domain.model.WorkoutSet>,
    weightUnitLabel: String,
    callbacks: ExerciseCallbacks,
    exIndex: Int,
    dragDropState: com.eugene.lift.ui.util.DragDropState,
    onEvent: (ActiveWorkoutUiEvent) -> Unit
) {
    val dragHandle: @Composable () -> Unit = {
        ExerciseDragHandle(exIndex = exIndex, dragDropState = dragDropState, onEvent = onEvent)
    }

    if (uiState.reorderState.isReorderMode) {
        CompactExerciseRow(exerciseName = exercise.exercise.name, dragHandle = dragHandle)
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

private fun buildExerciseCallbacks(
    exIndex: Int,
    exerciseId: String,
    onEvent: (ActiveWorkoutUiEvent) -> Unit
): ExerciseCallbacks {
    val dispatcher = ExerciseEventDispatcher(
        exIndex = exIndex,
        exerciseId = exerciseId,
        onEvent = onEvent
    )
    return ExerciseCallbacks(
        onWeightChange = dispatcher::onWeightChange,
        onRepsChange = dispatcher::onRepsChange,
        onDistanceChange = dispatcher::onDistanceChange,
        onTimeChange = dispatcher::onTimeChange,
        onRpeChange = dispatcher::onRpeChange,
        onRirChange = dispatcher::onRirChange,
        onSetCompleted = dispatcher::onSetCompleted,
        onAddSet = dispatcher::onAddSet,
        onRemoveSet = dispatcher::onRemoveSet,
        onExerciseClick = dispatcher::onExerciseClick,
        onExerciseNoteChange = dispatcher::onExerciseNoteChange,
        onDeleteExercise = dispatcher::onDeleteExercise,
        onReplaceExercise = dispatcher::onReplaceExercise
    )
}

private class ExerciseEventDispatcher(
    private val exIndex: Int,
    private val exerciseId: String,
    private val onEvent: (ActiveWorkoutUiEvent) -> Unit
) {
    fun onWeightChange(setIdx: Int, value: String) = onEvent(ActiveWorkoutUiEvent.WeightChanged(exIndex, setIdx, value))
    fun onRepsChange(setIdx: Int, value: String) = onEvent(ActiveWorkoutUiEvent.RepsChanged(exIndex, setIdx, value))
    fun onDistanceChange(setIdx: Int, value: String) = onEvent(ActiveWorkoutUiEvent.DistanceChanged(exIndex, setIdx, value))
    fun onTimeChange(setIdx: Int, value: String) = onEvent(ActiveWorkoutUiEvent.TimeChanged(exIndex, setIdx, value))
    fun onRpeChange(setIdx: Int, value: String) = onEvent(ActiveWorkoutUiEvent.RpeChanged(exIndex, setIdx, value))
    fun onRirChange(setIdx: Int, value: String) = onEvent(ActiveWorkoutUiEvent.RirChanged(exIndex, setIdx, value))
    fun onSetCompleted(setIdx: Int) = onEvent(ActiveWorkoutUiEvent.SetCompleted(exIndex, setIdx))
    fun onAddSet() = onEvent(ActiveWorkoutUiEvent.AddSet(exIndex))
    fun onRemoveSet(setIdx: Int) = onEvent(ActiveWorkoutUiEvent.RemoveSet(exIndex, setIdx))
    fun onExerciseClick() = onEvent(ActiveWorkoutUiEvent.ExerciseClicked(exerciseId))
    fun onExerciseNoteChange(note: String) = onEvent(ActiveWorkoutUiEvent.ExerciseNoteChanged(exIndex, note))
    fun onDeleteExercise() = onEvent(ActiveWorkoutUiEvent.RemoveExercise(exIndex))
    fun onReplaceExercise() = onEvent(ActiveWorkoutUiEvent.ReplaceExercise(exIndex))
}

@Composable
private fun ExerciseDragHandle(
    exIndex: Int,
    dragDropState: com.eugene.lift.ui.util.DragDropState,
    onEvent: (ActiveWorkoutUiEvent) -> Unit
) {
    val gestureHandler = remember(exIndex, dragDropState, onEvent) {
        ExerciseReorderGestureHandler(exIndex, dragDropState, onEvent)
    }

    Icon(
        imageVector = Icons.Default.DragHandle,
        contentDescription = stringResource(R.string.exercise_drag_handle),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(20.dp)
            .pointerInput(exIndex) {
                detectDragGestures(
                    onDragStart = gestureHandler::onDragStart,
                    onDrag = gestureHandler::onDrag,
                    onDragEnd = gestureHandler::onDragEnd,
                    onDragCancel = gestureHandler::onDragCancel
                )
            }
    )
}

private class ExerciseReorderGestureHandler(
    private val exIndex: Int,
    private val dragDropState: com.eugene.lift.ui.util.DragDropState,
    private val onEvent: (ActiveWorkoutUiEvent) -> Unit
) {
    fun onDragStart(offset: androidx.compose.ui.geometry.Offset) {
        dragDropState.onDragStart(exIndex, offset.y)
    }

    fun onDrag(change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: androidx.compose.ui.geometry.Offset) {
        change.consume()
        dragDropState.onDragBy(dragAmount.y)
    }

    fun onDragEnd() {
        dragDropState.onDragEnd { from, to ->
            onEvent(ActiveWorkoutUiEvent.ExercisesReordered(from, to))
        }
    }

    fun onDragCancel() {
        dragDropState.onDragCancelled()
    }
}

@Composable
private fun AddExerciseButton(onEvent: (ActiveWorkoutUiEvent) -> Unit) {
    OutlinedButton(
        onClick = { onEvent(ActiveWorkoutUiEvent.AddExerciseClicked) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.active_workout_add_exercise))
    }
}

@Composable
private fun DraggedExerciseOverlay(
    uiState: ActiveWorkoutUiState,
    dragDropState: com.eugene.lift.ui.util.DragDropState
) {
    if (!dragDropState.isDragging || dragDropState.draggingItemIndex == null) return

    val draggedIndex = dragDropState.draggingItemIndex ?: return
    val draggedExercise = uiState.exercises.getOrNull(draggedIndex) ?: return
    val top = dragDropState.itemTopY(draggedIndex) + dragDropState.dragOffset

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
