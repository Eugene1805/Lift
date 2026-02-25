package com.eugene.lift.ui.dragdrop

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Modifier to register a component's window bounds as a drop target.
 * 
 * @param targetId The unique identifier for this drop target (e.g., Folder ID).
 * @param onBoundsReported Callback fired when this component's position/size changes, providing its global rect.
 */
fun Modifier.dropTarget(
    targetId: String,
    onBoundsReported: (targetId: String, bounds: androidx.compose.ui.geometry.Rect) -> Unit
): Modifier = this.then(
    Modifier.onGloballyPositioned { layoutCoordinates ->
        val bounds = layoutCoordinates.boundsInWindow()
        onBoundsReported(targetId, bounds)
    }
)
