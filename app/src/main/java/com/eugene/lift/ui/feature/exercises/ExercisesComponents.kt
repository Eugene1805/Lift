package com.eugene.lift.ui.feature.exercises

import com.eugene.lift.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheetContent(
    selectedBodyParts: Set<BodyPart>,
    selectedCategories: Set<ExerciseCategory>,
    totalExerciseCount: Int,
    onBodyPartToggle: (BodyPart) -> Unit,
    onCategoryToggle: (ExerciseCategory) -> Unit,
    onClearFilters: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.title_filters),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(R.string.filter_exercise_count, totalExerciseCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onClearFilters) {
                Text(stringResource(R.string.btn_clear_filters))
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.subtitle_body_part),
            style = MaterialTheme.typography.titleMedium,
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.subtitle_category),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ExerciseCategory.entries.forEach { cat ->
                FilterChip(
                    selected = cat in selectedCategories,
                    onClick = { onCategoryToggle(cat) },
                    label = { Text(stringResource(cat.labelRes)) },
                    leadingIcon = if (cat in selectedCategories) {
                        { Icon(Icons.Default.Check, null) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_apply))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}