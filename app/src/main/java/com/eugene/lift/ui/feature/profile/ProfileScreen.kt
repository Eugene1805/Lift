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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        onEditProfileClick = onEditProfileClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onTimeRangeChange: (TimeRange) -> Unit,
    onEditProfileClick: () -> Unit
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

                // Exercise Progression Section (placeholder for future)
                item {
                    ExerciseProgressionSection()
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
                    // TODO: Load image with Coil
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
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
                        text = "${profile?.totalWorkouts ?: 0} workouts",
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
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Duration", "Volume", "Reps")

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
                    text = "Statistics",
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

            // Histogram
            val data = when (selectedTabIndex) {
                0 -> stats.durationData
                1 -> stats.volumeData
                else -> stats.repsData
            }

            val unit = when (selectedTabIndex) {
                0 -> "min"
                1 -> "kg"
                else -> "reps"
            }

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                SimpleBarChart(
                    data = data,
                    unit = unit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        }
    }
}

@Composable
private fun TimeRangeDropdown(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(
                text = when (selectedRange) {
                    TimeRange.WEEK -> "This Week"
                    TimeRange.MONTH -> "Last Month"
                    TimeRange.THREE_MONTHS -> "3 Months"
                    TimeRange.SIX_MONTHS -> "6 Months"
                    TimeRange.YEAR -> "1 Year"
                    TimeRange.ALL_TIME -> "All Time"
                }
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TimeRange.entries.forEach { range ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (range) {
                                TimeRange.WEEK -> "This Week"
                                TimeRange.MONTH -> "Last Month"
                                TimeRange.THREE_MONTHS -> "3 Months"
                                TimeRange.SIX_MONTHS -> "6 Months"
                                TimeRange.YEAR -> "1 Year"
                                TimeRange.ALL_TIME -> "All Time"
                            }
                        )
                    },
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
    unit: String,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0

    Column(modifier = modifier) {
        // Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.takeLast(8).forEach { point ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    val barHeight = if (maxValue > 0) (point.value / maxValue) else 0.0

                    Text(
                        text = point.value.toInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .width(24.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.takeLast(8).forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DashboardSection(stats: ProfileStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Workouts per Week",
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
                    text = "Average",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%.1f workouts/week".format(stats.averageWorkoutsPerWeek),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (stats.workoutsPerWeek.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                SimpleBarChart(
                    data = stats.workoutsPerWeek,
                    unit = "workouts",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        }
    }
}

@Composable
private fun ExerciseProgressionSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercise Progression",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { /* TODO: Add exercise diagram */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add exercise chart",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap + to track an exercise's progression over time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ProfileSkeletonLoader(modifier: Modifier = Modifier) {
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

