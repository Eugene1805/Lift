package com.eugene.lift.ui.feature.workout.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutTemplate

@Composable
fun TemplateDetailRoute(
    onNavigateBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    onEditTemplate: (String) -> Unit,
    onExerciseClick: (String) -> Unit,
    viewModel: TemplateDetailViewModel = hiltViewModel()
) {
    val template by viewModel.template.collectAsStateWithLifecycle()

    TemplateDetailScreen(
        template = template,
        onNavigateBack = onNavigateBack,
        onStartWorkout = { template?.let { onStartWorkout(it.id) } },
        onEditTemplate = { template?.let { onEditTemplate(it.id) } },
        onExerciseClick = onExerciseClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    template: WorkoutTemplate?,
    onNavigateBack: () -> Unit,
    onStartWorkout: () -> Unit,
    onEditTemplate: () -> Unit,
    onExerciseClick: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(template?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onEditTemplate) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.template_detail_edit_template))
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
                onClick = onStartWorkout,
                icon = { Icon(Icons.Default.PlayArrow, null) },
                text = { Text(stringResource(R.string.template_detail_start_routine)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        if (template == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                if (template.notes.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.template_detail_notes),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = template.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.template_detail_exercises_count, template.exercises.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(template.exercises) { item ->
                        TemplateExerciseReadOnlyCard(
                            item = item,
                            onClick = { onExerciseClick(item.exercise.id) }
                        )                    }
                }
            }
        }
    }
}

@Composable
fun TemplateExerciseReadOnlyCard(item: TemplateExercise,onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(item.exercise.category.labelRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Resumen de la configuración (Ej: "3 x 10-12")
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.targetSets} ${stringResource(R.string.template_detail_series)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.targetReps} ${stringResource(R.string.label_reps)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}