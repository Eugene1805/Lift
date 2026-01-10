package com.eugene.lift.ui.feature.exercises

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.ui.AppDropdown

@Composable
fun AddExerciseRoute(
    onNavigateBack: () -> Unit,
    viewModel: AddExerciseViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val selectedBodyParts by viewModel.selectedBodyParts.collectAsStateWithLifecycle()
    val category by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val measureType by viewModel.selectedMeasureType.collectAsStateWithLifecycle()
    val isEditing = viewModel.isEditing
    AddExerciseScreen(
        name = name,
        selectedBodyParts = selectedBodyParts,
        category = category,
        isEditing = isEditing,
        onNameChange = viewModel::onNameChange,
        onBodyPartToggle = viewModel::toggleBodyPart,
        onCategoryChange = viewModel::onCategoryChange,
        measureType = measureType,
        onMeasureTypeChange = viewModel::onMeasureTypeChange,
        onSaveClick = { viewModel.saveExercise(onSuccess = onNavigateBack) },
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExerciseScreen(
    name: String,
    selectedBodyParts: Set<BodyPart>,
    category: ExerciseCategory,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onBodyPartToggle: (BodyPart) -> Unit,
    onCategoryChange: (ExerciseCategory) -> Unit,
    measureType: MeasureType,
    onMeasureTypeChange: (MeasureType) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) stringResource(R.string.screen_edit_title) else stringResource(R.string.screen_add_title))

                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
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
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.label_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isBlank(),

                supportingText = {
                    Text(
                        text = stringResource(id = R.string.text_field_character_counter, name.length, MAX_EXERCISE_NAME_LENGTH),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.label_body_part),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BodyPart.entries.forEach { part ->
                    FilterChip(
                        selected = part in selectedBodyParts,
                        onClick = { onBodyPartToggle(part) },
                        label = { Text(stringResource(part.labelRes)) },
                        leadingIcon = if (part in selectedBodyParts) {
                            { Icon(Icons.Default.Check, null) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppDropdown(
                label = stringResource(R.string.label_category),
                options = ExerciseCategory.entries,
                selectedOption = category,
                onOptionSelected = onCategoryChange,
                labelProvider = { cat -> stringResource(cat.labelRes) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppDropdown(
                label = stringResource(R.string.label_measure_type),
                options = MeasureType.entries,
                selectedOption = measureType,
                onOptionSelected = onMeasureTypeChange,
                labelProvider = { type -> stringResource(type.labelRes) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save))
                Spacer(modifier = Modifier.padding(4.dp))
                Text(stringResource(R.string.btn_save))
            }
        }
    }
}