package com.eugene.lift.ui.feature.exercises.detail

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.eugene.lift.R

/**
 * Resolves a drawable resource name (e.g. "bench_press") to an
 * android.resource URI that Coil can load.
 * Returns null if the name is null or the resource does not exist.
 */
@Composable
private fun drawableUriOrNull(drawableName: String?): String? {
    if (drawableName == null) return null
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    return if (resId != 0) "android.resource://${context.packageName}/$resId" else null
}

/**
 * Loads an image from [imageUri] and extracts a dominant color via Palette.
 * Animates the background smoothly from white to the extracted color.
 */
@Composable
private fun DynamicColorImageBox(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dominantColor by remember(imageUri) { mutableStateOf(Color.White) }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUri)
            .allowHardware(false) // required for Palette bitmap access
            .crossfade(true)
            .build()
    )

    // Extract Palette once the image finishes loading
    LaunchedEffect(painter.state) {
        val success = painter.state as? AsyncImagePainter.State.Success ?: return@LaunchedEffect
        val bitmap = (success.result.drawable as? BitmapDrawable)?.bitmap ?: return@LaunchedEffect
        val palette = Palette.from(bitmap).generate()
        val swatch = palette.dominantSwatch
            ?: palette.lightVibrantSwatch
            ?: palette.lightMutedSwatch
        swatch?.let { dominantColor = Color(it.rgb) }
    }

    val animatedBg by animateColorAsState(
        targetValue = dominantColor,
        animationSpec = tween(durationMillis = 600),
        label = "imageBg"
    )

    Box(
        modifier = modifier.background(animatedBg),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ExerciseDetailRoute(
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExerciseDetailScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                ExerciseDetailUiEvent.BackClicked -> onNavigateBack()
                ExerciseDetailUiEvent.EditClicked -> uiState.exercise?.let { onEditClick(it.id) }
            }
            viewModel.onEvent(event)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailScreen(
    uiState: ExerciseDetailUiState,
    onEvent: (ExerciseDetailUiEvent) -> Unit
) {

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(uiState.exercise?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ExerciseDetailUiEvent.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(ExerciseDetailUiEvent.EditClicked) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.exercise_detail_edit))
                    }
                },
                windowInsets = WindowInsets(0,0,0,0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        val state = uiState.exercise
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.loading))
            }
            return@Scaffold
        }
        if (state == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.exercise_detail_no_data))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            val imageUri = if (state.imagePath != null) drawableUriOrNull(state.imagePath) else null
            if (imageUri != null) {
                DynamicColorImageBox(
                    imageUri = imageUri,
                    contentDescription = state.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.exercise_detail_no_image),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    stringResource(R.string.exercise_detail_details),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    SuggestionChip(
                        onClick = {},
                        label = { Text(stringResource(state.category.labelRes)) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    state.bodyParts.forEach { part ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(part.labelRes)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


                Text(
                    stringResource(R.string.exercise_detail_instructions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.instructions.ifBlank { stringResource(R.string.exercise_detail_no_instructions) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}