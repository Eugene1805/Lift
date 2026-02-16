package com.eugene.lift.ui.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.usecase.folder.CreateFolderUseCase
import com.eugene.lift.domain.usecase.folder.DeleteFolderUseCase
import com.eugene.lift.domain.usecase.folder.GetFoldersUseCase
import com.eugene.lift.domain.usecase.folder.MoveTemplateToFolderUseCase
import com.eugene.lift.domain.usecase.template.DeleteTemplateUseCase
import com.eugene.lift.domain.usecase.template.DuplicateTemplateUseCase
import com.eugene.lift.domain.usecase.template.GetAllTemplatesUseCase
import com.eugene.lift.domain.usecase.template.ToggleTemplateArchiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    getAllTemplatesUseCase: GetAllTemplatesUseCase,
    private val toggleArchiveUseCase: ToggleTemplateArchiveUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase,
    private val duplicateTemplateUseCase: DuplicateTemplateUseCase,
    getFoldersUseCase: GetFoldersUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val moveTemplateToFolderUseCase: MoveTemplateToFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _currentFolderId = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)

    private val templatesFlow = combine(
        getAllTemplatesUseCase(),
        _currentFolderId
    ) { allTemplates, folderId ->
        allTemplates.filter { it.folderId == folderId }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val foldersFlow = getFoldersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<WorkoutUiState> = combine(
        templatesFlow,
        foldersFlow,
        _selectedTab,
        _currentFolderId,
        _isLoading
    ) { templates, folders, tab, folderId, isLoading ->
        WorkoutUiState(
            templates = templates,
            isLoading = isLoading,
            selectedTab = tab,
            folders = folders,
            currentFolderId = folderId
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WorkoutUiState()
    )

    init {
        templatesFlow.onEach { _isLoading.value = false }.launchIn(viewModelScope)
    }

    fun onEvent(event: WorkoutUiEvent) {
        when (event) {
            is WorkoutUiEvent.TabSelected -> _selectedTab.value = event.index
            is WorkoutUiEvent.FolderSelected -> _currentFolderId.value = event.folderId
            is WorkoutUiEvent.FolderCreated -> createFolder(event.name, event.color)
            is WorkoutUiEvent.FolderDeleted -> deleteFolder(event.folderId)
            is WorkoutUiEvent.TemplateMoved -> moveTemplate(event.templateId, event.folderId)
            is WorkoutUiEvent.TemplateArchiveToggled -> archiveTemplate(event.templateId, event.archive)
            is WorkoutUiEvent.TemplateDeleted -> deleteTemplate(event.templateId)
            is WorkoutUiEvent.TemplateDuplicated -> duplicateTemplate(event.templateId)
            WorkoutUiEvent.AddTemplateClicked,
            is WorkoutUiEvent.TemplateClicked,
            is WorkoutUiEvent.TemplateEditClicked,
            is WorkoutUiEvent.TemplateStartClicked,
            is WorkoutUiEvent.TemplateShared,
            WorkoutUiEvent.StartEmptyClicked -> Unit
        }
    }

    private fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            deleteFolderUseCase(folderId)
            if (_currentFolderId.value == folderId) {
                _currentFolderId.value = null
            }
        }
    }

    private fun archiveTemplate(templateId: String, archive: Boolean) {
        viewModelScope.launch { toggleArchiveUseCase(templateId, archive) }
    }

    private fun deleteTemplate(templateId: String) {
        viewModelScope.launch { deleteTemplateUseCase(templateId) }
    }

    private fun duplicateTemplate(templateId: String) {
        viewModelScope.launch { duplicateTemplateUseCase(templateId) }
    }

    private fun createFolder(name: String, color: String) {
        viewModelScope.launch { createFolderUseCase(name, color) }
    }

    private fun moveTemplate(templateId: String, folderId: String?) {
        viewModelScope.launch { moveTemplateToFolderUseCase(templateId, folderId) }
    }
}