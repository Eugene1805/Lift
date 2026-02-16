package com.eugene.lift.ui.feature.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.util.Locale

@Composable
fun HistoryRoute(
    onSessionClick: (String) -> Unit,
    onCalendarClick: () -> Unit,
    scrollToDate: java.time.LocalDate?,
    onScrollConsumed: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    HistoryScreen(
        historyItems = historyItems,
        userSettings = userSettings,
        onSessionClick = onSessionClick,
        onCalendarClick = onCalendarClick,
        scrollToDate = scrollToDate,
        onScrollConsumed = onScrollConsumed
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyItems: List<HistoryUiItem>,
    userSettings: UserSettings,
    onSessionClick: (String) -> Unit,
    onCalendarClick: () -> Unit,
    scrollToDate: java.time.LocalDate?,
    onScrollConsumed: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToDate, historyItems) {
        if (scrollToDate == null) return@LaunchedEffect
        val targetIndex = historyItems.indexOfFirst { item ->
            item is HistoryUiItem.SessionItem && item.session.date.toLocalDate() == scrollToDate
        }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
        onScrollConsumed()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                actions = {
                    IconButton(onClick = onCalendarClick) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.history_calendar),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        if (historyItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                state = listState,
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
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
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
    val totalVolumeKg = session.exercises.flatMap { it.sets }
        .filter { it.completed }
        .sumOf { it.weight * it.reps }
    val totalVolume = if (userSettings.weightUnit == WeightUnit.LBS) {
        WeightConverter.kgToLbs(totalVolumeKg)
    } else {
        totalVolumeKg
    }
    val weightLabel = if (userSettings.weightUnit == WeightUnit.LBS) {
        stringResource(R.string.unit_lbs)
    } else {
        stringResource(R.string.unit_kg)
    }
    val prCount = session.exercises.flatMap { it.sets }.count { it.isPr }
    val repsLabel = stringResource(R.string.unit_reps)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SessionHeaderRow(session)

            Spacer(modifier = Modifier.height(12.dp))

            SessionStatsRow(totalVolume = totalVolume, weightLabel = weightLabel, prCount = prCount)

            BestSetsSection(
                exercises = session.exercises,
                userSettings = userSettings,
                weightLabel = weightLabel,
                repsLabel = repsLabel
            )

            SessionNoteSection(note = session.note)
        }
    }
}

@Composable
private fun SessionHeaderRow(session: WorkoutSession) {
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

        DurationBadge(durationSeconds = session.durationSeconds)
    }
}

@Composable
private fun DurationBadge(durationSeconds: Long) {
    Badge(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(formatDuration(durationSeconds))
        }
    }
}

@Composable
private fun SessionStatsRow(totalVolume: Double, weightLabel: String, prCount: Int) {
    if (totalVolume <= 0 && prCount <= 0) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (totalVolume > 0) VolumeStat(totalVolume = totalVolume, weightLabel = weightLabel)
        if (prCount > 0) PrStat(prCount = prCount)
    }
}

@Composable
private fun VolumeStat(totalVolume: Double, weightLabel: String) {
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

@Composable
private fun PrStat(prCount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$prCount ${stringResource(R.string.history_detail_prs)}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BestSetsSection(
    exercises: List<com.eugene.lift.domain.model.SessionExercise>,
    userSettings: UserSettings,
    weightLabel: String,
    repsLabel: String
) {
    if (exercises.isEmpty()) return

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = stringResource(R.string.history_best_set),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        exercises.forEach { sessionExercise ->
            ExerciseSummaryCard(
                sessionExercise = sessionExercise,
                userSettings = userSettings,
                weightLabel = weightLabel,
                repsLabel = repsLabel
            )
        }
    }
}

@Composable
private fun ExerciseSummaryCard(
    sessionExercise: com.eugene.lift.domain.model.SessionExercise,
    userSettings: UserSettings,
    weightLabel: String,
    repsLabel: String
) {
    if (sessionExercise.sets.isEmpty()) return

    val completedSets = sessionExercise.sets.count { it.completed }
    val hasPR = sessionExercise.sets.any { it.isPr }
    val bestSet = sessionExercise.sets
        .filter { it.completed }
        .maxWithOrNull(compareBy({ it.weight }, { it.reps }))
    val bestSetText = getBestSetString(
        sessionExercise.sets,
        weightLabel,
        repsLabel,
        userSettings
    )
    val effortText = bestSet.toEffortSuffix()

    Surface(
        color = if (hasPR) {
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
            ExerciseSummaryHeader(
                completedSets = completedSets,
                hasPR = hasPR,
                exerciseName = sessionExercise.exercise.name,
                bestSetLabel = bestSetText + effortText
            )

            ExerciseNote(note = sessionExercise.note)
        }
    }
}

@Composable
private fun ExerciseSummaryHeader(
    completedSets: Int,
    hasPR: Boolean,
    exerciseName: String,
    bestSetLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (hasPR) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "PR",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Text(
            text = bestSetLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SessionNoteSection(note: String?) {
    if (note.isNullOrBlank()) return

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.history_session_note_label),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = note,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ExerciseNote(note: String?) {
    if (note.isNullOrBlank()) return

    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = stringResource(R.string.history_exercise_note_label),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = note,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface
    )
}

private fun com.eugene.lift.domain.model.WorkoutSet?.toEffortSuffix(): String {
    return this?.let { set ->
        when {
            set.rpe != null -> " RPE ${set.rpe}"
            set.rir != null -> " @${set.rir}"
            else -> ""
        }
    } ?: ""
}

@Composable
fun formatDuration(seconds: Long): String {
    val hoursLabel = stringResource(R.string.unit_hours_short)
    val minutesLabel = stringResource(R.string.unit_minutes_short)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "$hours$hoursLabel $minutes$minutesLabel" else "$minutes$minutesLabel"
}

fun formatWeight(weight: Double): String {
    return if (weight % 1.0 == 0.0) {
        weight.toInt().toString()
    } else {
        String.format(Locale.ENGLISH,"%.1f", weight)
    }
}

fun getBestSetString(
    sets: List<com.eugene.lift.domain.model.WorkoutSet>,
    kgLabel: String,
    repsLabel: String,
    userSettings: UserSettings
): String {
    val bestSet = sets.filter { it.completed }
        .maxWithOrNull(compareBy({ it.weight }, { it.reps }))
        ?: return "-"

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

fun formatDurationSimple(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
