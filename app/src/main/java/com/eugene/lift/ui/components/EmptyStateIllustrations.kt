package com.eugene.lift.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eugene.lift.R

// ── Shared layout wrapper ─────────────────────────────────────────────────────

@Composable
private fun EmptyStateLayout(
    illustration: @Composable () -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        illustration()
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── Workout empty state ───────────────────────────────────────────────────────

/**
 * Empty state for the Workout / Templates screen.
 * Draws a dumbbell with a "+" callout bubble.
 */
@Composable
fun WorkoutEmptyState(modifier: Modifier = Modifier) {
    EmptyStateLayout(
        modifier = modifier,
        title = stringResource(R.string.empty_state_workout_title),
        subtitle = stringResource(R.string.empty_state_workout_subtitle),
        illustration = { DumbbellPlusIllustration() }
    )
}

@Composable
private fun DumbbellPlusIllustration() {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surfaceContainerHighest

    Canvas(modifier = Modifier.size(140.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        val barRadius = 8.dp.toPx()
        val plateR = 26.dp.toPx()
        val plateW = 14.dp.toPx()
        val barLen = size.width * 0.46f

        // Shadow
        drawOval(
            color = primary.copy(alpha = 0.07f),
            topLeft = Offset(cx - barLen * 0.7f, cy + plateR - 6.dp.toPx()),
            size = Size(barLen * 1.4f, 12.dp.toPx())
        )

        // Bar
        drawRoundRect(
            color = primaryContainer,
            topLeft = Offset(cx - barLen / 2, cy - barRadius),
            size = Size(barLen, barRadius * 2),
            cornerRadius = CornerRadius(barRadius)
        )
        // Left plate
        drawRoundRect(primary,
            topLeft = Offset(cx - barLen / 2 - plateW, cy - plateR),
            size = Size(plateW, plateR * 2),
            cornerRadius = CornerRadius(5.dp.toPx())
        )
        drawRoundRect(primary.copy(alpha = 0.55f),
            topLeft = Offset(cx - barLen / 2 - plateW * 1.7f, cy - plateR * 0.78f),
            size = Size(plateW * 0.65f, plateR * 1.56f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        // Right plate
        drawRoundRect(primary,
            topLeft = Offset(cx + barLen / 2, cy - plateR),
            size = Size(plateW, plateR * 2),
            cornerRadius = CornerRadius(5.dp.toPx())
        )

        // + bubble
        drawCircle(surface, radius = 18.dp.toPx(), center = Offset(cx + 38.dp.toPx(), cy - 30.dp.toPx()))
        drawCircle(primary.copy(alpha = 0.15f), radius = 18.dp.toPx(), center = Offset(cx + 38.dp.toPx(), cy - 30.dp.toPx()))
        val bx = cx + 38.dp.toPx(); val by = cy - 30.dp.toPx()
        val arm = 7.dp.toPx(); val thick = 3.dp.toPx()
        drawLine(primary, Offset(bx - arm, by), Offset(bx + arm, by), strokeWidth = thick, cap = StrokeCap.Round)
        drawLine(primary, Offset(bx, by - arm), Offset(bx, by + arm), strokeWidth = thick, cap = StrokeCap.Round)
    }
}

// ── History empty state ───────────────────────────────────────────────────────

/**
 * Empty state for the History screen.
 * Draws a simple calendar with a flame accent.
 */
@Composable
fun HistoryEmptyState(modifier: Modifier = Modifier) {
    EmptyStateLayout(
        modifier = modifier,
        title = stringResource(R.string.empty_state_history_title),
        subtitle = stringResource(R.string.empty_state_history_subtitle),
        illustration = { CalendarIllustration() }
    )
}

@Composable
private fun CalendarIllustration() {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surfaceContainerHighest

    Canvas(modifier = Modifier.size(140.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val calW = size.width * 0.72f
        val calH = size.height * 0.62f
        val calL = cx - calW / 2f
        val calT = cy - calH / 2f + 8.dp.toPx()
        val corner = 10.dp.toPx()

        // Calendar body
        drawRoundRect(
            primaryContainer,
            topLeft = Offset(calL, calT),
            size = Size(calW, calH),
            cornerRadius = CornerRadius(corner)
        )

        // Header strip
        drawRoundRect(
            primary,
            topLeft = Offset(calL, calT),
            size = Size(calW, 20.dp.toPx()),
            cornerRadius = CornerRadius(corner)
        )
        // Header notch fix (straight bottom of header)
        drawRect(
            primary,
            topLeft = Offset(calL, calT + 10.dp.toPx()),
            size = Size(calW, 10.dp.toPx())
        )

        // Ring hooks
        drawCircle(surface, radius = 4.dp.toPx(), center = Offset(calL + calW * 0.3f, calT))
        drawCircle(surface, radius = 4.dp.toPx(), center = Offset(calL + calW * 0.7f, calT))

        // Grid dots (representing days)
        val dotR = 3.dp.toPx()
        val gridStartX = calL + 12.dp.toPx()
        val gridStartY = calT + 28.dp.toPx()
        val gapX = (calW - 24.dp.toPx()) / 5f
        val gapY = (calH - 35.dp.toPx()) / 2.5f

        for (row in 0..2) {
            for (col in 0..5) {
                val dotX = gridStartX + col * gapX
                val dotY = gridStartY + row * gapY
                // Highlight one "streak" dot
                val isHighlight = (row == 1 && col == 2)
                drawCircle(
                    if (isHighlight) tertiary else primary.copy(alpha = 0.25f),
                    radius = if (isHighlight) dotR * 1.6f else dotR,
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}

// ── Exercises empty state ─────────────────────────────────────────────────────

/**
 * Empty state shown when exercise search returns no results.
 * Draws a magnifying glass with a dumbbell inside it.
 */
@Composable
fun ExercisesEmptyState(modifier: Modifier = Modifier) {
    EmptyStateLayout(
        modifier = modifier,
        title = stringResource(R.string.empty_state_exercises_title),
        subtitle = stringResource(R.string.empty_state_exercises_subtitle),
        illustration = { SearchExerciseIllustration() }
    )
}

@Composable
private fun SearchExerciseIllustration() {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    Canvas(modifier = Modifier.size(140.dp)) {
        val cx = size.width * 0.42f
        val cy = size.height * 0.42f
        val glassR = 45.dp.toPx()
        val strokeW = 8.dp.toPx()

        // Glass circle
        drawCircle(
            primaryContainer,
            radius = glassR,
            center = Offset(cx, cy)
        )
        drawCircle(
            primary,
            radius = glassR,
            style = Stroke(width = strokeW),
            center = Offset(cx, cy)
        )

        // Handle
        val handleStart = Offset(cx + glassR * 0.68f, cy + glassR * 0.68f)
        val handleEnd = Offset(cx + glassR * 1.35f, cy + glassR * 1.35f)
        drawLine(primary, handleStart, handleEnd, strokeWidth = strokeW * 1.1f, cap = StrokeCap.Round)

        // Mini dumbbell inside glass
        val barLen = 24.dp.toPx()
        val barR = 4.dp.toPx()
        val plateR = 9.dp.toPx()
        val plateW = 6.dp.toPx()
        drawRoundRect(primaryContainer.copy(alpha = 0.0f), topLeft = Offset(cx - barLen / 2, cy - barR), size = Size(barLen, barR * 2), cornerRadius = CornerRadius(barR))
        drawRoundRect(primary.copy(alpha = 0.8f), topLeft = Offset(cx - barLen / 2, cy - barR), size = Size(barLen, barR * 2), cornerRadius = CornerRadius(barR))
        drawRoundRect(primary, topLeft = Offset(cx - barLen / 2 - plateW, cy - plateR), size = Size(plateW, plateR * 2), cornerRadius = CornerRadius(3.dp.toPx()))
        drawRoundRect(primary, topLeft = Offset(cx + barLen / 2, cy - plateR), size = Size(plateW, plateR * 2), cornerRadius = CornerRadius(3.dp.toPx()))
    }
}
