package com.eugene.lift.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.ui.feature.exercises.create.AddExerciseRoute
import com.eugene.lift.ui.feature.exercises.ExercisesRoute
import com.eugene.lift.ui.feature.exercises.detail.ExerciseDetailRoute
import com.eugene.lift.ui.feature.history.HistoryRoute
import com.eugene.lift.ui.feature.history.calendar.HistoryCalendarRoute as HistoryCalendarRouteScreen
import com.eugene.lift.ui.feature.history.detail.SessionDetailRoute as SessionDetailRouteScreen
import com.eugene.lift.ui.feature.onboarding.OnboardingRoute as OnboardingRouteScreen
import com.eugene.lift.ui.feature.profile.ProfileRoute
import com.eugene.lift.ui.feature.profile.edit.EditProfileRoute as EditProfileRouteScreen
import com.eugene.lift.ui.feature.settings.SettingsRoute
import com.eugene.lift.ui.feature.workout.WorkoutRoute
import com.eugene.lift.ui.feature.workout.active.ActiveWorkoutViewModel
import com.eugene.lift.ui.feature.workout.active.ActiveWorkoutScreenRoute
import com.eugene.lift.ui.feature.workout.detail.TemplateDetailRoute
import com.eugene.lift.ui.feature.workout.edit.EditTemplateRoute
import com.eugene.lift.ui.feature.workout.edit.EditTemplateUiEvent
import com.eugene.lift.ui.feature.workout.edit.EditTemplateViewModel
import java.time.LocalDate

/**
 * Main navigation graph for the Lift app.
 *
 * @param navController Navigation controller for handling navigation
 * @param settingsRepository Used to gate the onboarding start destination
 * @param modifier Modifier to apply to the NavHost
 */
@Composable
fun LiftNavGraph(
    navController: NavHostController,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val onboardingComplete by settingsRepository.isOnboardingComplete()
        .collectAsStateWithLifecycle(initialValue = null)

    val startDestination = when (onboardingComplete) {
        true -> com.eugene.lift.ui.navigation.WorkoutRoute as Any
        false -> com.eugene.lift.ui.navigation.OnboardingRoute as Any
        null -> return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        onboardingScreen(navController)

        profileScreen(navController)
        historyScreen(navController)
        workoutScreen(navController)
        exerciseListScreen(navController)
        settingsScreen()

        templateDetailScreen(navController)
        templateEditScreen(navController)
        exerciseDetailScreen(navController)
        exerciseAddScreen(navController)
        exercisePickerScreen(navController)
        activeWorkoutScreen(navController)
        sessionDetailScreen(navController)
        historyCalendarScreen(navController)
        editProfileScreen(navController)
    }
}

private fun NavGraphBuilder.onboardingScreen(navController: NavHostController) {
    composable<com.eugene.lift.ui.navigation.OnboardingRoute> {
        OnboardingRouteScreen(
            onComplete = {
                navController.navigate(com.eugene.lift.ui.navigation.WorkoutRoute) {
                    popUpTo(com.eugene.lift.ui.navigation.OnboardingRoute) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.profileScreen(navController: NavHostController) {
    composable<com.eugene.lift.ui.navigation.ProfileRoute> {
        ProfileRoute(
            onEditProfileClick = {
                navController.navigate(EditProfileRoute)
            }
        )
    }
}

private fun NavGraphBuilder.editProfileScreen(navController: NavHostController) {
    composable<EditProfileRoute> {
        EditProfileRouteScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.historyScreen(navController: NavHostController) {
    composable<com.eugene.lift.ui.navigation.HistoryRoute> { backStackEntry ->
        val savedStateHandle = backStackEntry.savedStateHandle
        val scrollDateString by savedStateHandle
            .getStateFlow<String?>("history_scroll_date", null)
            .collectAsStateWithLifecycle()
        val scrollDate = scrollDateString?.let { LocalDate.parse(it) }

        HistoryRoute(
            onSessionClick = { sessionId ->
                navController.navigate(SessionDetailRoute(sessionId))
            },
            onCalendarClick = {
                navController.navigate(HistoryCalendarRoute)
            },
            scrollToDate = scrollDate,
            onScrollConsumed = {
                savedStateHandle.remove<String>("history_scroll_date")
            }
        )
    }
}

private fun NavGraphBuilder.workoutScreen(navController: NavHostController) {
    composable<com.eugene.lift.ui.navigation.WorkoutRoute> {
        WorkoutRoute(
            onNavigateToEdit = { templateId ->
                navController.navigate(TemplateEditRoute(templateId))
            },
            onTemplateClick = { templateId ->
                navController.navigate(TemplateDetailRoute(templateId))
            },
            onStartWorkoutClick = { templateId ->
                navController.navigate(ActiveWorkoutRoute(templateId = templateId))
            },
            onStartEmptyClick = {
                navController.navigate(ActiveWorkoutRoute(templateId = null))
            }
        )
    }
}

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

private fun NavGraphBuilder.templateEditScreen(navController: NavHostController) {
    composable<TemplateEditRoute> { backStackEntry ->
        val viewModel: EditTemplateViewModel = hiltViewModel()
        val savedStateHandle = backStackEntry.savedStateHandle
        val selectedExerciseIds = savedStateHandle.get<List<String>>("selected_exercise_ids")
        val replaceExerciseId = savedStateHandle.get<String>("replace_exercise_id")
        val replaceExerciseIndex = savedStateHandle.get<Int>("replace_exercise_index")

        LaunchedEffect(selectedExerciseIds) {
            selectedExerciseIds?.let { ids ->
                viewModel.onEvent(EditTemplateUiEvent.ExercisesSelected(ids))
                savedStateHandle.remove<List<String>>("selected_exercise_ids")
            }
        }

        LaunchedEffect(replaceExerciseId, replaceExerciseIndex) {
            if (replaceExerciseId != null && replaceExerciseIndex != null) {
                viewModel.onEvent(EditTemplateUiEvent.ExerciseReplaced(replaceExerciseIndex, replaceExerciseId))
                savedStateHandle.remove<String>("replace_exercise_id")
                savedStateHandle.remove<Int>("replace_exercise_index")
            }
        }

        EditTemplateRoute(
            onNavigateBack = { navController.popBackStack() },
            onAddExerciseClick = { navController.navigate(ExercisePickerRoute) },
            onReplaceExerciseClick = { exerciseIndex ->
                backStackEntry.savedStateHandle["replace_exercise_index"] = exerciseIndex
                navController.navigate(ExercisePickerRoute)
            },
            viewModel = viewModel
        )
    }
}

private fun NavGraphBuilder.exercisePickerScreen(navController: NavHostController) {
    composable<ExercisePickerRoute> {
        ExercisesRoute(
            isSelectionMode = true,
            onAddClick = {
                navController.navigate(ExerciseAddRoute())
            },
            onExerciseClick = { exerciseId ->
                val previousEntry = navController.previousBackStackEntry
                val replaceIndex = previousEntry?.savedStateHandle?.get<Int>("replace_exercise_index")
                if (replaceIndex != null) {
                    // Replace mode: hand back a single exercise id for replacement
                    previousEntry.savedStateHandle["replace_exercise_id"] = exerciseId
                } else {
                    previousEntry?.savedStateHandle?.set("selected_exercise_id", exerciseId)
                }
                navController.popBackStack()
            },
            onExercisesSelected = { ids ->
                val previousEntry = navController.previousBackStackEntry
                val replaceIndex = previousEntry?.savedStateHandle?.get<Int>("replace_exercise_index")
                if (replaceIndex != null && ids.isNotEmpty()) {
                    // Replace mode: keep the same slot and replace with a single selected exercise.
                    previousEntry.savedStateHandle["replace_exercise_id"] = ids.first()
                } else {
                    previousEntry?.savedStateHandle?.set("selected_exercise_ids", ids)
                    previousEntry?.savedStateHandle?.set("selected_exercise_ids_active", ids)
                }
                navController.popBackStack()
            }
        )
    }
}

private fun NavGraphBuilder.settingsScreen() {
    composable<com.eugene.lift.ui.navigation.SettingsRoute> {
        SettingsRoute()
    }
}

private fun NavGraphBuilder.exerciseAddScreen(navController: NavHostController) {
    composable<ExerciseAddRoute> {
        AddExerciseRoute(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

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

        val replaceExerciseId = savedStateHandle.get<String>("replace_exercise_id")
        val replaceExerciseIndex = savedStateHandle.get<Int>("replace_exercise_index")
        LaunchedEffect(replaceExerciseId, replaceExerciseIndex) {
            if (replaceExerciseId != null && replaceExerciseIndex != null) {
                viewModel.replaceExerciseInSession(replaceExerciseIndex, replaceExerciseId)
                savedStateHandle.remove<String>("replace_exercise_id")
                savedStateHandle.remove<Int>("replace_exercise_index")
            }
        }

        ActiveWorkoutScreenRoute(
            onNavigateBack = { navController.popBackStack() },
            onAddExerciseClick = {
                navController.navigate(ExercisePickerRoute)
            },
            onExerciseClick = { exerciseId ->
                navController.navigate(ExerciseDetailRoute(exerciseId))
            },
            onReplaceExercise = { exerciseIndex ->
                // Store which slot to replace, then open picker
                backStackEntry.savedStateHandle["replace_exercise_index"] = exerciseIndex
                navController.navigate(ExercisePickerRoute)
            }
        )
    }
}

private fun NavGraphBuilder.sessionDetailScreen(navController: NavHostController) {
    composable<SessionDetailRoute> {
        SessionDetailRouteScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.historyCalendarScreen(navController: NavHostController) {
    composable<HistoryCalendarRoute> {
        HistoryCalendarRouteScreen(
            onNavigateBack = { navController.popBackStack() },
            onDateClick = { date ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("history_scroll_date", date.toString())
                navController.popBackStack()
            }
        )
    }
}
