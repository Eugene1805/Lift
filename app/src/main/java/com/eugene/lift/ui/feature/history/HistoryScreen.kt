package com.eugene.lift.ui.feature.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.util.WeightConverter
import java.time.format.DateTimeFormatter

@Composable
fun HistoryRoute(
    onSessionClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    HistoryScreen(
        historyItems = historyItems,
        userSettings = userSettings,
        onSessionClick = onSessionClick
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyItems: List<HistoryUiItem>,
    userSettings: UserSettings,
    onSessionClick: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        if (historyItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            ) {
                // Iteramos sobre la lista polimórfica (Header o Session)
                historyItems.forEach { item ->
                    when (item) {
                        is HistoryUiItem.Header -> {
                            stickyHeader(key = item.title) {
                                HistoryHeader(title = item.title)
                            }
                        }
                        is HistoryUiItem.SessionItem -> {
                            item(key = item.session.id) {
                                HistorySessionCard(
                                    session = item.session,
                                    userSettings = userSettings,
                                    onClick = { onSessionClick(item.session.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface, // O surfaceVariant para resaltar más
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun HistorySessionCard(
    session: WorkoutSession,
    userSettings: UserSettings,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. CABECERA: Nombre, Hora y Duración
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = session.date.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(formatDuration(session.durationSeconds))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. ESTADÍSTICAS: Volumen y PRs
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Volumen Total (Suma de Peso * Reps de todos los sets completados)
                val totalVolumeKg = session.exercises.flatMap { it.sets }
                    .filter { it.completed }
                    .sumOf { it.weight * it.reps }

                // Convert to display unit
                val totalVolume = if (userSettings.weightUnit == WeightUnit.LBS) {
                    WeightConverter.kgToLbs(totalVolumeKg)
                } else {
                    totalVolumeKg
                }

                val weightLabel = if (userSettings.weightUnit == WeightUnit.LBS) {
                    stringResource(R.string.history_lbs)
                } else {
                    stringResource(R.string.history_kg)
                }

                if (totalVolume > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${formatWeight(totalVolume)} $weightLabel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Contador de PRs
                val prCount = session.exercises.flatMap { it.sets }.count { it.isPr }
                if (prCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFC107) // Color Dorado/Ambar
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$prCount ${stringResource(R.string.history_prs)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // 3. LISTA DE EJERCICIOS (Vertical)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                session.exercises.forEach { sessionExercise ->
                    val completedSets = sessionExercise.sets.count { it.completed }

                    // Solo mostramos ejercicios que tuvieron actividad
                    if (completedSets > 0 || sessionExercise.sets.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Izquierda: Círculo con Sets + Nombre
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Indicador de Series (ej: "3")
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$completedSets",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = sessionExercise.exercise.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Derecha: Mejor Set (ej: "100kg x 5")
                            val weightLabel = if (userSettings.weightUnit == WeightUnit.LBS) {
                                stringResource(R.string.history_lbs)
                            } else {
                                stringResource(R.string.history_kg)
                            }
                            val bestSetText = getBestSetString(
                                sessionExercise.sets,
                                weightLabel,
                                stringResource(R.string.history_reps),
                                userSettings
                            )
                            Text(
                                text = bestSetText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- FUNCIONES DE UTILIDAD (Ponlas al final del archivo o en tu util) ---

// Formatea doubles quitando decimales si es entero (100.0 -> "100")
fun formatWeight(weight: Double): String {
    return if (weight % 1.0 == 0.0) {
        weight.toInt().toString()
    } else {
        weight.toString()
    }
}

// Calcula el "Mejor Set" para mostrar en el resumen
fun getBestSetString(
    sets: List<com.eugene.lift.domain.model.WorkoutSet>,
    kgLabel: String,
    repsLabel: String,
    userSettings: UserSettings
): String {
    // Filtramos completados y ordenamos por peso descendente
    val bestSet = sets.filter { it.completed }
        .maxWithOrNull(compareBy({ it.weight }, { it.reps }))
        ?: return "-"

    // Convert weight from kg (storage) to display unit
    val displayWeight = if (userSettings.weightUnit == WeightUnit.LBS) {
        WeightConverter.kgToLbs(bestSet.weight)
    } else {
        bestSet.weight
    }

    return when {
        displayWeight > 0 -> "${formatWeight(displayWeight)}$kgLabel × ${bestSet.reps}"
        bestSet.reps > 0 -> "${bestSet.reps} $repsLabel"
        (bestSet.timeSeconds ?: 0) > 0 -> formatDurationSimple(bestSet.timeSeconds!!)
        (bestSet.distance ?: 0.0) > 0 -> "${bestSet.distance} km"
        else -> "-"
    }
}

// Utilidad simple para formato 1h 30m (version non-Composable)
fun formatDurationSimple(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

// Versión Composable de formatDuration que usa strings resources
@Composable
fun formatDuration(seconds: Long): String {
    val hoursLabel = stringResource(R.string.history_hours_short)
    val minutesLabel = stringResource(R.string.history_minutes_short)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "$hours$hoursLabel $minutes$minutesLabel" else "$minutes$minutesLabel"
}