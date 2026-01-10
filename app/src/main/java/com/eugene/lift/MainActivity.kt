package com.eugene.lift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.usecase.GetSettingsUseCase
import com.eugene.lift.ui.feature.exercises.AddExerciseRoute
import com.eugene.lift.ui.feature.exercises.ExercisesRoute
import com.eugene.lift.ui.feature.exercises.detail.ExerciseDetailRoute
import com.eugene.lift.ui.feature.history.HistoryRoute
import com.eugene.lift.ui.feature.profile.ProfileRoute
import com.eugene.lift.ui.feature.settings.SettingsRoute
import com.eugene.lift.ui.feature.workout.WorkoutRoute
import com.eugene.lift.ui.feature.workout.edit.EditTemplateScreen
import com.eugene.lift.ui.feature.workout.edit.EditTemplateViewModel
import com.eugene.lift.ui.navigation.ExerciseAddRoute
import com.eugene.lift.ui.navigation.ExerciseDetailRoute
import com.eugene.lift.ui.navigation.ExerciseListRoute
import com.eugene.lift.ui.navigation.ExercisePickerRoute
import com.eugene.lift.ui.navigation.TemplateEditRoute
import com.eugene.lift.ui.theme.LiftTheme
import com.eugene.lift.worker.SeedDatabaseWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var getSettingsUseCase: GetSettingsUseCase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val seedRequest = OneTimeWorkRequest.Builder(SeedDatabaseWorker::class.java)
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "seed_db_work",
            ExistingWorkPolicy.KEEP,
            seedRequest
        )
        setContent {
            val settingsState by getSettingsUseCase()
                .collectAsState(initial = UserSettings())

            val useDarkTheme = when (settingsState.theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            LiftTheme (darkTheme = useDarkTheme){
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppShell()
                }
            }
        }
    }
}

data class BottomNavItem<T : Any>(
    @get:StringRes val labelRes: Int,
    val icon: ImageVector,
    val route: T
)

@Composable
fun MainAppShell() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(R.string.nav_profile, Icons.Default.Person, com.eugene.lift.ui.navigation.ProfileRoute),
        BottomNavItem(R.string.nav_history, Icons.Default.History, com.eugene.lift.ui.navigation.HistoryRoute),
        BottomNavItem(R.string.nav_workout, Icons.Default.Add, com.eugene.lift.ui.navigation.WorkoutRoute),
        BottomNavItem(R.string.nav_exercises, Icons.Default.FitnessCenter, ExerciseListRoute),
        BottomNavItem(R.string.nav_settings, Icons.Default.Settings, com.eugene.lift.ui.navigation.SettingsRoute)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination


    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.hasRoute(item.route::class)
                        } == true

                        val label = stringResource(item.labelRes)

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ExerciseListRoute,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable<com.eugene.lift.ui.navigation.ProfileRoute> {
                ProfileRoute()
            }

            composable<com.eugene.lift.ui.navigation.HistoryRoute> {
                HistoryRoute()
            }

            composable<com.eugene.lift.ui.navigation.WorkoutRoute> {
                WorkoutRoute(
                    onNavigateToEdit = { templateId ->
                        // Si templateId es null, navegamos sin argumento (crear)
                        // Si tiene valor, lo pasamos. La data class maneja el nullable.
                        navController.navigate(TemplateEditRoute(templateId))
                    }
                )
            }
            composable<TemplateEditRoute> { backStackEntry ->
                // Obtenemos el ViewModel asociado a este BackStackEntry (Scoped)
                val viewModel: EditTemplateViewModel = hiltViewModel()

                // INTERCEPTOR DE RESULTADOS:
                // Observamos si la pantalla de selección nos devolvió un ID
                val savedStateHandle = backStackEntry.savedStateHandle
                val selectedExerciseId = savedStateHandle.get<String>("selected_exercise_id")

                // Si hay un ID, se lo pasamos al ViewModel y limpiamos el estado para no re-procesarlo
                LaunchedEffect(selectedExerciseId) {
                    selectedExerciseId?.let { id ->
                        viewModel.onExerciseSelected(id)
                        savedStateHandle.remove<String>("selected_exercise_id")
                    }
                }

                EditTemplateScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAddExerciseClick = {
                        navController.navigate(ExercisePickerRoute)
                    }
                    // El ViewModel se inyecta internamente con hiltViewModel()
                )
            }

            // 3. SELECTOR DE EJERCICIOS (Reutilizando ExercisesRoute)
            composable<ExercisePickerRoute> {
                ExercisesRoute(
                    isSelectionMode = true, // Activamos modo selección (Oculta FAB)
                    onAddClick = { /* No hace nada o navega a crear si quisieras permitirlo */ },
                    onExerciseClick = { exerciseId ->
                        // AL HACER CLICK:
                        // 1. Guardamos el ID en el SavedStateHandle de la pantalla ANTERIOR (el Editor)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_exercise_id", exerciseId)

                        // 2. Cerramos el selector
                        navController.popBackStack()
                    }
                )
            }
            composable<com.eugene.lift.ui.navigation.SettingsRoute> {
                SettingsRoute()
            }

            composable<ExerciseAddRoute> {
                AddExerciseRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<ExerciseListRoute> {
                ExercisesRoute(
                    onAddClick = { navController.navigate(ExerciseAddRoute()) },
                    onExerciseClick = { exerciseId ->
                        navController.navigate(ExerciseDetailRoute(exerciseId))
                    }
                )
            }
            composable<ExerciseDetailRoute> {
                ExerciseDetailRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onEditClick = { exerciseId ->
                        navController.navigate(ExerciseAddRoute(exerciseId = exerciseId))
                    }
                )
            }
        }
    }
}