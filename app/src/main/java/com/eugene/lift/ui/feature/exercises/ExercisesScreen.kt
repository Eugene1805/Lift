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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eugene.lift.R
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.usecase.exercise.SortOrder

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
            hasFilters = uiState.selectedBodyParts.isNotEmpty() || uiState.selectedCategories.isNotEmpty(),
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
                icon = { Icon(Icons.Default.Check, null) },
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
                Icon(Icons.Default.Add, contentDescription = null)
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
    if (uiState.exercises.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.empty_exercises_text),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = uiState.exercises, key = { it.id }) { exercise ->
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
                if (exercise.imagePath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(exercise.imagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = exercise.name.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
private fun ExerciseSupportingContent(exercise: Exercise) {
    val bodyPartStrings = exercise.bodyParts.map { part ->
        stringResource(part.labelRes)
    }
    val bodyPartsString = bodyPartStrings.joinToString(", ")

    Text(
        text = "$bodyPartsString • ${stringResource(exercise.category.labelRes)}",
        color = MaterialTheme.colorScheme.onSurface
    )
}