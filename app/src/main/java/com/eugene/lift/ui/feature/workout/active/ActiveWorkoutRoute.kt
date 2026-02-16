package com.eugene.lift.ui.feature.workout.active

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ActiveWorkoutScreenRoute(
    onNavigateBack: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onExerciseClick: (String) -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onExerciseClickState by rememberUpdatedState(newValue = onExerciseClick)
    val onAddExerciseState by rememberUpdatedState(newValue = onAddExerciseClick)

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            if (effect is ActiveWorkoutEffect.NavigateBack) onNavigateBack()
        }
    }

    val onEvent: (ActiveWorkoutUiEvent) -> Unit = { event ->
        when (event) {
            ActiveWorkoutUiEvent.AddExerciseClicked -> onAddExerciseState()
            is ActiveWorkoutUiEvent.ExerciseClicked -> onExerciseClickState(event.exerciseId)
            else -> viewModel.onEvent(event)
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        ActiveWorkoutScreen(
            uiState = uiState,
            onEvent = onEvent
        )
    }
}
