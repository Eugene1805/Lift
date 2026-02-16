package com.eugene.lift.ui.feature.workout.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.domain.model.TemplateExercise
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import com.eugene.lift.R

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
                        Icon(androidx.compose.material.icons.Icons.Filled.Close, contentDescription = null)
                    }
                },
                actions = {
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
                icon = { Icon(androidx.compose.material.icons.Icons.Filled.Add, null) },
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

            // Lista de ejercicios agregados
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f) // Ocupa el espacio restante
            ) {
                items(uiState.exercises, key = { it.id }) { item ->
                    TemplateExerciseRow(
                        item = item,
                        onRemove = { onEvent(EditTemplateUiEvent.ExerciseRemoved(item.id)) },
                        onConfigChange = { s, r -> onEvent(EditTemplateUiEvent.ExerciseConfigChanged(item.id, s, r)) }
                    )
                }
                // Espacio extra para que el FAB no tape el último item
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun TemplateExerciseRow(
    item: TemplateExercise,
    onRemove: () -> Unit,
    onConfigChange: (sets: String, reps: String) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        androidx.compose.material.icons.Icons.Filled.Close,
                        contentDescription = stringResource(R.string.component_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.targetSets.toString(),
                    onValueChange = { onConfigChange(it, item.targetReps) },
                    label = { Text(stringResource(R.string.label_sets)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = item.targetReps,
                    onValueChange = { onConfigChange(item.targetSets.toString(), it) },
                    label = { Text(stringResource(R.string.label_reps)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}