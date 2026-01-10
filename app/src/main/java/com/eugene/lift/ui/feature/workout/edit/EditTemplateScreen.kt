package com.eugene.lift.ui.feature.workout.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.domain.model.TemplateExercise
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import com.eugene.lift.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateScreen(
    onNavigateBack: () -> Unit,
    onAddExerciseClick: () -> Unit,
    viewModel: EditTemplateViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_edit_routine)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveTemplate(onSuccess = onNavigateBack) }) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.action_save))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExerciseClick,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.btn_add_exercise)) }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            // Nombre de la Rutina
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.label_routine_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.exercises), style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de ejercicios agregados
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f) // Ocupa el espacio restante
            ) {
                items(exercises, key = { it.id }) { item ->
                    TemplateExerciseRow(
                        item = item,
                        onRemove = { viewModel.removeExercise(item) },
                        onConfigChange = { s, r -> viewModel.updateExerciseConfig(item, s, r) }
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
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.exercise.name, style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
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