package com.eugene.lift.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eugene.lift.ui.feature.exercises.AddExerciseRoute
import com.eugene.lift.ui.feature.exercises.ExercisesRoute
import com.eugene.lift.ui.feature.exercises.detail.ExerciseDetailRoute
import com.eugene.lift.ui.feature.history.HistoryRoute
import com.eugene.lift.ui.feature.profile.ProfileRoute
import com.eugene.lift.ui.feature.settings.SettingsRoute
import com.eugene.lift.ui.feature.workout.WorkoutRoute
import com.eugene.lift.ui.feature.workout.active.ActiveWorkoutRoute
import com.eugene.lift.ui.feature.workout.active.ActiveWorkoutViewModel
import com.eugene.lift.ui.feature.workout.detail.TemplateDetailRoute
import com.eugene.lift.ui.feature.workout.edit.EditTemplateScreen
import com.eugene.lift.ui.feature.workout.edit.EditTemplateViewModel

/**
 * Main navigation graph for the Lift app.
 *
 * @param navController Navigation controller for handling navigation
 * @param modifier Modifier to apply to the NavHost
 */
@Composable
fun LiftNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = com.eugene.lift.ui.navigation.WorkoutRoute,
        modifier = modifier
    ) {
        // Main bottom navigation destinations
        profileScreen()
        historyScreen(navController)
        workoutScreen(navController)
        exerciseListScreen(navController)
        settingsScreen()

        // Detail and edit screens
        templateDetailScreen(navController)
        templateEditScreen(navController)
        exerciseDetailScreen(navController)
        exerciseAddScreen(navController)
        exercisePickerScreen(navController)
        activeWorkoutScreen(navController)
    }
}

// Profile Screen
private fun NavGraphBuilder.profileScreen() {
    composable<com.eugene.lift.ui.navigation.ProfileRoute> {
        ProfileRoute()
    }
}

// History Screen
private fun NavGraphBuilder.historyScreen(navController: NavHostController) {
    composable<com.eugene.lift.ui.navigation.HistoryRoute> {
        HistoryRoute(
            onSessionClick = { sessionId -> /* TODO: Navigate to session detail */ }
        )
    }
}

// Workout Screen
private fun NavGraphBuilder.workoutScreen(navController: NavHostController) {
    composable<com.eugene.lift.ui.navigation.WorkoutRoute> {
        WorkoutRoute(
            onNavigateToEdit = { templateId ->
                navController.navigate(TemplateEditRoute(templateId))
            },
            onTemplateClick = { templateId ->
                navController.navigate(TemplateDetailRoute(templateId))
            },
            onStartEmptyClick = {
                navController.navigate(ActiveWorkoutRoute(templateId = null))
            }
        )
    }
}

// Template Detail Screen
private fun NavGraphBuilder.templateDetailScreen(navController: NavHostController) {
    composable<TemplateDetailRoute> {
        TemplateDetailRoute(
            onNavigateBack = { navController.popBackStack() },
            onStartWorkout = { templateId ->
                navController.navigate(ActiveWorkoutRoute(templateId = templateId)) {
                    popUpTo(com.eugene.lift.ui.navigation.WorkoutRoute)
                }
            },
            onEditTemplate = { templateId ->
                navController.navigate(TemplateEditRoute(templateId))
            },
            onExerciseClick = { exerciseId ->
                navController.navigate(ExerciseDetailRoute(exerciseId))
            }
        )
    }
}

// Template Edit Screen
private fun NavGraphBuilder.templateEditScreen(navController: NavHostController) {
    composable<TemplateEditRoute> { backStackEntry ->
        val viewModel: EditTemplateViewModel = hiltViewModel()
        val savedStateHandle = backStackEntry.savedStateHandle
        val selectedExerciseIds = savedStateHandle.get<List<String>>("selected_exercise_ids")

        LaunchedEffect(selectedExerciseIds) {
            selectedExerciseIds?.let { ids ->
                viewModel.onExercisesSelected(ids)
                savedStateHandle.remove<List<String>>("selected_exercise_ids")
            }
        }

        EditTemplateScreen(
            onNavigateBack = { navController.popBackStack() },
            onAddExerciseClick = {
                navController.navigate(ExercisePickerRoute)
            }
        )
    }
}

// Exercise Picker Screen
private fun NavGraphBuilder.exercisePickerScreen(navController: NavHostController) {
    composable<ExercisePickerRoute> {
        ExercisesRoute(
            isSelectionMode = true,
            onAddClick = {
                navController.navigate(ExerciseAddRoute())
            },
            onExerciseClick = { exerciseId ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("selected_exercise_id", exerciseId)
                navController.popBackStack()
            },
            onExercisesSelected = { ids ->
                val previousEntry = navController.previousBackStackEntry
                previousEntry?.savedStateHandle?.set("selected_exercise_ids", ids)
                previousEntry?.savedStateHandle?.set("selected_exercise_ids_active", ids)
                navController.popBackStack()
            }
        )
    }
}

// Settings Screen
private fun NavGraphBuilder.settingsScreen() {
    composable<com.eugene.lift.ui.navigation.SettingsRoute> {
        SettingsRoute()
    }
}

// Exercise Add/Edit Screen
private fun NavGraphBuilder.exerciseAddScreen(navController: NavHostController) {
    composable<ExerciseAddRoute> {
        AddExerciseRoute(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

// Exercise List Screen
private fun NavGraphBuilder.exerciseListScreen(navController: NavHostController) {
    composable<ExerciseListRoute> {
        ExercisesRoute(
            onAddClick = { navController.navigate(ExerciseAddRoute()) },
            onExerciseClick = { exerciseId ->
                navController.navigate(ExerciseDetailRoute(exerciseId))
            }
        )
    }
}

// Exercise Detail Screen
private fun NavGraphBuilder.exerciseDetailScreen(navController: NavHostController) {
    composable<ExerciseDetailRoute> {
        ExerciseDetailRoute(
            onNavigateBack = { navController.popBackStack() },
            onEditClick = { exerciseId ->
                navController.navigate(ExerciseAddRoute(exerciseId = exerciseId))
            }
        )
    }
}

// Active Workout Screen
private fun NavGraphBuilder.activeWorkoutScreen(navController: NavHostController) {
    composable<ActiveWorkoutRoute> { backStackEntry ->
        val viewModel: ActiveWorkoutViewModel = hiltViewModel()
        val savedStateHandle = backStackEntry.savedStateHandle
        val selectedExerciseIds = savedStateHandle.get<List<String>>("selected_exercise_ids_active")

        LaunchedEffect(selectedExerciseIds) {
            selectedExerciseIds?.let { ids ->
                viewModel.onAddExercisesToSession(ids)
                savedStateHandle.remove<List<String>>("selected_exercise_ids_active")
            }
        }

        ActiveWorkoutRoute(
            onNavigateBack = { navController.popBackStack() },
            onAddExerciseClick = {
                navController.navigate(ExercisePickerRoute)
            },
            onExerciseClick = { exerciseId ->
                navController.navigate(ExerciseDetailRoute(exerciseId))
            }
        )
    }
}
