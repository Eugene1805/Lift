package com.eugene.lift.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Reusable drag-and-drop state for LazyColumn exercise reordering.
 *
 * Usage:
 *  val dragDropState = rememberDragDropState(lazyListState)
 *  // attach Modifier.dragHandle(dragDropState, index) to each row's handle
 *  // read dragDropState.draggingItemIndex / dragDropState.dragOffset for overlay rendering
 */
class DragDropState(val lazyListState: LazyListState) {

    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    var dragOffset by mutableFloatStateOf(0f)
        private set

    /** Pixel Y of the pointer relative to the viewport top */
    private var pointerY by mutableFloatStateOf(0f)

    val isDragging get() = draggingItemIndex != null

    fun onDragStart(index: Int, initialOffsetInItem: Float) {
        draggingItemIndex = index
        pointerY = itemTopY(index) + initialOffsetInItem
        dragOffset = 0f
    }

    fun onDragBy(delta: Float) {
        dragOffset += delta
        pointerY += delta
    }

    /**
     * Commits the drag. Calls [onMove] with (from, to) if the index changed.
     */
    fun onDragEnd(onMove: (fromIndex: Int, toIndex: Int) -> Unit) {
        val from = draggingItemIndex ?: return
        val to = computeTargetIndex()
        if (from != to) {
            onMove(from, to)
        }
        draggingItemIndex = null
        dragOffset = 0f
        pointerY = 0f
    }

    fun onDragCancelled() {
        draggingItemIndex = null
        dragOffset = 0f
        pointerY = 0f
    }

    /** Returns the current snap-target index based on pointer position */
    fun computeTargetIndex(): Int {
        val items = lazyListState.layoutInfo.visibleItemsInfo
        if (items.isEmpty()) return draggingItemIndex ?: 0
        val totalItems = lazyListState.layoutInfo.totalItemsCount
        // Clamp pointer to viewport
        val viewportTop = lazyListState.layoutInfo.viewportStartOffset.toFloat()
        val viewportBottom = lazyListState.layoutInfo.viewportEndOffset.toFloat()
        val clampedY = pointerY.coerceIn(viewportTop, viewportBottom)
        // Find item whose centre is closest to the clamped pointer
        val target = items.minByOrNull { item ->
            val itemCentre = (item.offset + item.size / 2).toFloat()
            kotlin.math.abs(itemCentre - clampedY)
        }
        return (target?.index ?: (draggingItemIndex ?: 0)).coerceIn(0, totalItems - 1)
    }

    /** Y-coordinate (relative to viewport) of the top of [index] */
    fun itemTopY(index: Int): Float {
        return lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }
            ?.offset?.toFloat() ?: 0f
    }

    /** Height in px of item at [index] */
    fun itemHeight(index: Int): Float {
        return lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }
            ?.size?.toFloat() ?: 0f
    }
}

@Composable
fun rememberDragDropState(lazyListState: LazyListState): DragDropState {
    return remember(lazyListState) { DragDropState(lazyListState) }
}
