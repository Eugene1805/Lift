package com.eugene.lift.ui.dragdrop

import androidx.compose.ui.geometry.Offset

/**
 * Global hoisted state for tracking a drag-and-drop operation.
 * Designed to be held in a ViewModel to separate drag math from Compose UI.
 */
data class DragUiState(
    /** Whether a drag operation is currently active */
    val isDragging: Boolean = false,
    /** The ID of the item being dragged (e.g., Template ID) */
    val draggedItemId: String? = null,
    /** The display name of the dragged item for the preview */
    val draggedItemName: String = "",
    /** Current absolute pointer position in window coordinates */
    val dragPosition: Offset = Offset.Zero,
    /** The ID of the valid drop target currently being hovered over, if any */
    val hoveredTargetId: String? = null
)

/**
 * Interface that ViewModels implement to receive global drag updates from the UI.
 */
interface DragDropActionHandler {
    fun onDragStart(itemId: String, itemName: String, initialPosition: Offset)
    fun onDragMove(position: Offset)
    fun onDragEnd()
    fun onDragCancel()
}
