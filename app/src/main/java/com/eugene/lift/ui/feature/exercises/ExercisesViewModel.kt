package com.eugene.lift.ui.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class SortOrder { NAME_ASC, NAME_DESC }
@HiltViewModel
class ExercisesViewModel @Inject constructor(
    repository: ExerciseRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery

    private val _selectedBodyParts = MutableStateFlow<Set<BodyPart>>(emptySet())
    val selectedBodyParts = _selectedBodyParts

    private val _selectedCategories = MutableStateFlow<Set<ExerciseCategory>>(emptySet())
    val selectedCategories = _selectedCategories

    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    val sortOrder = _sortOrder

    val exercises = combine(
        repository.getExercises(),
        _searchQuery,
        _selectedBodyParts,
        _selectedCategories,
        _sortOrder
    ) { list, query, bodyParts, categories, sort ->

        var result = list

        if (query.isNotBlank()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }

        if (bodyParts.isNotEmpty()) {
            result = result.filter { it.bodyPart in bodyParts }
        }

        if (categories.isNotEmpty()) {
            result = result.filter { it.category in categories }
        }

        when (sort) {
            SortOrder.NAME_ASC -> result.sortedBy { it.name }
            SortOrder.NAME_DESC -> result.sortedByDescending { it.name }
        }

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