package com.eugene.lift.ui.feature.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.ui.theme.LiftTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.ImeAction
import com.eugene.lift.domain.model.MeasureType

@Composable
fun ExercisesRoute(
    onAddClick: () -> Unit,
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
        onAddClick = onAddClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    exercises: List<ExerciseEntity>,
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
    modifier: Modifier = Modifier
) {

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            Column{
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.exercises),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    scrollBehavior = scrollBehavior
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
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    )
    { innerPadding ->
        ExercisesContent(
            exercises = exercises,
            modifier = Modifier.padding(innerPadding)
        )

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {showSheet = false },
                sheetState = sheetState
            ) {
                FilterBottomSheetContent(
                    selectedBodyParts = selectedBodyParts,
                    selectedCategories = selectedCategories,
                    onBodyPartToggle = onBodyPartToggle,
                    onCategoryToggle = onCategoryToggle,
                    onClearFilters = onClearFilters,
                    onApply = { showSheet = false}
                )
            }
        }
    }
}

@Composable
fun ExercisesContent(
    exercises: List<ExerciseEntity>,
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
            items(
                items = exercises,
                key = { it.id }
            ) { exercise ->
                ExerciseItemCard(exercise = exercise)
            }
        }
    }
}

@Composable
fun ExerciseItemCard(exercise: ExerciseEntity, modifier: Modifier = Modifier) {

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
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
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}

@Composable
private fun ExerciseSupportingContent(exercise: ExerciseEntity) {
    val bodyPartStrings = exercise.bodyParts.map { part ->
        stringResource(part.labelRes)
    }
    val bodyPartsString = bodyPartStrings.joinToString(", ")

    Text(
        text = "$bodyPartsString • ${stringResource(exercise.category.labelRes)}",
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true)
@Composable
fun ExercisesScreenPreview() {
    LiftTheme {
        ExercisesScreen(
            exercises = listOf(
                ExerciseEntity(
                    id = "1",
                    name = "Push Up",
                    bodyParts = listOf(BodyPart.CHEST, BodyPart.CHEST, BodyPart.SHOULDERS),
                    category = ExerciseCategory.ASSISTED_BODYWEIGHT,
                    measureType = MeasureType.REPS_AND_WEIGHT
                ),
                ExerciseEntity(
                    id = "2",
                    name = "Squat",
                    bodyParts = listOf(BodyPart.CARDIO, BodyPart.CARDIO, BodyPart.CHEST),
                    category = ExerciseCategory.BARBELL,
                    measureType = MeasureType.REPS_AND_WEIGHT
                ),
                ExerciseEntity(
                    id = "9",
                    name = "Plank",
                    bodyParts = listOf(BodyPart.CORE, BodyPart.SHOULDERS),
                    category = ExerciseCategory.CARDIO,
                    measureType = MeasureType.REPS_AND_WEIGHT
                ),
                ExerciseEntity(
                    id = "4",
                    name = "Pull Up",
                    bodyParts = listOf(BodyPart.BACK, BodyPart.BACK),
                    category = ExerciseCategory.CARDIO,
                    measureType = MeasureType.REPS_AND_WEIGHT
                )
            ),
            searchQuery = "",
            sortOrder = SortOrder.NAME_ASC,
            selectedBodyParts = emptySet(),
            selectedCategories = emptySet(),
            onSearchQueryChange = {},
            onSortToggle = {},
            onBodyPartToggle = {},
            onCategoryToggle = {},
            onClearFilters = {},
            onAddClick = {}
        )
    }
}