package com.eugene.lift.ui.feature.workout

import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.model.WorkoutTemplate

import com.eugene.lift.ui.dragdrop.DragUiState
import com.eugene.lift.ui.feature.workout.edit.ReorderUiState

data class WorkoutUiState(
    val templates: List<WorkoutTemplate> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val folders: List<Folder> = emptyList(),
    val currentFolderId: String? = null,
    val dragState: DragUiState = DragUiState(),
    val reorderState: ReorderUiState = ReorderUiState()
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
    data object ToggleReorderMode : WorkoutUiEvent
    data class TemplatesReordered(val fromIndex: Int, val toIndex: Int, val isArchived: Boolean) : WorkoutUiEvent

    // --- Drag & Drop Events ---
    data class OnDragStart(val templateId: String, val templateName: String, val position: androidx.compose.ui.geometry.Offset) : WorkoutUiEvent
    data class OnDragMove(val position: androidx.compose.ui.geometry.Offset) : WorkoutUiEvent
    data object OnDragEnd : WorkoutUiEvent
    data object OnDragCancel : WorkoutUiEvent
    data class OnDropTargetBoundsChanged(val targetId: String, val bounds: androidx.compose.ui.geometry.Rect) : WorkoutUiEvent
}
