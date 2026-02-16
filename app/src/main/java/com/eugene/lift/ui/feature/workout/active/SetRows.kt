package com.eugene.lift.ui.feature.workout.active

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eugene.lift.R
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.WorkoutSet

@Composable
fun SetRowItem(
    set: WorkoutSet,
    context: SetRowContext,
    callbacks: SetRowCallbacks
) {
    val rowBackground = if (set.completed)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = context.setNumber.toString(),
            modifier = Modifier.width(32.dp),
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

        IconButton(onClick = callbacks.onCompleted, modifier = Modifier.width(48.dp)) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
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
            value = if (set.weight > 0) formatWeight(set.weight) else "",
            onValueChange = callbacks.onWeightChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        if (historyDisplayWeight != null) HistoryText("${formatWeight(historyDisplayWeight)} ${context.weightUnitLabel}")
    }
    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
        CompactNumberInput(
            value = if (set.reps > 0) set.reps.toString() else "",
            onValueChange = callbacks.onRepsChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        context.historySet?.reps?.let { HistoryText("$it reps") }
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
        context.historySet?.reps?.let { HistoryText("$it reps", Modifier.align(Alignment.CenterHorizontally)) }
    }
}

@Composable
private fun RowScope.DistanceTimeRow(set: WorkoutSet, context: SetRowContext, callbacks: SetRowCallbacks) {
    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
        val distanceValue = set.distance ?: 0.0
        CompactDecimalInput(
            value = if (distanceValue > 0) formatWeight(distanceValue) else "",
            onValueChange = callbacks.onDistanceChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        context.historySet?.let { hist ->
            val distUnitLabel = if (context.userSettings.distanceUnit == DistanceUnit.KM) "km" else "mi"
            HistoryText("${hist.distance?.let(::formatWeight) ?: "-"} $distUnitLabel")
        }
    }
    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
        CompactNumberInput(
            value = set.timeSeconds?.takeIf { it > 0 }?.toString() ?: "",
            onValueChange = callbacks.onTimeChange,
            placeholder = { Text("0") },
            enabled = !set.completed
        )
        context.historySet?.timeSeconds?.let { HistoryText("$it s") }
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
    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
        if (context.effortMetric == "RPE") {
            CompactDecimalInput(
                value = set.rpe?.toString() ?: "",
                onValueChange = callbacks.onRpeChange,
                placeholder = { Text("-") },
                enabled = !set.completed
            )
        } else {
            CompactNumberInput(
                value = set.rir?.toString() ?: "",
                onValueChange = callbacks.onRirChange,
                placeholder = { Text("-") },
                enabled = !set.completed
            )
        }
        context.historySet?.let { hist ->
            val histVal = if (context.effortMetric == "RPE") hist.rpe else hist.rir
            histVal?.let { HistoryText("$it") }
        }
    }
}

data class ExerciseCallbacks(
    val onWeightChange: (setIndex: Int, value: String) -> Unit,
    val onRepsChange: (setIndex: Int, value: String) -> Unit,
    val onDistanceChange: (setIndex: Int, value: String) -> Unit,
    val onTimeChange: (setIndex: Int, value: String) -> Unit,
    val onRpeChange: (setIndex: Int, value: String) -> Unit,
    val onRirChange: (setIndex: Int, value: String) -> Unit,
    val onSetCompleted: (setIndex: Int) -> Unit,
    val onAddSet: () -> Unit,
    val onRemoveSet: (setIndex: Int) -> Unit,
    val onExerciseClick: () -> Unit,
    val onExerciseNoteChange: (String) -> Unit
)

data class SetRowCallbacks(
    val onWeightChange: (String) -> Unit,
    val onRepsChange: (String) -> Unit,
    val onDistanceChange: (String) -> Unit,
    val onTimeChange: (String) -> Unit,
    val onRpeChange: (String) -> Unit,
    val onRirChange: (String) -> Unit,
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
