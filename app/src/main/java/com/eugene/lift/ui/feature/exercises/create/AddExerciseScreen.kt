package com.eugene.lift.ui.feature.exercises.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eugene.lift.R
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.ui.components.AppDropdown

@Composable
fun AddExerciseRoute(
    onNavigateBack: () -> Unit,
    viewModel: AddExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaveCompleted) {
        if (uiState.isSaveCompleted) {
            onNavigateBack()
            viewModel.onEvent(AddExerciseUiEvent.NavigationHandled)
        }
    }

    AddExerciseScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                AddExerciseUiEvent.BackClicked -> onNavigateBack()
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("LongParameterList")
@Composable
fun AddExerciseScreen(
    uiState: AddExerciseUiState,
    onEvent: (AddExerciseUiEvent) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditing) stringResource(R.string.screen_edit_title) else stringResource(R.string.screen_add_title))

                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AddExerciseUiEvent.BackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { onEvent(AddExerciseUiEvent.SaveClicked) },
                        enabled = uiState.isSaveEnabled && !uiState.isSaving
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { onEvent(AddExerciseUiEvent.NameChanged(it)) },
                label = { Text(stringResource(R.string.label_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.isNameError,

                supportingText = {
                    Text(
                        text = stringResource(id = R.string.text_field_character_counter, uiState.name.length,
                            MAX_EXERCISE_NAME_LENGTH
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = if (uiState.name.length > MAX_EXERCISE_NAME_LENGTH) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.label_body_part),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BodyPart.entries.forEach { part ->
                    FilterChip(
                        selected = part in uiState.selectedBodyParts,
                        onClick = { onEvent(AddExerciseUiEvent.BodyPartToggled(part)) },
                        label = { Text(stringResource(part.labelRes)) },
                        leadingIcon = if (part in uiState.selectedBodyParts) {
                            { Icon(Icons.Default.Check, null) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppDropdown(
                label = stringResource(R.string.label_category),
                options = ExerciseCategory.entries,
                selectedOption = uiState.category,
                onOptionSelected = { onEvent(AddExerciseUiEvent.CategoryChanged(it)) },
                labelProvider = { cat -> stringResource(cat.labelRes) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppDropdown(
                label = stringResource(R.string.label_measure_type),
                options = MeasureType.entries,
                selectedOption = uiState.measureType,
                onOptionSelected = { onEvent(AddExerciseUiEvent.MeasureTypeChanged(it)) },
                labelProvider = { type -> stringResource(type.labelRes) }
            )
        }
    }
}