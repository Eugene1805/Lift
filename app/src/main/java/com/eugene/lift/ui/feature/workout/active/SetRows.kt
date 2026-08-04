package com.eugene.lift.ui.feature.workout.active

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.stateDescription
import com.eugene.lift.R
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.ui.util.WeightFormatters
import kotlin.math.roundToInt

@Composable
fun SetRowItem(
    set: WorkoutSet,
    context: SetRowContext,
    callbacks: SetRowCallbacks
) {
    val isEvenRow = context.setNumber % 2 == 0
    val defaultRowColor = if (isEvenRow) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.surface
    }

    val rowBackground by animateColorAsState(
        targetValue = if (set.completed)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            defaultRowColor,
        label = "set_row_background"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = context.setNumber.toString(),
            modifier = Modifier
                .width(32.dp)
                .align(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        when (context.measureType) {
            MeasureType.REPS_AND_WEIGHT -> RepsWeightRow(set, context, callbacks)
            MeasureType.REPS_ONLY -> RepsOnlyRow(set, context, callbacks)
            MeasureType.DISTANCE_TIME -> DistanceTimeRow(set, context, callbacks)
            MeasureType.TIME -> TimeRow(set, context, callbacks)
        }

        EffortRow(set, context, callbacks)

        val completedDesc = stringResource(if (set.completed) R.string.cd_set_completed else R.string.cd_set_not_completed)
        IconButton(
            onClick = callbacks.onCompleted,
            modifier = Modifier
                .width(48.dp)
                .semantics { stateDescription = completedDesc }
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = completedDesc,
                tint = if (set.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun RowScope.RepsWeightRow(set: WorkoutSet, context: SetRowContext, callbacks: SetRowCallbacks) {
    val historyDisplayWeight = context.historySet?.weight
    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
        CompactDecimalInput(
            value = if (set.weight > 0) WeightFormatters.formatWeight(set.weight, context.userSettings.weightUnit) else "",
            onValueChange = callbacks.onWeightChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        if (historyDisplayWeight != null) {
            HistoryText("${WeightFormatters.formatWeight(historyDisplayWeight, context.userSettings.weightUnit)} ${context.weightUnitLabel}")
        }
    }
    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
        CompactNumberInput(
            value = if (set.reps > 0) set.reps.toString() else "",
            onValueChange = callbacks.onRepsChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        context.historySet?.reps?.let { HistoryText("$it") }
    }
}

@Composable
private fun RowScope.RepsOnlyRow(set: WorkoutSet, context: SetRowContext, callbacks: SetRowCallbacks) {
    Column(modifier = Modifier.weight(2f).padding(horizontal = 4.dp)) {
        CompactNumberInput(
            value = if (set.reps > 0) set.reps.toString() else "",
            onValueChange = callbacks.onRepsChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        context.historySet?.reps?.let { HistoryText("$it", Modifier.align(Alignment.CenterHorizontally)) }
    }
}

@Composable
private fun RowScope.DistanceTimeRow(set: WorkoutSet, context: SetRowContext, callbacks: SetRowCallbacks) {
    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
        val distanceValue = set.distance ?: 0.0
        CompactDecimalInput(
            value = if (distanceValue > 0) distanceValue.toString() else "",
            onValueChange = callbacks.onDistanceChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        context.historySet?.distance?.let { histDistance ->
            val distUnitLabel = if (context.userSettings.distanceUnit == DistanceUnit.KM)
                stringResource(R.string.unit_km)
            else
                stringResource(R.string.unit_miles)
            HistoryText("$histDistance $distUnitLabel")
        }
    }

    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
        CompactNumberInput(
            value = set.timeSeconds?.takeIf { it > 0 }?.toString() ?: "",
            onValueChange = callbacks.onTimeChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        context.historySet?.timeSeconds?.let { seconds ->
            HistoryText("$seconds ${stringResource(R.string.unit_seconds_short)}")
        }
    }
}

@Composable
private fun RowScope.TimeRow(set: WorkoutSet, context: SetRowContext, callbacks: SetRowCallbacks) {
    Column(modifier = Modifier.weight(2f).padding(horizontal = 4.dp)) {
        CompactNumberInput(
            value = set.timeSeconds?.takeIf { it > 0 }?.toString() ?: "",
            onValueChange = callbacks.onTimeChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        context.historySet?.timeSeconds?.let { HistoryText("$it ${stringResource(R.string.unit_seconds_short)}", Modifier.align(Alignment.CenterHorizontally)) }
    }
}

@Composable
private fun RowScope.EffortRow(set: WorkoutSet, context: SetRowContext, callbacks: SetRowCallbacks) {
    if (context.effortMetric == null) return
    Column(
        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EffortSliderPicker(
            set = set,
            metric = context.effortMetric,
            enabled = !set.completed,
            callbacks = callbacks
        )
        context.historySet?.let { hist ->
            val historyValue = if (context.effortMetric == "RPE") {
                hist.rpe?.let(::formatRpe)
            } else {
                hist.rir?.toString()
            }
            historyValue?.let { HistoryText(it) }
        }
    }
}

@Composable
private fun EffortSliderPicker(
    set: WorkoutSet,
    metric: String,
    enabled: Boolean,
    callbacks: SetRowCallbacks
) {
    val isRpe = metric == "RPE"
    val selectedValue = if (isRpe) set.rpe?.toFloat() else set.rir?.toFloat()
    val defaultValue = if (isRpe) 8f else 2f
    val valueRange = if (isRpe) {
        WorkoutSet.RPE_MIN.toFloat()..WorkoutSet.RPE_MAX.toFloat()
    } else {
        WorkoutSet.RIR_MIN.toFloat()..WorkoutSet.RIR_MAX.toFloat()
    }
    val steps = if (isRpe) 17 else 9
    val normalizedValue = when {
        selectedValue == null -> defaultValue
        isRpe -> ((selectedValue * 2f).roundToInt() / 2f)
            .coerceIn(valueRange.start, valueRange.endInclusive)
        else -> selectedValue.roundToInt().toFloat()
            .coerceIn(valueRange.start, valueRange.endInclusive)
    }
    var expanded by remember(set.id, metric) { mutableStateOf(false) }
    var draftValue by remember(set.id, metric) {
        mutableFloatStateOf(normalizedValue)
    }

    LaunchedEffect(selectedValue) {
        draftValue = normalizedValue
    }

    val displayValue = when {
        selectedValue == null -> "--"
        isRpe -> formatRpe(selectedValue.toDouble())
        else -> selectedValue.roundToInt().toString()
    }
    val accessibleValue = if (selectedValue == null) {
        stringResource(R.string.active_workout_effort_not_set)
    } else {
        displayValue
    }
    val pickerDescription = stringResource(
        R.string.active_workout_effort_picker_description,
        metric,
        accessibleValue
    )

    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (enabled) MaterialTheme.colorScheme.surfaceContainerHigh
                    else MaterialTheme.colorScheme.surfaceContainerLow
                )
                .border(
                    width = 1.dp,
                    color = if (expanded) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(enabled = enabled) { expanded = true }
                .semantics { contentDescription = pickerDescription },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selectedValue == null) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = metric,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRpe) formatRpe(draftValue.toDouble())
                        else draftValue.roundToInt().toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
                Slider(
                    value = draftValue,
                    onValueChange = { value ->
                        draftValue = if (isRpe) {
                            (value * 2f).roundToInt() / 2f
                        } else {
                            value.roundToInt().toFloat()
                        }
                    },
                    onValueChangeFinished = {
                        if (isRpe) {
                            callbacks.onRpeChange(draftValue.toDouble())
                        } else {
                            callbacks.onRirChange(draftValue.roundToInt())
                        }
                    },
                    valueRange = valueRange,
                    steps = steps
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRpe) formatRpe(valueRange.start.toDouble())
                        else valueRange.start.roundToInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        enabled = selectedValue != null,
                        onClick = {
                            draftValue = defaultValue
                            if (isRpe) callbacks.onRpeChange(null) else callbacks.onRirChange(null)
                            expanded = false
                        }
                    ) {
                        Text(stringResource(R.string.active_workout_effort_clear))
                    }
                    Text(
                        text = if (isRpe) formatRpe(valueRange.endInclusive.toDouble())
                        else valueRange.endInclusive.roundToInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatRpe(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
}

data class ExerciseCallbacks(
    val onWeightChange: (setIndex: Int, value: String) -> Unit,
    val onRepsChange: (setIndex: Int, value: String) -> Unit,
    val onDistanceChange: (setIndex: Int, value: String) -> Unit,
    val onTimeChange: (setIndex: Int, value: String) -> Unit,
    val onRpeChange: (setIndex: Int, value: Double?) -> Unit,
    val onRirChange: (setIndex: Int, value: Int?) -> Unit,
    val onSetCompleted: (setIndex: Int) -> Unit,
    val onAddSet: () -> Unit,
    val onRemoveSet: (setIndex: Int) -> Unit,
    val onExerciseClick: () -> Unit,
    val onExerciseNoteChange: (String) -> Unit,
    val onDeleteExercise: () -> Unit,
    val onReplaceExercise: () -> Unit
)

data class SetRowCallbacks(
    val onWeightChange: (String) -> Unit,
    val onRepsChange: (String) -> Unit,
    val onDistanceChange: (String) -> Unit,
    val onTimeChange: (String) -> Unit,
    val onRpeChange: (Double?) -> Unit,
    val onRirChange: (Int?) -> Unit,
    val onCompleted: () -> Unit
)

data class SetRowContext(
    val measureType: MeasureType,
    val setNumber: Int,
    val historySet: WorkoutSet?,
    val effortMetric: String?,
    val weightUnitLabel: String,
    val userSettings: com.eugene.lift.domain.model.UserSettings
)
