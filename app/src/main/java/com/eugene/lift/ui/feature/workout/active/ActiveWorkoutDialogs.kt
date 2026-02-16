package com.eugene.lift.ui.feature.workout.active

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.eugene.lift.R

@Composable
fun ActiveSaveAsTemplateDialog(show: Boolean, onDismiss: () -> Unit, onSave: () -> Unit, onSkip: () -> Unit) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_as_template_title)) },
        text = { Text(stringResource(R.string.save_as_template_message)) },
        confirmButton = { TextButton(onClick = onSave) { Text(stringResource(R.string.save_as_template_confirm)) } },
        dismissButton = { TextButton(onClick = onSkip) { Text(stringResource(R.string.save_as_template_decline)) } }
    )
}

@Composable
fun ActiveUpdateTemplateDialog(show: Boolean, onDismiss: () -> Unit, onUpdate: () -> Unit, onKeep: () -> Unit) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_template_title)) },
        text = { Text(stringResource(R.string.update_template_message)) },
        confirmButton = { TextButton(onClick = onUpdate) { Text(stringResource(R.string.update_template_confirm)) } },
        dismissButton = { TextButton(onClick = onKeep) { Text(stringResource(R.string.update_template_decline)) } }
    )
}

@Composable
fun ActiveExitWorkoutDialog(show: Boolean, onDismiss: () -> Unit, onConfirmExit: () -> Unit) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exit_workout_title)) },
        text = { Text(stringResource(R.string.exit_workout_message)) },
        confirmButton = { TextButton(onClick = onConfirmExit) { Text(stringResource(R.string.exit_workout_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.exit_workout_cancel)) } }
    )
}
