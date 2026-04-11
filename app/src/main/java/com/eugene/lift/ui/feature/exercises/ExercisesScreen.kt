package com.eugene.lift.ui.feature.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.eugene.lift.R
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.usecase.exercise.SortOrder
import com.eugene.lift.ui.components.ExercisesEmptyState

private fun drawableResIdOrNull(drawableName: String?): Int? {
    return when (drawableName) {
        "abductors" -> R.drawable.abductors
        "back_squat" -> R.drawable.back_squat
        "barbell_row" -> R.drawable.barbell_row
        "bench_press" -> R.drawable.bench_press
        "cable_lateral_raise" -> R.drawable.cable_lateral_raise
        "chest_peck_fly" -> R.drawable.chest_peck_fly
        "deadlift" -> R.drawable.deadlift
        "dumbell_biceps_curl" -> R.drawable.dumbell_biceps_curl
        "dumbell_bulgarian_split_squat" -> R.drawable.dumbell_bulgarian_split_squat
        "dumbell_incline_chest_press" -> R.drawable.dumbell_incline_chest_press
        "dumbell_shoulder_press" -> R.drawable.dumbell_shoulder_press
        "hip_thrust" -> R.drawable.hip_thrust
        "leg_extension" -> R.drawable.leg_extension
        "machine_preacher_curl" -> R.drawable.machine_preacher_curl
        "machine_standing_calf_raises" -> R.drawable.machine_standing_calf_raises
        "overhead_shoulder_press" -> R.drawable.overhead_shoulder_press
        "pull_up" -> R.drawable.pull_up
        "single_arm_triceps_extension" -> R.drawable.single_arm_triceps_extension
        "smith_machine_bulgarian_split_squat" -> R.drawable.smith_machine_bulgarian_split_squat
        "weigthed_dips" -> R.drawable.weigthed_dips
        "wrist_curl" -> R.drawable.wrist_curl
        else -> null
    }
}


@Composable
fun ExercisesRoute(
    onAddClick: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onExercisesSelected: (List<String>) -> Unit = {},
    isSelectionMode: Boolean = false,
    viewModel: ExercisesViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isSelectionMode) {
        viewModel.onEvent(ExercisesUiEvent.SelectionModeChanged(isSelectionMode))
    }

    ExercisesScreen(
        uiState = uiState.value,
        onEvent = { event ->
            when (event) {
                ExercisesUiEvent.AddClicked -> onAddClick()
                is ExercisesUiEvent.ExerciseClicked -> onExerciseClick(event.exerciseId)
                ExercisesUiEvent.SelectionConfirmed -> {
                    onExercisesSelected(uiState.value.selectedExerciseIds.toList())
                    viewModel.onEvent(ExercisesUiEvent.SelectionConfirmed)
                }
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    uiState: ExercisesUiState,
    onEvent: (ExercisesUiEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            ExercisesTopBar(
                uiState = uiState,
                scrollBehavior = scrollBehavior,
                onEvent = onEvent
            )
        },
        floatingActionButton = {
            ExercisesFab(
                uiState = uiState,
                onEvent = onEvent
            )
        }
    ) { innerPadding ->

        ExercisesContent(
            uiState = uiState,
            modifier = Modifier.padding(innerPadding),
            onEvent = onEvent
        )

        if (uiState.isFilterSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { onEvent(ExercisesUiEvent.FilterSheetVisibilityChanged(false)) },
                sheetState = sheetState
            ) {
                FilterBottomSheetContent(
                    selectedBodyParts = uiState.selectedBodyParts,
                    selectedCategories = uiState.selectedCategories,
                    totalExerciseCount = uiState.totalExerciseCount,
                    onBodyPartToggle = { onEvent(ExercisesUiEvent.BodyPartToggled(it)) },
                    onCategoryToggle = { onEvent(ExercisesUiEvent.CategoryToggled(it)) },
                    onClearFilters = { onEvent(ExercisesUiEvent.ClearFilters) },
                    onApply = { onEvent(ExercisesUiEvent.FilterSheetVisibilityChanged(false)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisesTopBar(
    uiState: ExercisesUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ExercisesUiEvent) -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.exercises),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        SearchAndFilterRow(
            uiState = uiState,
            onEvent = onEvent
        )
    }
}

@Composable
private fun SearchAndFilterRow(
    uiState: ExercisesUiState,
    onEvent: (ExercisesUiEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val hasActiveFilters by remember(uiState.selectedBodyParts, uiState.selectedCategories) {
        derivedStateOf {
            uiState.selectedBodyParts.isNotEmpty() || uiState.selectedCategories.isNotEmpty()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchTextField(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { onEvent(ExercisesUiEvent.SearchQueryChanged(it)) },
            onSearch = { focusManager.clearFocus() },
            modifier = Modifier.weight(1f)
        )

        FilterButton(
            hasFilters = hasActiveFilters,
            onClick = { onEvent(ExercisesUiEvent.FilterSheetVisibilityChanged(true)) }
        )

        SortMenuButton(
            sortOrder = uiState.sortOrder,
            isExpanded = uiState.isSortMenuVisible,
            onEvent = onEvent
        )
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(stringResource(R.string.hint_search)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier,
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
private fun FilterButton(
    hasFilters: Boolean,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = if (hasFilters) {
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        } else {
            IconButtonDefaults.filledTonalIconButtonColors()
        }
    ) {
        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.title_filters))
    }
}

@Composable
private fun SortMenuButton(
    sortOrder: SortOrder,
    isExpanded: Boolean,
    onEvent: (ExercisesUiEvent) -> Unit
) {
    Box {
        IconButton(onClick = { onEvent(ExercisesUiEvent.SortMenuVisibilityChanged(true)) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.sort)
            )
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onEvent(ExercisesUiEvent.SortMenuVisibilityChanged(false)) }
        ) {
            SortMenuItem(
                textRes = R.string.sort_name_asc,
                sortOrder = SortOrder.NAME_ASC,
                currentSortOrder = sortOrder,
                onEvent = onEvent
            )
            SortMenuItem(
                textRes = R.string.sort_name_desc,
                sortOrder = SortOrder.NAME_DESC,
                currentSortOrder = sortOrder,
                onEvent = onEvent
            )
            SortMenuItem(
                textRes = R.string.sort_recent,
                sortOrder = SortOrder.RECENT,
                currentSortOrder = sortOrder,
                onEvent = onEvent
            )
            SortMenuItem(
                textRes = R.string.sort_frequency,
                sortOrder = SortOrder.FREQUENCY,
                currentSortOrder = sortOrder,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun SortMenuItem(
    textRes: Int,
    sortOrder: SortOrder,
    currentSortOrder: SortOrder,
    onEvent: (ExercisesUiEvent) -> Unit
) {
    DropdownMenuItem(
        text = { Text(stringResource(textRes)) },
        onClick = {
            onEvent(ExercisesUiEvent.SortOrderChanged(sortOrder))
            onEvent(ExercisesUiEvent.SortMenuVisibilityChanged(false))
        },
        leadingIcon = if (currentSortOrder == sortOrder) {
            { Icon(Icons.Default.Check, null) }
        } else null
    )
}

@Composable
private fun ExercisesFab(
    uiState: ExercisesUiState,
    onEvent: (ExercisesUiEvent) -> Unit
) {
    when {
        uiState.isSelectionMode && uiState.selectedExerciseIds.isNotEmpty() -> {
            ExtendedFloatingActionButton(
                onClick = { onEvent(ExercisesUiEvent.SelectionConfirmed) },
                icon = { Icon(Icons.Default.Check, stringResource(R.string.cd_check_selected)) },
                text = { Text(stringResource(R.string.exercise_add_selected, uiState.selectedExerciseIds.size)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
        uiState.selectedExerciseIds.isEmpty() -> {
            FloatingActionButton(
                onClick = { onEvent(ExercisesUiEvent.AddClicked) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
            }
        }
    }
}

@Composable
fun ExercisesContent(
    uiState: ExercisesUiState,
    onEvent: (ExercisesUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    if (uiState.exercises.isEmpty()) {
        ExercisesEmptyState(
            modifier = modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = uiState.exercises,
                key = { it.id },
                contentType = { "exercise_item" }
            ) { exercise ->
                val isSelected = exercise.id in uiState.selectedExerciseIds

                ExerciseItemCard(
                    exercise = exercise,
                    isSelectionMode = uiState.isSelectionMode,
                    isSelected = isSelected,
                    onClick = {
                        if (uiState.isSelectionMode) {
                            onEvent(ExercisesUiEvent.ExerciseSelectionToggled(exercise.id))
                        } else {
                            onEvent(ExercisesUiEvent.ExerciseClicked(exercise.id))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseItemCard(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false
) {
    val cardColors = if (isSelected) {
        CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = cardColors
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = exercise.name,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                ExerciseSupportingContent(exercise)
            },
            leadingContent = {
                ExerciseThumbnail(exerciseName = exercise.name, imagePath = exercise.imagePath)
            },
            trailingContent = {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() }
                    )
                }
            }
        )
    }
}

@Composable
private fun ExerciseThumbnail(exerciseName: String, imagePath: String?) {
    val imageResId = remember(imagePath) { drawableResIdOrNull(imagePath) }
    if (imageResId != null) {
        AsyncImage(
            model = imageResId,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    } else {
        ExercisePlaceholder(initial = exerciseName.take(1))
    }
}

@Composable
private fun ExercisePlaceholder(initial: String) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseSupportingContent(exercise: Exercise) {
    val bodyPartStrings = exercise.bodyParts.map { part ->
        stringResource(part.labelRes)
    }
    val bodyPartsString = remember(exercise.id, bodyPartStrings) { bodyPartStrings.joinToString(", ") }

    Text(
        text = "$bodyPartsString • ${stringResource(exercise.category.labelRes)}",
        color = MaterialTheme.colorScheme.onSurface
    )
}