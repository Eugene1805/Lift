package com.eugene.lift.ui.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.usecase.exercise.ExerciseFilter
import com.eugene.lift.domain.usecase.exercise.ExerciseUsageStats
import com.eugene.lift.domain.usecase.exercise.GetExercisesUseCase
import com.eugene.lift.domain.usecase.exercise.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery

    private val _selectedBodyParts = MutableStateFlow<Set<BodyPart>>(emptySet())
    val selectedBodyParts = _selectedBodyParts

    private val _selectedCategories = MutableStateFlow<Set<ExerciseCategory>>(emptySet())
    val selectedCategories = _selectedCategories

    private val _sortOrder = MutableStateFlow(SortOrder.RECENT)
    val sortOrder = _sortOrder

    private val _usageStats = MutableStateFlow(ExerciseUsageStats())

    private val filterFlow = combine(
        _searchQuery,
        _selectedBodyParts,
        _selectedCategories,
        _sortOrder
    ) { query, parts, categories, sort ->
        ExerciseFilter(query, parts, categories, sort)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises = combine(
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

    // Total count of exercises matching current filters
    val totalExerciseCount = exercises.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        loadUsageStats()
    }

    private fun loadUsageStats() {
        viewModelScope.launch {
            _usageStats.value = getExercisesUseCase.getUsageStats()
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        if (order == SortOrder.RECENT || order == SortOrder.FREQUENCY) {
            loadUsageStats()
        }
    }

    fun toggleBodyPartFilter(part: BodyPart) {
        val current = _selectedBodyParts.value.toMutableSet()
        if (part in current) current.remove(part) else current.add(part)
        _selectedBodyParts.value = current
    }

    fun toggleCategoryFilter(cat: ExerciseCategory) {
        val current = _selectedCategories.value.toMutableSet()
        if (cat in current) current.remove(cat) else current.add(cat)
        _selectedCategories.value = current
    }

    fun clearFilters() {
        _selectedBodyParts.value = emptySet()
        _selectedCategories.value = emptySet()
    }
}