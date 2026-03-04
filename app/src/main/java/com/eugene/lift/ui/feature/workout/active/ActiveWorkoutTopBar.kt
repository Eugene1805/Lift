package com.eugene.lift.ui.feature.workout.active

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eugene.lift.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTopBar(
    uiState: ActiveWorkoutUiState,
    formattedTime: String,
    onExit: () -> Unit,
    onMetricChange: (String?) -> Unit,
    onToggleAutoTimer: () -> Unit,
    onToggleReorderMode: () -> Unit,
    onFinish: (Boolean?) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.active_workout_training), style = MaterialTheme.typography.labelSmall)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = uiState.sessionName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    ) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onExit) { Icon(Icons.Default.Close, stringResource(R.string.cd_close)) }
        },
        actions = {
            IconButton(onClick = onToggleReorderMode) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_reorder_exercise))
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.active_workout_configuration))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.active_workout_use_rpe)) },
                    onClick = { onMetricChange("RPE"); showMenu = false },
                    trailingIcon = { if (uiState.effortMetric == "RPE") Icon(Icons.Default.Check, stringResource(R.string.cd_check_selected)) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.active_workout_use_rir)) },
                    onClick = { onMetricChange("RIR"); showMenu = false },
                    trailingIcon = { if (uiState.effortMetric == "RIR") Icon(Icons.Default.Check, stringResource(R.string.cd_check_selected)) }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.active_workout_hide_metric)) },
                    onClick = { onMetricChange(null); showMenu = false },
                    trailingIcon = { if (uiState.effortMetric == null) Icon(Icons.Default.Check, stringResource(R.string.cd_check_selected)) }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.active_workout_auto_timer)) },
                    onClick = { onToggleAutoTimer() },
                    trailingIcon = { Switch(checked = uiState.isAutoTimerEnabled, onCheckedChange = null) }
                )
            }
            Button(onClick = { onFinish(null) }) { Text(stringResource(R.string.active_workout_finish)) }
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
