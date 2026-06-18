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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.usecase.exercise.GetExerciseProgressionUseCase
import com.eugene.lift.ui.util.WeightFormatters
import kotlin.math.abs
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
    weightUnit: WeightUnit,
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
    weightUnit: WeightUnit,
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
private fun PrBadge(pr: PrRecord, weightUnit: WeightUnit) {
    val unitLabel = when (weightUnit) {
        WeightUnit.KG -> stringResource(R.string.unit_kg)
        WeightUnit.LBS -> stringResource(R.string.unit_lbs)
    }
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
                    text = "${WeightFormatters.formatWeight(pr.weight, weightUnit)} $unitLabel × ${pr.reps}",
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
    weightUnit: WeightUnit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val isWeightBased = measureType == MeasureType.REPS_AND_WEIGHT
    val unitLabel = when (weightUnit) {
        WeightUnit.KG -> stringResource(R.string.unit_kg)
        WeightUnit.LBS -> stringResource(R.string.unit_lbs)
    }

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
    val midValue = minValue + (valueRange / 2.0)
    val latestValue = values.lastOrNull() ?: 0.0
    val firstValue = values.firstOrNull() ?: 0.0
    val progressDelta = latestValue - firstValue

    val fillColor = primaryColor.copy(alpha = 0.15f)
    val lineColor = primaryColor
    val guideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val latestPointColor = MaterialTheme.colorScheme.tertiary

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgressionSummaryChip(
                label = stringResource(R.string.profile_chart_best),
                value = formatProgressValue(maxValue, isWeightBased, weightUnit, unitLabel)
            )
            ProgressionSummaryChip(
                label = stringResource(R.string.profile_chart_latest),
                value = formatProgressValue(latestValue, isWeightBased, weightUnit, unitLabel)
            )
            ProgressionSummaryChip(
                label = stringResource(R.string.profile_chart_progress),
                value = formatProgressDelta(progressDelta, isWeightBased, weightUnit, unitLabel)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                ChartScaleLabel(
                    text = formatProgressValue(maxValue, isWeightBased, weightUnit, unitLabel),
                    emphasis = true
                )
                ChartScaleLabel(
                    text = formatProgressValue(midValue, isWeightBased, weightUnit, unitLabel)
                )
                ChartScaleLabel(
                    text = formatProgressValue(minValue, isWeightBased, weightUnit, unitLabel)
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .fillMaxHeight()
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

                val baselineValues = listOf(minValue, midValue, maxValue)
                baselineValues.forEach { baseline ->
                    val y = yAt(baseline)
                    drawLine(
                        color = guideColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                values.forEachIndexed { i, v ->
                    val x = xAt(i)
                    val y = yAt(v)
                    if (i == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevX = xAt(i - 1)
                        val prevY = yAt(values[i - 1])
                        val cpX = (prevX + x) / 2f
                        path.cubicTo(cpX, prevY, cpX, y, x, y)
                        fillPath.cubicTo(cpX, prevY, cpX, y, x, y)
                    }
                }

                fillPath.lineTo(xAt(pointCount - 1), h)
                fillPath.close()

                drawPath(path = fillPath, color = fillColor)
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                values.forEachIndexed { i, v ->
                    val isLatest = i == values.lastIndex
                    drawCircle(
                        color = if (isLatest) latestPointColor else lineColor,
                        radius = if (isLatest) 6.dp.toPx() else 4.dp.toPx(),
                        center = Offset(xAt(i), yAt(v))
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isLatest) 3.dp.toPx() else 2.dp.toPx(),
                        center = Offset(xAt(i), yAt(v))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayPoints = if (dataPoints.size > 6) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(
                    R.string.profile_chart_range_start,
                    dataPoints.first().date.format(FULL_DATE)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = stringResource(
                    R.string.profile_chart_range_end,
                    dataPoints.last().date.format(FULL_DATE)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ProgressionSummaryChip(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ChartScaleLabel(
    text: String,
    emphasis: Boolean = false
) {
    Text(
        text = text,
        style = if (emphasis) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
        fontWeight = if (emphasis) FontWeight.SemiBold else FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End
    )
}

private fun formatProgressValue(
    value: Double,
    isWeightBased: Boolean,
    weightUnit: WeightUnit,
    unitLabel: String
): String {
    return if (isWeightBased) {
        "${WeightFormatters.formatWeight(value, weightUnit)} $unitLabel"
    } else {
        value.toInt().toString()
    }
}

private fun formatProgressDelta(
    value: Double,
    isWeightBased: Boolean,
    weightUnit: WeightUnit,
    unitLabel: String
): String {
    val prefix = when {
        value > 0 -> "+"
        value < 0 -> "-"
        else -> ""
    }
    val absolute = abs(value)
    return if (isWeightBased) {
        "$prefix${WeightFormatters.formatWeight(absolute, weightUnit)} $unitLabel"
    } else {
        "$prefix${absolute.toInt()}"
    }
}

// ── PR History list ───────────────────────────────────────────────────────────

@Composable
private fun PrHistoryList(
    records: List<PrRecord>,
    weightUnit: WeightUnit
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
private fun PrHistoryRow(record: PrRecord, weightUnit: WeightUnit, rank: Int) {
    val unitLabel = when (weightUnit) {
        WeightUnit.KG -> stringResource(R.string.unit_kg)
        WeightUnit.LBS -> stringResource(R.string.unit_lbs)
    }
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
                    text = "${WeightFormatters.formatWeight(record.weight, weightUnit)} $unitLabel × ${record.reps}",
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

