package com.eugene.lift.ui.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.usecase.template.DeleteTemplateUseCase
import com.eugene.lift.domain.usecase.template.GetTemplatesUseCase
import com.eugene.lift.domain.usecase.template.ToggleTemplateArchiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getTemplatesUseCase: GetTemplatesUseCase,
    private val toggleArchiveUseCase: ToggleTemplateArchiveUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase
) : ViewModel() {

    // Estado de la pestaña seleccionada (0 = Activas, 1 = Archivadas)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab

    // Cada vez que cambia la pestaña, recargamos la lista correspondiente
    @OptIn(ExperimentalCoroutinesApi::class)
    val templates = _selectedTab.flatMapLatest { tabIndex ->
        val isArchived = tabIndex == 1
        getTemplatesUseCase(isArchived)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun archiveTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            // Invertimos el estado actual
            toggleArchiveUseCase(template.id, !template.isArchived)
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            deleteTemplateUseCase(templateId)
        }
    }
}