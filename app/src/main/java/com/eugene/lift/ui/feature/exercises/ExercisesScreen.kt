
package com.eugene.lift.ui.feature.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.usecase.SortOrder

@Composable
fun ExercisesRoute(
    onAddClick: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onExercisesSelected: (List<String>) -> Unit = {},
    isSelectionMode: Boolean = false,
    viewModel: ExercisesViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedBodyParts by viewModel.selectedBodyParts.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()

    ExercisesScreen(
        exercises = exercises,
        searchQuery = searchQuery,
        sortOrder = sortOrder,
        selectedBodyParts = selectedBodyParts,
        selectedCategories = selectedCategories,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSortToggle = viewModel::toggleSortOrder,
        onBodyPartToggle = viewModel::toggleBodyPartFilter,
        onCategoryToggle = viewModel::toggleCategoryFilter,
        onClearFilters = viewModel::clearFilters,
        onAddClick = onAddClick,
        onExerciseClick = onExerciseClick,
        onExercisesSelected = onExercisesSelected,
        isSelectionMode = isSelectionMode
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    exercises: List<Exercise>,
    searchQuery: String,
    sortOrder: SortOrder,
    selectedBodyParts: Set<BodyPart>,
    selectedCategories: Set<ExerciseCategory>,
    onSearchQueryChange: (String) -> Unit,
    onSortToggle: () -> Unit,
    onBodyPartToggle: (BodyPart) -> Unit,
    onCategoryToggle: (ExerciseCategory) -> Unit,
    onClearFilters: () -> Unit,
    onAddClick: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onExercisesSelected: (List<String>) -> Unit,
    isSelectionMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
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
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text(stringResource(R.string.hint_search)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraLarge,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )

                    val hasFilters = selectedBodyParts.isNotEmpty() || selectedCategories.isNotEmpty()
                    FilledTonalIconButton(
                        onClick = { showSheet = true },
                        colors = if (hasFilters) IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) else IconButtonDefaults.filledTonalIconButtonColors()
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.sort))
                    }

                    IconButton(onClick = onSortToggle) {
                        val rotation = if (sortOrder == SortOrder.NAME_ASC) 0f else 180f
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.sort),
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isSelectionMode) {
                if (selectedIds.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { onExercisesSelected(selectedIds.toList()) },
                        icon = { Icon(Icons.Default.Check, null) },
                        text = { Text(stringResource(R.string.exercise_add_selected, selectedIds.size)) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            if(selectedIds.isEmpty()){
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->

        ExercisesContent(
            exercises = exercises,
            modifier = Modifier.padding(innerPadding),
            isSelectionMode = isSelectionMode,
            selectedIds = selectedIds,
            onExerciseClick = { exerciseId ->
                if (isSelectionMode) {
                    selectedIds = if (exerciseId in selectedIds) {
                        selectedIds - exerciseId
                    } else {
                        selectedIds + exerciseId
                    }
                } else {
                    onExerciseClick(exerciseId)
                }
            }
        )

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                FilterBottomSheetContent(
                    selectedBodyParts = selectedBodyParts,
                    selectedCategories = selectedCategories,
                    onBodyPartToggle = onBodyPartToggle,
                    onCategoryToggle = onCategoryToggle,
                    onClearFilters = onClearFilters,
                    onApply = { showSheet = false }
                )
            }
        }
    }
}

@Composable
fun ExercisesContent(
    exercises: List<Exercise>,
    onExerciseClick: (String) -> Unit,
    isSelectionMode: Boolean,
    selectedIds: Set<String>,
    modifier: Modifier = Modifier
) {
    if (exercises.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(32.dp),
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
            items(items = exercises, key = { it.id }) { exercise ->
                val isSelected = exercise.id in selectedIds

                ExerciseItemCard(
                    exercise = exercise,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onClick = { onExerciseClick(exercise.id) }
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
        CardDefaults.elevatedCardColors()
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
                        Text(exercise.name.take(1), style = MaterialTheme.typography.titleMedium)
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