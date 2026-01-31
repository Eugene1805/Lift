package com.eugene.lift.ui.feature.settings

import android.content.Intent
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
import androidx.compose.foundation.layout.width
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.ui.AppDropdown
import androidx.core.net.toUri

@Composable
fun SettingsRoute() {
    SettingsScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
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
                    selectedOption = settings.theme,
                    onOptionSelected = { viewModel.updateTheme(it) },
                    labelProvider = { theme ->
                        when (theme) {
                            AppTheme.LIGHT -> stringResource(R.string.theme_light)
                            AppTheme.DARK -> stringResource(R.string.theme_dark)
                            AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                val languages = listOf("en" to stringResource(R.string.language_english), "es" to stringResource(R.string.language_spanish))
                val currentLangCode = viewModel.getCurrentLanguageCode()
                val currentLangPair = languages.find { it.first == currentLangCode } ?: languages.first()

                AppDropdown(
                    label = stringResource(R.string.label_language),
                    options = languages,
                    selectedOption = currentLangPair,
                    onOptionSelected = { pair -> viewModel.updateLanguage(pair.first) },
                    labelProvider = { it.second }
                )
            }

            HorizontalDivider()

            SettingsSection(title = stringResource(R.string.section_units)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppDropdown(
                            label = stringResource(R.string.label_weight),
                            options = WeightUnit.entries,
                            selectedOption = settings.weightUnit,
                            onOptionSelected = { viewModel.updateWeightUnit(it) },
                            labelProvider = { it.name } // TODO: Use string resources
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AppDropdown(
                            label = stringResource(R.string.label_distance),
                            options = DistanceUnit.entries,
                            selectedOption = settings.distanceUnit,
                            onOptionSelected = { viewModel.updateDistanceUnit(it) },
                            labelProvider = { unit ->
                                when (unit) {
                                    DistanceUnit.KM -> stringResource(R.string.unit_km)
                                    DistanceUnit.MILES -> stringResource(R.string.unit_miles)
                                }
                            }
                        )
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
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:${context.getString(R.string.setting_contact_email)}".toUri()
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.setting_email_subject))
                        }
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.setting_email_chooser_title)))
                    }
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
            color = MaterialTheme.colorScheme.primary
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
