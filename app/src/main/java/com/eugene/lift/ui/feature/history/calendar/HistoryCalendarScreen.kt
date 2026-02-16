package com.eugene.lift.ui.feature.history.calendar

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
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.ui.feature.history.HistoryUiItem
import com.eugene.lift.ui.feature.history.HistoryViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter

@Composable
fun HistoryCalendarRoute(
    onNavigateBack: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val workoutDays = remember(historyItems) {
        historyItems.filterIsInstance<HistoryUiItem.SessionItem>()
            .map { it.session.date.toLocalDate() }
            .toSet()
    }

    HistoryCalendarScreen(
        workoutDays = workoutDays,
        onNavigateBack = onNavigateBack,
        onDateClick = onDateClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCalendarScreen(
    workoutDays: Set<LocalDate>,
    onNavigateBack: () -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    val months = remember(workoutDays) { buildMonthRange(workoutDays) }
    val streakWeeks = remember(workoutDays) { calculateWeekStreak(workoutDays, LocalDate.now()) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_calendar_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (months.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.history_calendar_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WeekStreakCard(streakWeeks = streakWeeks)
            }
            items(months, key = { it.toString() }) { month ->
                MonthCard(
                    month = month,
                    workoutDays = workoutDays,
                    onDateClick = onDateClick
                )
            }
        }
    }
}

@Composable
private fun WeekStreakCard(streakWeeks: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.history_calendar_streak_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = stringResource(R.string.history_calendar_streak_weeks, streakWeeks),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MonthCard(
    month: YearMonth,
    workoutDays: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            CalendarGrid(
                month = month,
                workoutDays = workoutDays,
                onDateClick = onDateClick
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    workoutDays: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value // Rest one so Monday=1, otherwise it stars on Sunday

    val cells = buildList(daysInMonth + startOffset) {
        repeat(startOffset) { add(null) }
        for (day in 1..daysInMonth) {
            add(month.atDay(day))
        }
    }

    val rows = cells.chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { date ->
                    DayCell(
                        date = date,
                        isWorkoutDay = date != null && workoutDays.contains(date),
                        onDateClick = onDateClick
                    )
                }
                repeat(7 - week.size) {
                    DayCell(date = null, isWorkoutDay = false, onDateClick = onDateClick)
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    isWorkoutDay: Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    val size = 36.dp
    Box(
        modifier = Modifier
            .size(size)
            .clickable(enabled = date != null) { date?.let(onDateClick) },
        contentAlignment = Alignment.Center
    ) {
        if (date == null) {
            Spacer(modifier = Modifier.size(size))
            return
        }

        if (isWorkoutDay) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(size)
            ) {}
        }

        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isWorkoutDay) FontWeight.Bold else FontWeight.Normal,
            color = if (isWorkoutDay) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun buildMonthRange(workoutDays: Set<LocalDate>): List<YearMonth> {
    if (workoutDays.isEmpty()) return emptyList()

    val minDate = workoutDays.minOrNull() ?: return emptyList()
    val maxDate = workoutDays.maxOrNull() ?: return emptyList()

    var cursor = YearMonth.from(maxDate)
    val start = YearMonth.from(minDate)
    val result = mutableListOf<YearMonth>()
    while (cursor >= start) {
        result.add(cursor)
        cursor = cursor.minusMonths(1)
    }
    return result
}

private fun calculateWeekStreak(workoutDays: Set<LocalDate>, today: LocalDate): Int {
    if (workoutDays.isEmpty()) return 0

    val weekStarts = workoutDays
        .map { it.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
        .toSet()

    val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    var cursor = if (weekStarts.contains(currentWeekStart)) {
        currentWeekStart
    } else {
        weekStarts.maxOrNull() ?: return 0
    }

    var streak = 0
    while (weekStarts.contains(cursor)) {
        streak += 1
        cursor = cursor.minusWeeks(1)
    }
    return streak
}
