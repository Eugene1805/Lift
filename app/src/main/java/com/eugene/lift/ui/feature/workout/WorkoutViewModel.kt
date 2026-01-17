package com.eugene.lift.ui.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.WorkoutTemplate
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    getAllTemplatesUseCase: GetAllTemplatesUseCase,
    private val toggleArchiveUseCase: ToggleTemplateArchiveUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase,
    private val duplicateTemplateUseCase: DuplicateTemplateUseCase,
    private val getFoldersUseCase: GetFoldersUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val moveTemplateToFolderUseCase: MoveTemplateToFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab

    private val _currentFolderId = MutableStateFlow<String?>(null)
    val currentFolderId = _currentFolderId.asStateFlow()

    // 2. Lista de carpetas disponibles
    val folders = getFoldersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates = combine(
        getAllTemplatesUseCase(),
        _currentFolderId
    ) { allTemplates, folderId ->
        // Si estamos en raíz (folderId == null), mostramos las que NO tienen folderId
        // OJO: Esto depende de cómo quieras la UX.
        // Opción A (Carpetas como filtro estricto): Solo muestras las sueltas en raíz.
        // Opción B (Carpetas como etiquetas): Muestras todas, pero arriba las carpetas.

        // Vamos con la Opción A (Estilo explorador de archivos)
        allTemplates.filter { it.folderId == folderId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun archiveTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            toggleArchiveUseCase(template.id, !template.isArchived)
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            deleteTemplateUseCase(templateId)
        }
    }

    fun duplicateTemplate(templateId: String) {
        viewModelScope.launch {
            duplicateTemplateUseCase(templateId)
        }
    }

    fun selectFolder(folderId: String?) {
        _currentFolderId.value = folderId
    }

    fun createFolder(name: String, color: String) {
        viewModelScope.launch {
            createFolderUseCase(name, color)
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            deleteFolderUseCase(folderId)
            // Si borramos la carpeta actual, volvemos a la raíz
            if (_currentFolderId.value == folderId) {
                _currentFolderId.value = null
            }
        }
    }

    fun moveTemplate(workOutTemplate: WorkoutTemplate, folderId: String?) {
        viewModelScope.launch {
            moveTemplateToFolderUseCase(workOutTemplate.id, folderId)
        }
    }
}