package com.eugene.lift.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eugene.lift.R
import com.eugene.lift.domain.model.UserProfile

@Composable
fun ProfileRoute(
    onEditProfileClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        onTimeRangeChange = viewModel::setTimeRange,
        onEditProfileClick = onEditProfileClick,
        onShowExercisePicker = viewModel::showExercisePicker,
        onHideExercisePicker = viewModel::hideExercisePicker,
        onToggleTrackedExercise = viewModel::toggleTrackedExercise,
        onRemoveTrackedExercise = viewModel::removeTrackedExercise
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onTimeRangeChange: (TimeRange) -> Unit,
    onEditProfileClick: () -> Unit,
    onShowExercisePicker: () -> Unit,
    onHideExercisePicker: () -> Unit,
    onToggleTrackedExercise: (String) -> Unit,
    onRemoveTrackedExercise: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_profile)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            ProfileSkeletonLoader(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                item {
                    ProfileHeader(
                        profile = uiState.profile,
                        onEditClick = onEditProfileClick
                    )
                }

                // Histogram Section
                item {
                    HistogramSection(
                        stats = uiState.stats,
                        selectedTimeRange = uiState.selectedTimeRange,
                        onTimeRangeChange = onTimeRangeChange
                    )
                }

                // Dashboard Section
                item {
                    DashboardSection(
                        stats = uiState.stats
                    )
                }

                // Exercise Progression Section
                item {
                    ExerciseProgressionSection(
                        progressions = uiState.progressions,
                        allExercises = uiState.allExercises,
                        trackedIds = uiState.progressions.map { it.exerciseId },
                        showPickerDialog = uiState.showExercisePickerDialog,
                        weightUnit = uiState.weightUnit,
                        onAddClick = onShowExercisePicker,
                        onRemoveClick = onRemoveTrackedExercise,
                        onToggleExercise = onToggleTrackedExercise,
                        onDismissPicker = onHideExercisePicker
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile?,
    onEditClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        color = profile?.avatarColor?.let {
                            try { Color(it.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        } ?: MaterialTheme.colorScheme.primary
                    )
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                if (profile?.avatarUrl != null) {
                    // Load saved avatar with Coil
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profile.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = profile?.displayName?.take(2)?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Stats
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile?.displayName ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "@${profile?.username ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.profile_workouts_count, profile?.totalWorkouts ?: 0),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun HistogramSection(
    stats: ProfileStats,
    selectedTimeRange: TimeRange,
    onTimeRangeChange: (TimeRange) -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.profile_tab_duration),
        stringResource(R.string.profile_tab_volume),
        stringResource(R.string.profile_tab_reps)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_statistics),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TimeRangeDropdown(
                    selectedRange = selectedTimeRange,
                    onRangeSelected = onTimeRangeChange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart dataset is derived from selected tab and stats snapshot.
            val data by remember(selectedTabIndex, stats) {
                derivedStateOf {
                    when (selectedTabIndex) {
                        0 -> stats.durationData
                        1 -> stats.volumeData
                        else -> stats.repsData
                    }
                }
            }

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.profile_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                SimpleBarChart(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        }
    }
}

@Composable
private fun timeRangeLabel(range: TimeRange): String = when (range) {
    TimeRange.WEEK -> stringResource(R.string.profile_time_week)
    TimeRange.MONTH -> stringResource(R.string.profile_time_month)
    TimeRange.THREE_MONTHS -> stringResource(R.string.profile_time_3months)
    TimeRange.SIX_MONTHS -> stringResource(R.string.profile_time_6months)
    TimeRange.YEAR -> stringResource(R.string.profile_time_year)
    TimeRange.ALL_TIME -> stringResource(R.string.profile_time_all)
}

@Composable
private fun TimeRangeDropdown(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(text = timeRangeLabel(selectedRange))
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TimeRange.entries.forEach { range ->
                DropdownMenuItem(
                    text = { Text(timeRangeLabel(range)) },
                    onClick = {
                        onRangeSelected(range)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SimpleBarChart(
    data: List<HistogramDataPoint>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0

    val displayData = data.takeLast(8)

    Column(modifier = modifier) {
        // Bars row — each column takes equal weight so bars align with labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            displayData.forEach { point ->
                val barHeight = if (maxValue > 0) (point.value / maxValue) else 0.0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = point.value.toInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((80 * barHeight).dp.coerceAtLeast(4.dp))
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // X-axis labels — same weight(1f) so they line up with bars above
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            displayData.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DashboardSection(stats: ProfileStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.profile_workouts_per_week),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_average),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.profile_workouts_week_format, stats.averageWorkoutsPerWeek),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (stats.workoutsPerWeek.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                SimpleBarChart(
                    data = stats.workoutsPerWeek,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        }
    }
}


@Composable
fun ProfileSkeletonLoader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile card skeleton
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(20.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    )
                }
            }
        }

        // Stats skeleton
        repeat(2) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}



