package com.eugene.lift.ui.feature.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.ui.components.AppDropdown

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val contactEmail = stringResource(R.string.setting_contact_email)
    val contactSubject = stringResource(R.string.setting_email_subject)
    val contactChooserTitle = stringResource(R.string.setting_email_chooser_title)

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

                AppDropdown(
                    label = stringResource(R.string.label_theme),
                    options = AppTheme.entries,
                    selectedOption = uiState.theme,
                    onOptionSelected = { onEvent(SettingsUiEvent.ThemeChanged(it)) },
                    labelProvider = { theme ->
                        when (theme) {
                            AppTheme.LIGHT -> stringResource(R.string.theme_light)
                            AppTheme.DARK -> stringResource(R.string.theme_dark)
                            AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                        }
                    }
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
                    onOptionSelected = { pair -> onEvent(SettingsUiEvent.LanguageChanged(pair.first)) },
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

            SettingsSection(title = stringResource(R.string.section_about)) {
                SettingsActionItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.setting_version),
                    subtitle = stringResource(R.string.app_name),
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
