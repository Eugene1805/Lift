package com.eugene.lift.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    // Wait until we know whether onboarding is needed before rendering NavHost
    // (avoids a flash of the wrong start destination)
    val startDestination = when (onboardingComplete) {
        true -> com.eugene.lift.ui.navigation.WorkoutRoute as Any
        false -> com.eugene.lift.ui.navigation.OnboardingRoute as Any
        null -> return   // still loading from DataStore
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Onboarding
        onboardingScreen(navController)

        // Main bottom navigation destinations
        profileScreen(navController)
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
        sessionDetailScreen(navController)
        historyCalendarScreen(navController)
        editProfileScreen(navController)
    }
}

// Onboarding Screen
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

// Profile Screen
private fun NavGraphBuilder.profileScreen(navController: NavHostController) {
    composable<com.eugene.lift.ui.navigation.ProfileRoute> {
        ProfileRoute(
            onEditProfileClick = {
                navController.navigate(EditProfileRoute)
            }
        )
    }
}

// Edit Profile Screen
private fun NavGraphBuilder.editProfileScreen(navController: NavHostController) {
    composable<EditProfileRoute> {
        EditProfileRouteScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

// History Screen
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
            onStartWorkoutClick = { templateId ->
                navController.navigate(ActiveWorkoutRoute(templateId = templateId))
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
                viewModel.onEvent(EditTemplateUiEvent.ExercisesSelected(ids))
                savedStateHandle.remove<List<String>>("selected_exercise_ids")
            }
        }

        EditTemplateRoute(
            onNavigateBack = { navController.popBackStack() },
            onAddExerciseClick = { navController.navigate(ExercisePickerRoute) },
            viewModel = viewModel
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

        ActiveWorkoutScreenRoute(
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

// Session Detail Screen
private fun NavGraphBuilder.sessionDetailScreen(navController: NavHostController) {
    composable<SessionDetailRoute> {
        SessionDetailRouteScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

// History Calendar Screen
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
