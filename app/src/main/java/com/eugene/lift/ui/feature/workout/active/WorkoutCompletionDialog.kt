package com.eugene.lift.ui.feature.workout.active

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eugene.lift.R
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.model.WorkoutCompletionSummary
import com.eugene.lift.ui.feature.history.formatDuration
import com.eugene.lift.ui.util.WeightFormatters
import kotlin.math.roundToInt

@Composable
fun WorkoutCompletionDialog(
    summary: WorkoutCompletionSummary,
    onDone: () -> Unit
) {
    val hasPr = summary.personalRecordCount > 0

    Dialog(
        onDismissRequest = onDone,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(20.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CompletionHero(hasPr = hasPr)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(
                        if (hasPr) R.string.workout_summary_pr_title
                        else R.string.workout_summary_title
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = summary.workoutName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.workout_summary_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
                CompletionStats(summary = summary)

                AnimatedVisibility(visible = hasPr) {
                    PersonalRecordCelebration(count = summary.personalRecordCount)
                }

                VolumeMilestoneCard(totalVolumeKg = summary.totalVolumeKg)

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.workout_summary_done))
                }
            }
        }
    }
}

@Composable
private fun CompletionHero(hasPr: Boolean) {
    Surface(
        modifier = Modifier.size(76.dp),
        shape = CircleShape,
        color = if (hasPr) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (hasPr) Icons.Default.EmojiEvents else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (hasPr) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CompletionStats(summary: WorkoutCompletionSummary) {
    val unitLabel = stringResource(
        if (summary.weightUnit == WeightUnit.LBS) R.string.unit_lbs else R.string.unit_kg
    )
    val volume = "${WeightFormatters.formatWeight(summary.totalVolume, summary.weightUnit)} $unitLabel"

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompletionStatCard(
                icon = Icons.Default.Timer,
                value = formatDuration(summary.durationSeconds),
                label = stringResource(R.string.workout_summary_duration),
                modifier = Modifier.weight(1f)
            )
            CompletionStatCard(
                icon = Icons.Default.CheckCircle,
                value = summary.completedSetCount.toString(),
                label = stringResource(R.string.workout_summary_sets),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompletionStatCard(
                icon = Icons.Default.FitnessCenter,
                value = summary.completedExerciseCount.toString(),
                label = stringResource(R.string.workout_summary_exercises),
                modifier = Modifier.weight(1f)
            )
            CompletionStatCard(
                icon = Icons.Default.Scale,
                value = volume,
                label = stringResource(R.string.workout_summary_volume),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompletionStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PersonalRecordCelebration(count: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = pluralStringResource(R.plurals.workout_summary_pr_message, count, count),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun VolumeMilestoneCard(totalVolumeKg: Double) {
    val milestone = remember(totalVolumeKg) { calculateVolumeMilestone(totalVolumeKg) }
    val icon = when (milestone.kind) {
        VolumeMilestoneKind.BODYWEIGHT -> Icons.Default.FitnessCenter
        VolumeMilestoneKind.CATS -> Icons.Default.Pets
        VolumeMilestoneKind.CARS -> Icons.Default.DirectionsCar
        VolumeMilestoneKind.COWS -> Icons.Default.Agriculture
        VolumeMilestoneKind.YACHTS -> Icons.Default.Sailing
        VolumeMilestoneKind.WHALES -> Icons.Default.Waves
    }
    val message = when (milestone.kind) {
        VolumeMilestoneKind.BODYWEIGHT -> stringResource(R.string.workout_summary_bodyweight_motivation)
        VolumeMilestoneKind.CATS -> pluralStringResource(
            R.plurals.workout_summary_cats,
            milestone.count,
            milestone.count
        )
        VolumeMilestoneKind.CARS -> pluralStringResource(
            R.plurals.workout_summary_cars,
            milestone.count,
            milestone.count
        )
        VolumeMilestoneKind.COWS -> pluralStringResource(
            R.plurals.workout_summary_cows,
            milestone.count,
            milestone.count
        )
        VolumeMilestoneKind.YACHTS -> pluralStringResource(
            R.plurals.workout_summary_yachts,
            milestone.count,
            milestone.count
        )
        VolumeMilestoneKind.WHALES -> pluralStringResource(
            R.plurals.workout_summary_whales,
            milestone.count,
            milestone.count
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

internal enum class VolumeMilestoneKind {
    BODYWEIGHT,
    CATS,
    CARS,
    COWS,
    YACHTS,
    WHALES
}

internal data class VolumeMilestone(
    val kind: VolumeMilestoneKind,
    val count: Int
)

internal fun calculateVolumeMilestone(totalVolumeKg: Double): VolumeMilestone {
    if (!totalVolumeKg.isFinite() || totalVolumeKg <= 0.0) {
        return VolumeMilestone(VolumeMilestoneKind.BODYWEIGHT, 0)
    }

    val (kind, referenceWeightKg) = when {
        totalVolumeKg < 1_500.0 -> VolumeMilestoneKind.CATS to 4.5
        totalVolumeKg < 3_000.0 -> VolumeMilestoneKind.CARS to 1_500.0
        totalVolumeKg < 10_000.0 -> VolumeMilestoneKind.COWS to 600.0
        totalVolumeKg < 30_000.0 -> VolumeMilestoneKind.YACHTS to 10_000.0
        else -> VolumeMilestoneKind.WHALES to 30_000.0
    }
    val count = (totalVolumeKg / referenceWeightKg).roundToInt().coerceAtLeast(1)
    return VolumeMilestone(kind, count)
}
