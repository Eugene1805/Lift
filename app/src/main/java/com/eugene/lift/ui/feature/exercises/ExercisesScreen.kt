package com.eugene.lift.ui.feature.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.ui.theme.LiftTheme
import java.util.UUID


@Composable
fun ExercisesRoute(
    onAddClick: () -> Unit,
    viewModel: ExercisesViewModel = hiltViewModel()
) {
    val exercisesState by viewModel.exercises.collectAsStateWithLifecycle()

    ExercisesScreen(
        exercises = exercisesState,
        onAddClick = onAddClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    exercises: List<ExerciseEntity>,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.excercises)) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_excercise))
            }
        }
    ) { innerPadding ->
        ExercisesContent(
            exercises = exercises,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun ExercisesContent(
    exercises: List<ExerciseEntity>,
    modifier: Modifier = Modifier
) {
    if (exercises.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.empty_exercises_text),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = exercises,
                key = { it.id }
            ) { exercise ->
                ExerciseItemCard(exercise = exercise)
            }
        }
    }
}

@Composable
fun ExerciseItemCard(exercise: ExerciseEntity, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = {
                Text(
                    text = exercise.name,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Text(
                    text = "${exercise.bodyPart} • ${exercise.category}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ExercisesScreenPreview() {
    LiftTheme {
        val dummyData = listOf(
            ExerciseEntity(UUID.randomUUID().toString(), "Press de Banca", BodyPart.CHEST, ExerciseCategory.CARDIO, MeasureType.REPS_AND_WEIGHT),
            ExerciseEntity(UUID.randomUUID().toString(), "Press de Banca", BodyPart.CHEST, ExerciseCategory.CARDIO, MeasureType.REPS_AND_WEIGHT),
            ExerciseEntity(UUID.randomUUID().toString(), "Press de Banca", BodyPart.CHEST, ExerciseCategory.CARDIO, MeasureType.REPS_AND_WEIGHT)
        )

        ExercisesScreen(
            exercises = dummyData,
            onAddClick = {}
        )
    }
}