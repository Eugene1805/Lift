package com.eugene.lift

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.usecase.GetSettingsUseCase
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
import com.eugene.lift.ui.navigation.ActiveWorkoutRoute
import com.eugene.lift.ui.navigation.ExerciseAddRoute
import com.eugene.lift.ui.navigation.ExerciseDetailRoute
import com.eugene.lift.ui.navigation.ExerciseListRoute
import com.eugene.lift.ui.navigation.ExercisePickerRoute
import com.eugene.lift.ui.navigation.HistoryRoute
import com.eugene.lift.ui.navigation.ProfileRoute
import com.eugene.lift.ui.navigation.SettingsRoute
import com.eugene.lift.ui.navigation.TemplateDetailRoute
import com.eugene.lift.ui.navigation.TemplateEditRoute
import com.eugene.lift.ui.navigation.WorkoutRoute
import com.eugene.lift.ui.theme.LiftTheme
import com.eugene.lift.worker.SeedDatabaseWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
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
                .collectAsState(initial = UserSettings(AppTheme.SYSTEM, WeightUnit.KG, DistanceUnit.KM, "en"))

            val context = LocalContext.current

            // 2. Creamos el contexto localizado pero lo envolvemos en nuestro HiltSafeLocalizedContext
            val localizedContext = remember(settingsState.languageCode) {
                val locale = Locale(settingsState.languageCode)
                Locale.setDefault(locale)
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)

                // Creamos el contexto de configuración (el que tiene las traducciones)
                val configContext = context.createConfigurationContext(config)

                // IMPORTANTE: Envolvemos el contexto original (la Activity)
                // para que Hilt pueda encontrarla haciendo "baseContext"
                HiltSafeLocalizedContext(base = context, localizedConfigContext = configContext)
            }
            CompositionLocalProvider(LocalContext provides localizedContext) {
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
        BottomNavItem(R.string.nav_profile, Icons.Default.Person, ProfileRoute),
        BottomNavItem(R.string.nav_history, Icons.Default.History, HistoryRoute),
        BottomNavItem(R.string.nav_workout, Icons.Default.Add, WorkoutRoute),
        BottomNavItem(R.string.nav_exercises, Icons.Default.FitnessCenter, ExerciseListRoute),
        BottomNavItem(R.string.nav_settings, Icons.Default.Settings, SettingsRoute)
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
            startDestination = WorkoutRoute,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable<ProfileRoute> {
                ProfileRoute()
            }

            composable<HistoryRoute> {
                HistoryRoute(
                    onSessionClick = { sessionId ->}
                )
            }

            composable<WorkoutRoute> {
                WorkoutRoute(
                    onNavigateToEdit = { templateId ->
                        navController.navigate(TemplateEditRoute(templateId))
                    },
                    onTemplateClick = { templateId ->
                        navController.navigate(
                            com.eugene.lift.ui.navigation.TemplateDetailRoute(
                                templateId
                            )
                        )
                    },
                    onStartEmptyClick = {
                        navController.navigate(com.eugene.lift.ui.navigation.ActiveWorkoutRoute(templateId = null))
                    }
                )
            }

            composable<TemplateDetailRoute> {
                TemplateDetailRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onStartWorkout = { templateId ->
                        // Aquí SÍ iniciamos el entrenamiento (reemplazando la pantalla de detalle para que al dar atrás volvamos a la lista)
                        navController.navigate(com.eugene.lift.ui.navigation.ActiveWorkoutRoute(templateId = templateId)) {
                            // Opcional: Sacar el detalle del stack para que "Atrás" en el entreno vaya a la lista
                            popUpTo(WorkoutRoute)
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
            composable<SettingsRoute> {
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
            composable<ActiveWorkoutRoute> { backStackEntry ->
                val viewModel: ActiveWorkoutViewModel = hiltViewModel()
                val savedStateHandle = backStackEntry.savedStateHandle
                val selectedExerciseIds = savedStateHandle.get<List<String>>("selected_exercise_ids_active")

                LaunchedEffect(selectedExerciseIds) {
                    selectedExerciseIds?.let { ids ->
                        viewModel.onAddExercisesToSession(ids) // <--- Método nuevo del VM
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
    }
}

class HiltSafeLocalizedContext(
    base: Context,
    private val localizedConfigContext: Context
) : ContextWrapper(base) {
    // Redirigimos las llamadas de recursos al contexto localizado
    override fun getResources(): Resources = localizedConfigContext.resources
    override fun getAssets(): AssetManager = localizedConfigContext.assets
}