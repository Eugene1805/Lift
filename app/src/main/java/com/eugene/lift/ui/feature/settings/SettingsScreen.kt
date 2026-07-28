package com.eugene.lift.ui.feature.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.ui.components.AppDropdown
import com.eugene.lift.ui.theme.liftThemeSpecFor
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val contactEmail = stringResource(R.string.setting_contact_email)
    val contactSubject = stringResource(R.string.setting_email_subject)
    val contactChooserTitle = stringResource(R.string.setting_email_chooser_title)
    val exportSuccess = stringResource(R.string.settings_export_success)
    val exportFailure = stringResource(R.string.settings_export_failure)
    val importSuccess = stringResource(R.string.settings_import_success)
    val importFailure = stringResource(R.string.settings_import_failure)
    val importInvalid = stringResource(R.string.settings_import_invalid)
    val importConfirmTitle = stringResource(R.string.settings_import_confirm_title)
    val importConfirmMessage = stringResource(R.string.settings_import_confirm_message)
    val importConfirm = stringResource(R.string.settings_import_confirm)
    val importCancel = stringResource(R.string.settings_import_cancel)
    var pendingImportJson by androidx.compose.runtime.remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val json = viewModel.exportBackupJson()
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                    ?: error("Unable to open export destination")
            }.onSuccess {
                Toast.makeText(context, exportSuccess, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, exportFailure, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Unable to open import source")
            }.onSuccess { json ->
                pendingImportJson = json
            }.onFailure {
                Toast.makeText(context, importFailure, Toast.LENGTH_SHORT).show()
            }
        }
    }

    pendingImportJson?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingImportJson = null },
            title = { Text(importConfirmTitle) },
            text = { Text(importConfirmMessage) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        pendingImportJson = null
                        scope.launch {
                            when (val result = viewModel.importBackupJson(json)) {
                                is AppResult.Success -> {
                                    Toast.makeText(context, importSuccess, Toast.LENGTH_SHORT).show()
                                    (context as? Activity)?.recreate()
                                }
                                is AppResult.Error -> {
                                    val errorMessage = when (result.error) {
                                        AppError.Validation -> importInvalid
                                        else -> importFailure
                                    }
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) { Text(importConfirm) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { pendingImportJson = null }
                ) { Text(importCancel) }
            }
        )
    }

    SettingsScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                SettingsUiEvent.ContactUsClicked -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:$contactEmail".toUri()
                        putExtra(Intent.EXTRA_SUBJECT, contactSubject)
                    }
                    context.startActivity(Intent.createChooser(intent, contactChooserTitle))
                }
                SettingsUiEvent.ExportDataClicked -> {
                    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
                    exportLauncher.launch("lift-backup-$timestamp.json")
                }
                SettingsUiEvent.ImportDataClicked -> {
                    importLauncher.launch(arrayOf("application/json", "text/plain"))
                }
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val packageInfo = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    val versionName = packageInfo.versionName.orEmpty()
    val versionCode = remember(packageInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            SettingsSection(title = stringResource(R.string.section_appearance)) {
                ThemeSelector(
                    selectedTheme = uiState.theme,
                    onThemeSelected = { onEvent(SettingsUiEvent.ThemeChanged(it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                val languages = listOf(
                    "en" to stringResource(R.string.language_english),
                    "es" to stringResource(R.string.language_spanish)
                )
                val currentLangPair = languages.find { it.first == uiState.languageCode } ?: languages.first()

                AppDropdown(
                    label = stringResource(R.string.label_language),
                    options = languages,
                    selectedOption = currentLangPair,
                    onOptionSelected = { pair ->
                        onEvent(SettingsUiEvent.LanguageChanged(pair.first))
                        // Recreate the Activity so attachBaseContext applies the new locale
                        // to ALL windows (including DropdownMenus, dialogs, etc.)
                        (context as? Activity)?.recreate()
                    },
                    labelProvider = { it.second }
                )
            }

            HorizontalDivider()

            SettingsSection(title = stringResource(R.string.section_units)) {
                val segmentedButtonColors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    activeBorderColor = MaterialTheme.colorScheme.outline,
                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline
                )
                // Weight Unit
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.label_weight),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WeightUnit.entries.forEachIndexed { index, unit ->
                            SegmentedButton(
                                selected = uiState.weightUnit == unit,
                                onClick = { onEvent(SettingsUiEvent.WeightUnitChanged(unit)) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = WeightUnit.entries.size
                                ),
                                colors = segmentedButtonColors
                            ) {
                                Text(unit.name)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Distance Unit
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.label_distance),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DistanceUnit.entries.forEachIndexed { index, unit ->
                            SegmentedButton(
                                selected = uiState.distanceUnit == unit,
                                onClick = { onEvent(SettingsUiEvent.DistanceUnitChanged(unit)) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = DistanceUnit.entries.size
                                )
                                ,
                                colors = segmentedButtonColors
                            ) {
                                Text(
                                    when (unit) {
                                        DistanceUnit.KM -> stringResource(R.string.unit_km)
                                        DistanceUnit.MILES -> stringResource(R.string.unit_miles)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // ── Workout Section ──────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.section_workout)) {

                // Effort Metric picker
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_effort_metric_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.settings_effort_metric_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val segmentedButtonColors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        activeBorderColor = MaterialTheme.colorScheme.outline,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveBorderColor = MaterialTheme.colorScheme.outline
                    )
                    val options = listOf("RPE", "RIR", null)
                    val labels = listOf(
                        stringResource(R.string.active_workout_use_rpe),
                        stringResource(R.string.active_workout_use_rir),
                        stringResource(R.string.active_workout_hide_metric)
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        options.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = uiState.effortMetric == option,
                                onClick = { onEvent(SettingsUiEvent.EffortMetricChanged(option)) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index, count = options.size),
                                colors = segmentedButtonColors
                            ) {
                                Text(labels[index], style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto-timer toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.active_workout_auto_timer),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.settings_auto_timer_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.autoTimerEnabled,
                        onCheckedChange = { onEvent(SettingsUiEvent.AutoTimerToggled(it)) }
                    )
                }
            }

            HorizontalDivider()

            SettingsSection(title = stringResource(R.string.section_data)) {
                SettingsActionItem(
                    icon = Icons.Default.FileDownload,
                    title = stringResource(R.string.settings_export_title),
                    subtitle = stringResource(R.string.settings_export_subtitle),
                    onClick = { onEvent(SettingsUiEvent.ExportDataClicked) }
                )

                SettingsActionItem(
                    icon = Icons.Default.FileUpload,
                    title = stringResource(R.string.settings_import_title),
                    subtitle = stringResource(R.string.settings_import_subtitle),
                    onClick = { onEvent(SettingsUiEvent.ImportDataClicked) }
                )
            }

            HorizontalDivider()

            SettingsSection(title = stringResource(R.string.section_about)) {
                SettingsActionItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.setting_version_label),
                    subtitle = stringResource(
                        R.string.setting_version_value,
                        versionName,
                        versionCode
                    ),
                    onClick = { /* Easter egg? */ }
                )

                SettingsActionItem(
                    icon = Icons.Default.Mail,
                    title = stringResource(R.string.btn_contact_us),
                    subtitle = stringResource(R.string.setting_contact_email),
                    onClick = { onEvent(SettingsUiEvent.ContactUsClicked) }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Text(
        text = stringResource(R.string.label_theme),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = stringResource(R.string.theme_selector_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
    )

    AppTheme.entries.chunked(2).forEach { rowThemes ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rowThemes.forEach { theme ->
                ThemeOptionCard(
                    theme = theme,
                    selected = selectedTheme == theme,
                    onClick = { onThemeSelected(theme) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (rowThemes.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ThemeOptionCard(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = liftThemeSpecFor(theme)
    Card(
        modifier = modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.RadioButton
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(
                            width = 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                        .background(
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                            shape = CircleShape
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
                            .background(
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    Color.Transparent
                                },
                                shape = CircleShape
                            )
                    )
                }
                Text(
                    text = stringResource(spec.nameRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                spec.previewColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color = color, shape = CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
