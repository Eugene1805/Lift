package com.eugene.lift.ui.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.usecase.folder.CreateFolderUseCase
import com.eugene.lift.domain.usecase.folder.DeleteFolderUseCase
import com.eugene.lift.domain.usecase.folder.GetFoldersUseCase
import com.eugene.lift.domain.usecase.folder.MoveTemplateToFolderUseCase
import com.eugene.lift.domain.usecase.template.DeleteTemplateUseCase
import com.eugene.lift.domain.usecase.template.DuplicateTemplateUseCase
import com.eugene.lift.domain.usecase.template.GetAllTemplatesUseCase
import com.eugene.lift.domain.usecase.template.ToggleTemplateArchiveUseCase
import com.eugene.lift.domain.usecase.template.UpdateTemplatesOrderUseCase
import com.eugene.lift.ui.event.UiEvent
import com.eugene.lift.ui.feature.workout.edit.ReorderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val updateTemplatesOrderUseCase: UpdateTemplatesOrderUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _currentFolderId = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)

    private val _events = Channel<UiEvent>()
    val events = _events.receiveAsFlow()

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


    private val _dragState = MutableStateFlow(com.eugene.lift.ui.dragdrop.DragUiState())
    private val dropTargets = mutableMapOf<String, androidx.compose.ui.geometry.Rect>()
    private val _reorderState = MutableStateFlow(ReorderUiState())

    val uiState: StateFlow<WorkoutUiState> = combine(
        templatesFlow,
        foldersFlow,
        _selectedTab,
        _currentFolderId,
        _isLoading,
        _dragState,
        _reorderState
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        WorkoutUiState(
            templates = args[0] as List<com.eugene.lift.domain.model.WorkoutTemplate>,
            folders = args[1] as List<com.eugene.lift.domain.model.Folder>,
            selectedTab = args[2] as Int,
            currentFolderId = args[3] as String?,
            isLoading = args[4] as Boolean,
            dragState = args[5] as com.eugene.lift.ui.dragdrop.DragUiState,
            reorderState = args[6] as ReorderUiState
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WorkoutUiState()
    )

    init {
        // Only stop loading after the first emission from the data source,
        // not from the stateIn default (which emits emptyList immediately).
        viewModelScope.launch {
            getAllTemplatesUseCase().firstOrNull()
            _isLoading.value = false
        }
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
            
            WorkoutUiEvent.ToggleReorderMode -> toggleReorderMode()
            is WorkoutUiEvent.TemplatesReordered -> reorderTemplates(event.fromIndex, event.toIndex, event.isArchived)

            is WorkoutUiEvent.OnDragStart -> {
                _dragState.value = _dragState.value.copy(
                    isDragging = true,
                    draggedItemId = event.templateId,
                    draggedItemName = event.templateName,
                    dragPosition = event.position,
                    hoveredTargetId = null
                )
            }
            is WorkoutUiEvent.OnDragMove -> {
                val hoveredId = dropTargets.entries.firstOrNull { (_, rect) ->
                    rect.contains(event.position)
                }?.key
                _dragState.value = _dragState.value.copy(
                    dragPosition = event.position,
                    hoveredTargetId = hoveredId
                )
            }
            is WorkoutUiEvent.OnDragEnd -> {
                val state = _dragState.value
                if (state.draggedItemId != null && state.hoveredTargetId != null) {
                    moveTemplate(state.draggedItemId, state.hoveredTargetId)
                }
                _dragState.value = com.eugene.lift.ui.dragdrop.DragUiState()
            }
            is WorkoutUiEvent.OnDragCancel -> {
                _dragState.value = com.eugene.lift.ui.dragdrop.DragUiState()
            }
            is WorkoutUiEvent.OnDropTargetBoundsChanged -> {
                dropTargets[event.targetId] = event.bounds
            }
        }
    }

    private fun toggleReorderMode() {
        val current = _reorderState.value
        _reorderState.value = current.copy(isReorderMode = !current.isReorderMode)
    }

    private fun reorderTemplates(fromIndex: Int, toIndex: Int, isArchived: Boolean) {
        viewModelScope.launch {
            val allTemplates = uiState.value.templates
            val (targetList, otherList) = allTemplates.partition { it.isArchived == isArchived }
            
            val mutableTargetList = targetList.toMutableList()
            if (fromIndex !in mutableTargetList.indices || toIndex !in mutableTargetList.indices) return@launch
            mutableTargetList.apply { add(toIndex, removeAt(fromIndex)) }

            // Reconstruct the full list, keeping unarchived items first generally, or just assign sequential sortOrders
            val combined = if (isArchived) otherList + mutableTargetList else mutableTargetList + otherList
            val orderedList = combined.mapIndexed { index, template ->
                template.copy(sortOrder = index)
            }
            updateTemplatesOrderUseCase(orderedList)
        }
    }

    private fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            when (val result = deleteFolderUseCase(folderId)) {
                is AppResult.Success -> {
                    if (_currentFolderId.value == folderId) {
                        _currentFolderId.value = null
                    }
                }
                is AppResult.Error -> _events.send(UiEvent.ShowSnackbar(result.error))
            }
        }
    }

    private fun archiveTemplate(templateId: String, archive: Boolean) {
        viewModelScope.launch {
            val result = toggleArchiveUseCase(templateId, archive)
            if (result is AppResult.Error) {
                _events.send(UiEvent.ShowSnackbar(result.error))
            }
        }
    }

    private fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            val result = deleteTemplateUseCase(templateId)
            if (result is AppResult.Error) {
                _events.send(UiEvent.ShowSnackbar(result.error))
            }
        }
    }

    private fun duplicateTemplate(templateId: String) {
        viewModelScope.launch {
            val result = duplicateTemplateUseCase(templateId)
            if (result is AppResult.Error) {
                _events.send(UiEvent.ShowSnackbar(result.error))
            }
        }
    }

    private fun createFolder(name: String, color: String) {
        viewModelScope.launch {
            val result = createFolderUseCase(name, color)
            if (result is AppResult.Error) {
                _events.send(UiEvent.ShowSnackbar(result.error))
            }
        }
    }

    private fun moveTemplate(templateId: String, folderId: String?) {
        viewModelScope.launch {
            val result = moveTemplateToFolderUseCase(templateId, folderId)
            if (result is AppResult.Error) {
                _events.send(UiEvent.ShowSnackbar(result.error))
            }
        }
    }
}