package com.eugene.lift.ui.feature.workout

import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.model.WorkoutTemplate

data class WorkoutUiState(
    val templates: List<WorkoutTemplate> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val folders: List<Folder> = emptyList(),
    val currentFolderId: String? = null
)

sealed interface WorkoutUiEvent {
    data class TabSelected(val index: Int) : WorkoutUiEvent
    data class FolderSelected(val folderId: String?) : WorkoutUiEvent
    data class FolderCreated(val name: String, val color: String) : WorkoutUiEvent
    data class FolderDeleted(val folderId: String) : WorkoutUiEvent
    data class TemplateMoved(val templateId: String, val folderId: String?) : WorkoutUiEvent
    data class TemplateClicked(val templateId: String) : WorkoutUiEvent
    data class TemplateEditClicked(val templateId: String?) : WorkoutUiEvent
    data class TemplateStartClicked(val templateId: String) : WorkoutUiEvent
    data class TemplateArchiveToggled(val templateId: String, val archive: Boolean) : WorkoutUiEvent
    data class TemplateDeleted(val templateId: String) : WorkoutUiEvent
    data class TemplateDuplicated(val templateId: String) : WorkoutUiEvent
    data class TemplateShared(val templateId: String) : WorkoutUiEvent
    data object StartEmptyClicked : WorkoutUiEvent
    data object AddTemplateClicked : WorkoutUiEvent
}
