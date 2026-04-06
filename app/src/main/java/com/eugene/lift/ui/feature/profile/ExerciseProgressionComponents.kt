package com.eugene.lift.ui.feature.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eugene.lift.R
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseProgression
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.PrRecord
import com.eugene.lift.domain.model.ProgressionDataPoint
import com.eugene.lift.domain.usecase.exercise.GetExerciseProgressionUseCase
import java.time.format.DateTimeFormatter

// ── Short date formatter ──────────────────────────────────────────────────────
private val SHORT_DATE = DateTimeFormatter.ofPattern("MMM d")
private val FULL_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy")

// ── Top-level section composable ─────────────────────────────────────────────

/**
 * The Exercise Progression section on the Profile screen.
 * Shows up to [GetExerciseProgressionUseCase.MAX_TRACKED] tracked exercises
 * with a line chart and PR history for each.
 */
@Composable
fun ExerciseProgressionSection(
    progressions: List<ExerciseProgression>,
    allExercises: List<Exercise>,
    trackedIds: List<String>,
    showPickerDialog: Boolean,
    weightUnit: String,
    onAddClick: () -> Unit,
    onRemoveClick: (String) -> Unit,
    onToggleExercise: (String) -> Unit,
    onDismissPicker: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_exercise_progression),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (trackedIds.size < GetExerciseProgressionUseCase.MAX_TRACKED) {
                    IconButton(onClick = onAddClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.profile_track_exercise),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ── Empty state ─────────────────────────────────────────────────
            if (progressions.isEmpty()) {
                EmptyProgressionState(onAddClick = onAddClick)
            } else {
                // ── Tabs (one per tracked exercise) ─────────────────────────
                var selectedTab by remember(progressions.size) { mutableIntStateOf(0) }
                val safeTab = selectedTab.coerceIn(0, progressions.lastIndex)

                ScrollableTabRow(
                    selectedTabIndex = safeTab,
                    containerColor = Color.Transparent,
                    edgePadding = 0.dp
                ) {
                    progressions.forEachIndexed { index, progression ->
                        Tab(
                            selected = safeTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    progression.exerciseName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = safeTab,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "progression_tab"
                ) { tabIndex ->
                    val progression = progressions.getOrNull(tabIndex)
                    if (progression != null) {
                        ExerciseProgressionDetail(
                            progression = progression,
                            weightUnit = weightUnit,
                            onRemove = { onRemoveClick(progression.exerciseId) }
                        )
                    }
                }
            }
        }
    }

    // ── Exercise picker dialog ───────────────────────────────────────────────
    if (showPickerDialog) {
        ExercisePickerDialog(
            allExercises = allExercises,
            trackedIds = trackedIds,
            onToggle = onToggleExercise,
            onDismiss = onDismissPicker
        )
    }
}

// ── Detail view for one tracked exercise ────────────────────────────────────

@Composable
private fun ExerciseProgressionDetail(
    progression: ExerciseProgression,
    weightUnit: String,
    onRemove: () -> Unit
) {
    Column {
        // Current PR badge
        progression.currentPr?.let { pr ->
            PrBadge(pr = pr, weightUnit = weightUnit)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Line chart
        if (progression.dataPoints.size >= 2) {
            ProgressionLineChart(
                dataPoints = progression.dataPoints,
                measureType = if (progression.dataPoints.first().estimatedOneRepMax > 0)
                    MeasureType.REPS_AND_WEIGHT else MeasureType.REPS_ONLY,
                weightUnit = weightUnit,
                primaryColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        } else if (progression.dataPoints.size == 1) {
            SingleDataPointNote()
        } else {
            NoDataNote()
        }

        // PR History
        if (progression.prHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            PrHistoryList(
                records = progression.prHistory,
                weightUnit = weightUnit
            )
        }

        // Remove button
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.profile_stop_tracking), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ── PR badge ─────────────────────────────────────────────────────────────────

@Composable
private fun PrBadge(pr: PrRecord, weightUnit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = stringResource(R.string.profile_personal_record),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.profile_personal_record),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (pr.weight > 0) {
                Text(
                    text = "${formatWeight(pr.weight)} $weightUnit × ${pr.reps}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Text(
                    text = "${pr.reps}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Text(
            text = pr.date.format(SHORT_DATE),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

// ── Progression line chart ────────────────────────────────────────────────────

@Composable
private fun ProgressionLineChart(
    dataPoints: List<ProgressionDataPoint>,
    measureType: MeasureType,
    weightUnit: String,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val isWeightBased = measureType == MeasureType.REPS_AND_WEIGHT

    val values = if (isWeightBased) {
        dataPoints.map {
            if (it.estimatedOneRepMax > 0) it.estimatedOneRepMax else it.weight.toDouble()
        }
    } else {
        dataPoints.map { it.reps.toDouble() }
    }

    val minValue = values.minOrNull() ?: 0.0
    val maxValue = values.maxOrNull() ?: 1.0
    val valueRange = (maxValue - minValue).let { if (it == 0.0) 1.0 else it }

    val fillColor = primaryColor.copy(alpha = 0.15f)
    val lineColor = primaryColor

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val w = size.width
            val h = size.height
            val pointCount = dataPoints.size
            if (pointCount < 2) return@Canvas

            val stepX = w / (pointCount - 1).toFloat()

            fun xAt(i: Int) = i * stepX
            fun yAt(v: Double) = (h * (1.0 - (v - minValue) / valueRange)).toFloat()

            val path = Path()
            val fillPath = Path()

            values.forEachIndexed { i, v ->
                val x = xAt(i)
                val y = yAt(v)
                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    // Smooth cubic bezier
                    val prevX = xAt(i - 1)
                    val prevY = yAt(values[i - 1])
                    val cpX = (prevX + x) / 2f
                    path.cubicTo(cpX, prevY, cpX, y, x, y)
                    fillPath.cubicTo(cpX, prevY, cpX, y, x, y)
                }
            }

            // Close fill area
            fillPath.lineTo(xAt(pointCount - 1), h)
            fillPath.close()

            // Draw fill
            drawPath(path = fillPath, color = fillColor)
            // Draw line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            // Draw dots at each point
            values.forEachIndexed { i, v ->
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(xAt(i), yAt(v))
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(xAt(i), yAt(v))
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayPoints = if (dataPoints.size > 6) {
                // Show first, last, and evenly spaced in between
                listOf(
                    dataPoints.first(),
                    dataPoints[dataPoints.size / 3],
                    dataPoints[2 * dataPoints.size / 3],
                    dataPoints.last()
                ).distinct()
            } else {
                dataPoints
            }

            displayPoints.forEach { point ->
                Text(
                    text = point.date.format(SHORT_DATE),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Y axis range label
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isWeightBased) stringResource(
                    R.string.profile_e1rm_label,
                    formatWeight(minValue),
                    weightUnit
                ) else stringResource(R.string.profile_min_reps, minValue.toInt()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = if (isWeightBased) "${formatWeight(maxValue)} $weightUnit"
                else maxValue.toInt().toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ── PR History list ───────────────────────────────────────────────────────────

@Composable
private fun PrHistoryList(
    records: List<PrRecord>,
    weightUnit: String
) {
    val showAll = rememberSaveable { mutableStateOf(false) }
    val displayRecords = if (showAll.value || records.size <= 3) records else records.take(3)

    Column {
        Text(
            text = stringResource(R.string.profile_pr_history),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        displayRecords.forEachIndexed { index, record ->
            PrHistoryRow(record = record, weightUnit = weightUnit, rank = index + 1)
            if (index < displayRecords.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        if (records.size > 3) {
            TextButton(
                onClick = { showAll.value = !showAll.value },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    if (showAll.value) stringResource(R.string.profile_show_less) else stringResource(R.string.profile_show_all_prs, records.size),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun PrHistoryRow(record: PrRecord, weightUnit: String, rank: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (rank == 1) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (rank == 1) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (record.weight > 0) {
                Text(
                    text = "${formatWeight(record.weight)} $weightUnit × ${record.reps}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = "${record.reps}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = record.sessionName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = record.date.format(SHORT_DATE),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Exercise picker dialog ────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExercisePickerDialog(
    allExercises: List<Exercise>,
    trackedIds: List<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(query, allExercises) {
        if (query.isBlank()) allExercises
        else allExercises.filter { it.name.contains(query, ignoreCase = true) }
    }
    val atLimit = trackedIds.size >= GetExerciseProgressionUseCase.MAX_TRACKED

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(stringResource(R.string.profile_track_exercise))
                Text(
                    text = stringResource(R.string.profile_tracked_count, trackedIds.size, GetExerciseProgressionUseCase.MAX_TRACKED),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.profile_search_exercises_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.id }) { exercise ->
                        val isTracked = exercise.id in trackedIds
                        ExercisePickerRow(
                            exercise = exercise,
                            isTracked = isTracked,
                            isDisabled = atLimit && !isTracked,
                            onToggle = { onToggle(exercise.id) }
                        )
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.profile_no_exercises_found),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_done)) }
        }
    )
}

@Composable
private fun ExercisePickerRow(
    exercise: Exercise,
    isTracked: Boolean,
    isDisabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (!isDisabled) Modifier.clickable(onClick = onToggle)
                else Modifier
            )
            .background(
                if (isTracked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else Color.Transparent
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isTracked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isDisabled && !isTracked)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = exercise.category.name.lowercase().replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (isDisabled && !isTracked) 0.38f else 1f
                )
            )
        }
        if (isTracked) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.cd_tracked),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Edge-case composables ─────────────────────────────────────────────────────

@Composable
private fun EmptyProgressionState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.profile_add_exercise_chart),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SingleDataPointNote() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.profile_chart_more_sessions_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NoDataNote() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.profile_no_completed_sets),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatWeight(value: Double): String {
    return if (value == kotlin.math.floor(value)) {
        value.toInt().toString()
    } else {
        "%.1f".format(value)
    }
}
