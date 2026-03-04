package com.eugene.lift.ui.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.WeightUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingRoute(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val selectedWeightUnit by viewModel.selectedWeightUnit.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToHome -> onComplete()
            }
        }
    }

    OnboardingScreen(
        selectedWeightUnit = selectedWeightUnit,
        onWeightUnitChange = viewModel::setWeightUnit,
        onComplete = viewModel::completeOnboarding
    )
}

@Composable
fun OnboardingScreen(
    selectedWeightUnit: WeightUnit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Skip button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(visible = pagerState.currentPage < 2) {
                    TextButton(onClick = onComplete) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> ProgressPage()
                    2 -> SetupPage(
                        selectedWeightUnit = selectedWeightUnit,
                        onWeightUnitChange = onWeightUnitChange
                    )
                }
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dot indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { i ->
                        val isSelected = pagerState.currentPage == i
                        val dotWidth by animateFloatAsState(
                            targetValue = if (isSelected) 24f else 8f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(dotWidth.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Next / Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage < 2)
                            stringResource(R.string.onboarding_next)
                        else
                            stringResource(R.string.onboarding_get_started),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Page 1: Welcome ───────────────────────────────────────────────────────────

@Composable
private fun WelcomePage() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it / 4 })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DumbbellIllustration()
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.onboarding_welcome_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.onboarding_welcome_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }
        }
    }
}

// ── Page 2: Progress ──────────────────────────────────────────────────────────

@Composable
private fun ProgressPage() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it / 4 })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ProgressChartIllustration()
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.onboarding_progress_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.onboarding_progress_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Page 3: Setup ─────────────────────────────────────────────────────────────

@Composable
private fun SetupPage(
    selectedWeightUnit: WeightUnit,
    onWeightUnitChange: (WeightUnit) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it / 4 })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ScalesIllustration()
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.onboarding_setup_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.onboarding_setup_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Weight unit segmented button
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    WeightUnit.entries.forEachIndexed { index, unit ->
                        SegmentedButton(
                            selected = selectedWeightUnit == unit,
                            onClick = { onWeightUnitChange(unit) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = WeightUnit.entries.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(
                                text = unit.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Canvas Illustrations ──────────────────────────────────────────────────────

@Composable
private fun DumbbellIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "dumbbell_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    MaterialTheme.colorScheme.onPrimary

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer { rotationZ = rotation }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val barRadius = 10.dp.toPx()
        val plateRadius = 30.dp.toPx()
        val plateWidth = 18.dp.toPx()
        val barLen = size.width * 0.5f

        // Bar
        drawRoundRect(
            color = primaryContainer,
            topLeft = Offset(cx - barLen / 2, cy - barRadius),
            size = androidx.compose.ui.geometry.Size(barLen, barRadius * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barRadius)
        )

        // Left plates
        drawRoundRect(
            color = primary,
            topLeft = Offset(cx - barLen / 2 - plateWidth, cy - plateRadius),
            size = androidx.compose.ui.geometry.Size(plateWidth, plateRadius * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.6f),
            topLeft = Offset(cx - barLen / 2 - plateWidth * 1.8f, cy - plateRadius * 0.8f),
            size = androidx.compose.ui.geometry.Size(plateWidth * 0.7f, plateRadius * 1.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )

        // Right plates
        drawRoundRect(
            color = primary,
            topLeft = Offset(cx + barLen / 2, cy - plateRadius),
            size = androidx.compose.ui.geometry.Size(plateWidth, plateRadius * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.6f),
            topLeft = Offset(cx + barLen / 2 + plateWidth, cy - plateRadius * 0.8f),
            size = androidx.compose.ui.geometry.Size(plateWidth * 0.7f, plateRadius * 1.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
    }
}

@Composable
private fun ProgressChartIllustration() {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiary = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "chart_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "glow"
    )

    Canvas(modifier = Modifier.size(200.dp)) {
        val w = size.width
        val h = size.height
        val padH = h * 0.2f
        val padW = w * 0.12f

        // Chart area data points (normalized 0–1)
        val points = listOf(0.2f, 0.35f, 0.3f, 0.5f, 0.45f, 0.65f, 0.75f, 0.9f)
        val n = points.size
        val stepX = (w - padW * 2) / (n - 1).toFloat()

        fun xAt(i: Int) = padW + i * stepX
        fun yAt(v: Float) = h - padH - v * (h - padH * 2)

        // Fill gradient
        val fillPath = Path()
        fillPath.moveTo(xAt(0), h - padH)
        fillPath.lineTo(xAt(0), yAt(points[0]))
        points.forEachIndexed { i, v ->
            if (i > 0) {
                val prevX = xAt(i - 1)
                val prevY = yAt(points[i - 1])
                val cpX = (prevX + xAt(i)) / 2f
                fillPath.cubicTo(cpX, prevY, cpX, yAt(v), xAt(i), yAt(v))
            }
        }
        fillPath.lineTo(xAt(n - 1), h - padH)
        fillPath.close()

        drawPath(
            fillPath,
            Brush.verticalGradient(
                listOf(primary.copy(alpha = 0.25f), Color.Transparent),
                startY = padH, endY = h - padH
            )
        )

        // Line
        val linePath = Path()
        linePath.moveTo(xAt(0), yAt(points[0]))
        points.forEachIndexed { i, v ->
            if (i > 0) {
                val prevX = xAt(i - 1)
                val prevY = yAt(points[i - 1])
                val cpX = (prevX + xAt(i)) / 2f
                linePath.cubicTo(cpX, prevY, cpX, yAt(v), xAt(i), yAt(v))
            }
        }
        drawPath(linePath, primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

        // Dots
        points.forEachIndexed { i, v ->
            drawCircle(primary, radius = 5.dp.toPx(), center = Offset(xAt(i), yAt(v)))
            drawCircle(Color.White, radius = 2.5f.dp.toPx(), center = Offset(xAt(i), yAt(v)))
        }

        // Glowing top dot (latest PR)
        drawCircle(
            tertiary.copy(alpha = glowAlpha),
            radius = 14.dp.toPx(),
            center = Offset(xAt(n - 1), yAt(points.last()))
        )
        drawCircle(tertiary, radius = 6.dp.toPx(), center = Offset(xAt(n - 1), yAt(points.last())))

        // X-axis base line
        drawLine(
            primaryContainer,
            start = Offset(padW, h - padH),
            end = Offset(w - padW, h - padH),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
private fun ScalesIllustration() {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val secondary = MaterialTheme.colorScheme.secondary

    Canvas(modifier = Modifier.size(180.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Central pole
        drawLine(
            primary,
            start = Offset(cx, cy * 0.2f),
            end = Offset(cx, cy * 1.8f),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Horizontal beam
        drawLine(
            primary,
            start = Offset(cx * 0.2f, cy * 0.45f),
            end = Offset(cx * 1.8f, cy * 0.45f),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Left pan strings
        drawLine(secondary.copy(alpha = 0.7f),
            start = Offset(cx * 0.2f, cy * 0.45f),
            end = Offset(cx * 0.25f, cy * 0.9f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(secondary.copy(alpha = 0.7f),
            start = Offset(cx * 0.2f, cy * 0.45f),
            end = Offset(cx * 0.15f, cy * 0.9f),
            strokeWidth = 2.dp.toPx()
        )
        // Left pan
        drawRoundRect(
            primaryContainer,
            topLeft = Offset(cx * 0.05f, cy * 0.9f),
            size = androidx.compose.ui.geometry.Size(cx * 0.3f, 10.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
        )

        // Right pan strings
        drawLine(secondary.copy(alpha = 0.7f),
            start = Offset(cx * 1.8f, cy * 0.45f),
            end = Offset(cx * 1.75f, cy * 0.9f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(secondary.copy(alpha = 0.7f),
            start = Offset(cx * 1.8f, cy * 0.45f),
            end = Offset(cx * 1.85f, cy * 0.9f),
            strokeWidth = 2.dp.toPx()
        )
        // Right pan
        drawRoundRect(
            primaryContainer,
            topLeft = Offset(cx * 1.65f, cy * 0.9f),
            size = androidx.compose.ui.geometry.Size(cx * 0.3f, 10.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
        )

        // KG / LB labels represented as weight circles on pans
        drawCircle(primary.copy(alpha = 0.4f), radius = 14.dp.toPx(), center = Offset(cx * 0.2f, cy * 0.9f - 12.dp.toPx()))
        drawCircle(secondary.copy(alpha = 0.4f), radius = 10.dp.toPx(), center = Offset(cx * 1.8f, cy * 0.9f - 10.dp.toPx()))
        drawCircle(secondary.copy(alpha = 0.3f), radius = 6.dp.toPx(), center = Offset(cx * 1.75f, cy * 0.85f - 14.dp.toPx()))

        // Base
        drawLine(
            primary,
            start = Offset(cx * 0.7f, cy * 1.8f),
            end = Offset(cx * 1.3f, cy * 1.8f),
            strokeWidth = 8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
