package com.eugene.lift.ui.feature.exercises.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eugene.lift.R
import com.eugene.lift.domain.model.Exercise

@Composable
fun ExerciseDetailRoute(
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel()
) {
    val exercise by viewModel.exercise.collectAsStateWithLifecycle()

    ExerciseDetailScreen(
        exercise = exercise,
        onNavigateBack = onNavigateBack,
        onEditClick = { exercise?.let { onEditClick(it.id) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailScreen(
    exercise: Exercise?,
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit
) {

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { exercise?.id?.let(onEditClick) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.exercise_detail_edit))
                    }
                },
                windowInsets = WindowInsets(0,0,0,0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        val state = exercise ?: return@Scaffold // Loading state si es null

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (state.imagePath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(state.imagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = state.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(stringResource(R.string.exercise_detail_no_image), color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {

                // 2. CHIPS DE INFO (Categoría y Músculos)
                Text(
                    stringResource(R.string.exercise_detail_details),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    SuggestionChip(
                        onClick = {},
                        label = { Text(stringResource(state.category.labelRes)) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    // Chips de Músculos
                    state.bodyParts.forEach { part ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(part.labelRes)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


                Text(
                    stringResource(R.string.exercise_detail_instructions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.instructions.ifBlank { stringResource(R.string.exercise_detail_no_instructions) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}