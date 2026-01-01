package com.eugene.lift.ui.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.usecase.ExerciseFilter
import com.eugene.lift.domain.usecase.GetExercisesUseCase
import com.eugene.lift.domain.usecase.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    getExercisesUseCase: GetExercisesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery

    private val _selectedBodyParts = MutableStateFlow<Set<BodyPart>>(emptySet())
    val selectedBodyParts = _selectedBodyParts

    private val _selectedCategories = MutableStateFlow<Set<ExerciseCategory>>(emptySet())
    val selectedCategories = _selectedCategories

    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    val sortOrder = _sortOrder

    private val filterFlow = combine(
        _searchQuery,
        _selectedBodyParts,
        _selectedCategories,
        _sortOrder
    ) { query, parts, categories, sort ->
        ExerciseFilter(query, parts, categories, sort)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises = filterFlow.flatMapLatest { filter ->
        getExercisesUseCase(filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.NAME_ASC)
            SortOrder.NAME_DESC else SortOrder.NAME_ASC
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