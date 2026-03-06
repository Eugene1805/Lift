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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.os.Build
import com.eugene.lift.ui.feature.workout.active.service.ActiveWorkoutService

@Composable
fun ActiveWorkoutScreenRoute(
    onNavigateBack: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onReplaceExercise: (exerciseIndex: Int) -> Unit = {},
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onExerciseClickState by rememberUpdatedState(newValue = onExerciseClick)
    val onAddExerciseState by rememberUpdatedState(newValue = onAddExerciseClick)
    val onReplaceExerciseState by rememberUpdatedState(newValue = onReplaceExercise)

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            if (effect is ActiveWorkoutEffect.NavigateBack) onNavigateBack()
        }
    }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        val intent = Intent(context, ActiveWorkoutService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        onDispose {
            val stopIntent = Intent(context, ActiveWorkoutService::class.java).apply {
                action = ActiveWorkoutService.ACTION_STOP_SERVICE
            }
            context.startService(stopIntent)
        }
    }

    val onEvent: (ActiveWorkoutUiEvent) -> Unit = { event ->
        when (event) {
            ActiveWorkoutUiEvent.AddExerciseClicked -> onAddExerciseState()
            is ActiveWorkoutUiEvent.ExerciseClicked -> onExerciseClickState(event.exerciseId)
            is ActiveWorkoutUiEvent.ReplaceExercise -> onReplaceExerciseState(event.exerciseIndex)
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
            effects = viewModel.effects,
            onEvent = onEvent
        )
    }
}
