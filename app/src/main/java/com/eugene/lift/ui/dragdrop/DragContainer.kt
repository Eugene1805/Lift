package com.eugene.lift.ui.dragdrop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Global container that wraps the screen and renders the floating drag preview.
 * This ensures the preview is drawn over all other content and isn't clipped by lists.
 */
@Composable
fun DragContainer(
    dragState: DragUiState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Trigger haptic feedback when entering drag state
    LaunchedEffect(dragState.isDragging) {
        if (dragState.isDragging) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Trigger haptic feedback when hovering over a new target
    LaunchedEffect(dragState.hoveredTargetId) {
        if (dragState.isDragging && dragState.hoveredTargetId != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (dragState.isDragging && dragState.draggedItemId != null) {
            DragPreview(
                itemName = dragState.draggedItemName,
                position = dragState.dragPosition
            )
        }
    }
}
