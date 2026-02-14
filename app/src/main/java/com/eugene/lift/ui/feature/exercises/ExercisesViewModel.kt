package com.eugene.lift.ui.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.usecase.exercise.ExerciseFilter
import com.eugene.lift.domain.usecase.exercise.ExerciseUsageStats
import com.eugene.lift.domain.usecase.exercise.GetExercisesUseCase
import com.eugene.lift.domain.usecase.exercise.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class ExercisesFilterState(
    val exercises: List<Exercise>,
    val searchQuery: String,
    val sortOrder: SortOrder,
    val bodyParts: Set<BodyPart>,
    val categories: Set<ExerciseCategory>
)

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedBodyParts = MutableStateFlow<Set<BodyPart>>(emptySet())
    private val _selectedCategories = MutableStateFlow<Set<ExerciseCategory>>(emptySet())
    private val _sortOrder = MutableStateFlow(SortOrder.RECENT)
    private val _usageStats = MutableStateFlow(ExerciseUsageStats())
    private val _isFilterSheetVisible = MutableStateFlow(false)
    private val _isSortMenuVisible = MutableStateFlow(false)
    private val _selectedExerciseIds = MutableStateFlow<Set<String>>(emptySet())
    private val _isSelectionMode = MutableStateFlow(false)

    private val filterFlow = combine(
        _searchQuery,
        _selectedBodyParts,
        _selectedCategories,
        _sortOrder
    ) { query, parts, categories, sort ->
        ExerciseFilter(query, parts, categories, sort)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val exercisesFlow = combine(
        filterFlow.flatMapLatest { filter -> getExercisesUseCase(filter) },
        _sortOrder,
        _usageStats
    ) { filteredExercises, sort, stats ->
        when (sort) {
            SortOrder.RECENT, SortOrder.FREQUENCY ->
                getExercisesUseCase.sortByStats(filteredExercises, sort, stats)
            else -> filteredExercises
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val filterState = combine(
        exercisesFlow,
        _searchQuery,
        _sortOrder,
        _selectedBodyParts,
        _selectedCategories
    ) { exercises, query, sort, bodyParts, categories ->
        ExercisesFilterState(exercises, query, sort, bodyParts, categories)
    }

    val uiState: StateFlow<ExercisesUiState> = combine(
        filterState,
        _isFilterSheetVisible,
        _isSortMenuVisible,
        _isSelectionMode,
        _selectedExerciseIds
    ) { filters, isSheetVisible, isSortMenuVisible, isSelectionMode, selectedIds ->
        ExercisesUiState(
            exercises = filters.exercises,
            searchQuery = filters.searchQuery,
            sortOrder = filters.sortOrder,
            selectedBodyParts = filters.bodyParts,
            selectedCategories = filters.categories,
            totalExerciseCount = filters.exercises.size,
            isFilterSheetVisible = isSheetVisible,
            isSortMenuVisible = isSortMenuVisible,
            isSelectionMode = isSelectionMode,
            selectedExerciseIds = selectedIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExercisesUiState()
    )

    init {
        loadUsageStats()
    }

    private fun loadUsageStats() {
        viewModelScope.launch {
            _usageStats.value = getExercisesUseCase.getUsageStats()
        }
    }

    fun onEvent(event: ExercisesUiEvent) {
        when (event) {
            is ExercisesUiEvent.SearchQueryChanged -> _searchQuery.value = event.query
            is ExercisesUiEvent.SortOrderChanged -> {
                _sortOrder.value = event.sortOrder
                if (event.sortOrder == SortOrder.RECENT || event.sortOrder == SortOrder.FREQUENCY) {
                    loadUsageStats()
                }
            }
            is ExercisesUiEvent.BodyPartToggled -> {
                val updated = _selectedBodyParts.value.toMutableSet()
                if (event.bodyPart in updated) updated.remove(event.bodyPart) else updated.add(event.bodyPart)
                _selectedBodyParts.value = updated
            }
            is ExercisesUiEvent.CategoryToggled -> {
                val updated = _selectedCategories.value.toMutableSet()
                if (event.category in updated) updated.remove(event.category) else updated.add(event.category)
                _selectedCategories.value = updated
            }
            ExercisesUiEvent.ClearFilters -> {
                _selectedBodyParts.value = emptySet()
                _selectedCategories.value = emptySet()
            }
            is ExercisesUiEvent.FilterSheetVisibilityChanged -> _isFilterSheetVisible.value = event.isVisible
            is ExercisesUiEvent.SortMenuVisibilityChanged -> _isSortMenuVisible.value = event.isVisible
            is ExercisesUiEvent.ExerciseSelectionToggled -> {
                if (_isSelectionMode.value) {
                    val updated = _selectedExerciseIds.value.toMutableSet()
                    if (event.exerciseId in updated) updated.remove(event.exerciseId) else updated.add(event.exerciseId)
                    _selectedExerciseIds.value = updated
                }
            }
            is ExercisesUiEvent.SelectionModeChanged -> {
                _isSelectionMode.value = event.enabled
                if (!event.enabled) {
                    _selectedExerciseIds.value = emptySet()
                }
            }
            ExercisesUiEvent.ClearSelection, ExercisesUiEvent.SelectionConfirmed -> _selectedExerciseIds.value = emptySet()
            ExercisesUiEvent.AddClicked, is ExercisesUiEvent.ExerciseClicked -> Unit
        }
    }
}