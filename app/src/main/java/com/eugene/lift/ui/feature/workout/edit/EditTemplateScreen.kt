package com.eugene.lift.ui.feature.workout.edit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.foundation.ExperimentalFoundationApi
import com.eugene.lift.ui.feature.workout.active.CompactExerciseRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.ui.util.DragDropState
import com.eugene.lift.ui.util.rememberDragDropState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateRoute(
    onNavigateBack: () -> Unit,
    onAddExerciseClick: () -> Unit,
    viewModel: EditTemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaveCompleted) {
        if (uiState.isSaveCompleted) {
            onNavigateBack()
            viewModel.onEvent(EditTemplateUiEvent.NavigationHandled)
        }
    }

    EditTemplateScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onEvent = { event ->
            when (event) {
                EditTemplateUiEvent.AddExerciseClicked -> onAddExerciseClick()
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateScreen(
    uiState: EditTemplateUiState,
    onNavigateBack: () -> Unit,
    onEvent: (EditTemplateUiEvent) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_edit_routine)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(EditTemplateUiEvent.ToggleReorderMode) }) {
                        Icon(Icons.Filled.Menu, contentDescription = null)
                    }
                    Button(
                        onClick = { onEvent(EditTemplateUiEvent.SaveClicked) },
                        enabled = !uiState.isSaving && !uiState.isNameError
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(EditTemplateUiEvent.AddExerciseClicked) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text(stringResource(R.string.btn_add_exercise)) }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            val isNameError = uiState.isNameError

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { onEvent(EditTemplateUiEvent.NameChanged(it)) },
                label = { Text(stringResource(R.string.label_routine_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isNameError,
                supportingText = {
                    Text(
                        text = stringResource(id = R.string.text_field_character_counter, uiState.name.length, MAX_TEMPLATE_NAME_LENGTH),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = if (uiState.name.length > MAX_TEMPLATE_NAME_LENGTH) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.exercises), style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            val lazyListState = rememberLazyListState()
            val dragDropState = rememberDragDropState(lazyListState)

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(uiState.exercises, key = { _, item -> item.id }) { index, item ->
                        val isDragging = dragDropState.draggingItemIndex == index
                        val alpha by animateFloatAsState(if (isDragging) 0.4f else 1f, label = "drag_alpha")

                        @OptIn(ExperimentalFoundationApi::class)
                        Box(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .alpha(if (uiState.reorderState.isReorderMode) alpha else 1f)
                        ) {
                            if (uiState.reorderState.isReorderMode) {
                                val dragHandle: @Composable () -> Unit = {
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = stringResource(R.string.exercise_drag_handle),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(8.dp)
                                            .pointerInput(index) {
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        dragDropState.onDragStart(index, offset.y)
                                                    },
                                                    onDrag = { _, dragAmount ->
                                                        dragDropState.onDragBy(dragAmount.y)
                                                    },
                                                    onDragEnd = {
                                                        dragDropState.onDragEnd { from, to ->
                                                            onEvent(EditTemplateUiEvent.ExercisesReordered(from, to))
                                                        }
                                                    },
                                                    onDragCancel = { dragDropState.onDragCancelled() }
                                                )
                                            }
                                    )
                                }
                                CompactExerciseRow(
                                    exerciseName = item.exercise.name,
                                    dragHandle = dragHandle
                                )
                            } else {
                                TemplateExerciseRowDraggable(
                                    item = item,
                                    isDragging = isDragging,
                                    dragDropState = dragDropState,
                                    itemIndex = index,
                                    onRemove = { onEvent(EditTemplateUiEvent.ExerciseRemoved(item.id)) },
                                    onConfigChange = { s, r -> onEvent(EditTemplateUiEvent.ExerciseConfigChanged(item.id, s, r)) },
                                    onReorder = { from, to -> onEvent(EditTemplateUiEvent.ExercisesReordered(from, to)) }
                                )
                            }
                        }
                    }
                    // Extra space so FAB doesn't cover last item
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }

                // Floating drag shadow
                if (dragDropState.isDragging && dragDropState.draggingItemIndex != null) {
                    val draggedItem = uiState.exercises.getOrNull(dragDropState.draggingItemIndex!!)
                    if (draggedItem != null) {
                        val top = dragDropState.itemTopY(dragDropState.draggingItemIndex!!) +
                                dragDropState.dragOffset
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { translationY = top }
                                .shadow(8.dp)
                                .alpha(0.85f),
                            tonalElevation = 8.dp,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(draggedItem.exercise.name, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateExerciseRowDraggable(
    item: TemplateExercise,
    isDragging: Boolean,
    dragDropState: DragDropState,
    itemIndex: Int,
    onRemove: () -> Unit,
    onConfigChange: (sets: String, reps: String) -> Unit,
    onReorder: (from: Int, to: Int) -> Unit
) {
    val alpha by animateFloatAsState(if (isDragging) 0.4f else 1f, label = "drag_alpha")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = if (isDragging) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            )
            // Editable Sets and Reps
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = item.targetSets.toString(),
                        onValueChange = { onConfigChange(it, item.targetReps) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = "  ×  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = item.targetReps,
                        onValueChange = { onConfigChange(item.targetSets.toString(), it) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.component_delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
