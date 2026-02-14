package com.eugene.lift.ui.feature.history.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.util.WeightConverter
import com.eugene.lift.ui.feature.history.formatDurationSimple
import com.eugene.lift.ui.feature.history.formatWeight
import java.time.format.DateTimeFormatter

@Composable
fun SessionDetailRoute(
    onNavigateBack: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    SessionDetailScreen(
        session = session,
        userSettings = userSettings,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    session: WorkoutSession?,
    userSettings: UserSettings,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(session?.name ?: stringResource(R.string.history_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val weightLabel = if (userSettings.weightUnit == WeightUnit.LBS) {
            stringResource(R.string.unit_lbs)
        } else {
            stringResource(R.string.unit_kg)
        }

        val dateText = session.date.format(DateTimeFormatter.ofPattern("EEE, MMM d • HH:mm"))
        val durationText = formatDurationSimple(session.durationSeconds)
        val prCount = session.exercises.flatMap { it.sets }.count { it.isPr }
        val totalVolumeKg = session.exercises.flatMap { it.sets }
            .filter { it.completed }
            .sumOf { it.weight * it.reps }
        val totalVolume = if (userSettings.weightUnit == WeightUnit.LBS) {
            WeightConverter.kgToLbs(totalVolumeKg)
        } else {
            totalVolumeKg
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SessionSummaryCard(
                    dateText = dateText,
                    durationText = durationText,
                    volumeText = if (totalVolume > 0) "${formatWeight(totalVolume)} $weightLabel" else "-",
                    prCount = prCount,
                    exercisesCount = session.exercises.size
                )
            }
            // Render session note if present
            item {
                val note = session.note
                if (!note.isNullOrBlank()) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.history_session_note_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            items(session.exercises, key = { it.id }) { sessionExercise ->
                SessionExerciseCard(
                    sessionExercise = sessionExercise,
                    userSettings = userSettings
                )
            }
        }
    }
}

@Composable
private fun SessionSummaryCard(
    dateText: String,
    durationText: String,
    volumeText: String,
    prCount: Int,
    exercisesCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryChip(
                    icon = Icons.Default.AccessTime,
                    label = stringResource(R.string.history_detail_duration),
                    value = durationText
                )
                SummaryChip(
                    icon = Icons.Default.FitnessCenter,
                    label = stringResource(R.string.history_detail_volume),
                    value = volumeText
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryChip(
                    icon = Icons.Default.EmojiEvents,
                    label = stringResource(R.string.history_detail_prs),
                    value = prCount.toString()
                )
                SummaryChip(
                    icon = Icons.Default.FitnessCenter,
                    label = stringResource(R.string.history_detail_exercises),
                    value = exercisesCount.toString()
                )
            }
        }
    }
}

@Composable
private fun SummaryChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.size(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SessionExerciseCard(
    sessionExercise: SessionExercise,
    userSettings: UserSettings
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sessionExercise.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.history_detail_sets, sessionExercise.sets.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Exercise note if present
            val exNote = sessionExercise.note
            if (!exNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.history_exercise_note_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = exNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                sessionExercise.sets.forEachIndexed { index, workoutSet ->
                    SetRow(
                        index = index + 1,
                        workoutSet = workoutSet,
                        userSettings = userSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun SetRow(
    index: Int,
    workoutSet: WorkoutSet,
    userSettings: UserSettings
) {
    val weightLabel = if (userSettings.weightUnit == WeightUnit.LBS) {
        stringResource(R.string.unit_lbs)
    } else {
        stringResource(R.string.unit_kg)
    }
    val repsLabel = stringResource(R.string.unit_reps)
    val distanceLabel = stringResource(R.string.unit_km)

    val text = formatSetSummary(workoutSet, userSettings, weightLabel, repsLabel, distanceLabel)
    val textColor = if (workoutSet.completed) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(R.string.history_detail_set, index),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

private fun formatSetSummary(
    workoutSet: WorkoutSet,
    userSettings: UserSettings,
    weightLabel: String,
    repsLabel: String,
    distanceLabel: String
): String {
    val displayWeight = if (userSettings.weightUnit == WeightUnit.LBS) {
        WeightConverter.kgToLbs(workoutSet.weight)
    } else {
        workoutSet.weight
    }

    val hasWeight = workoutSet.weight > 0
    val hasReps = workoutSet.reps > 0
    val base = when {
        hasWeight && hasReps -> "${formatWeight(displayWeight)} $weightLabel × ${workoutSet.reps}"
        hasWeight -> "${formatWeight(displayWeight)} $weightLabel"
        hasReps -> "${workoutSet.reps} $repsLabel"
        else -> "-"
    }

    val timePart = workoutSet.timeSeconds?.takeIf { it > 0 }?.let { formatDurationSimple(it) }
    val distancePart = workoutSet.distance?.takeIf { it > 0 }?.let { "${formatWeight(it)} $distanceLabel" }
    val extraParts = listOfNotNull(distancePart, timePart)

    val effortText = when {
        workoutSet.rpe != null -> " RPE ${workoutSet.rpe}"
        workoutSet.rir != null -> " @${workoutSet.rir}"
        else -> ""
    }
    val prText = if (workoutSet.isPr) " PR" else ""

    val core = if (extraParts.isEmpty()) base else listOf(base, extraParts.joinToString(" • ")).joinToString(" • ")
    return "$core$effortText$prText"
}
