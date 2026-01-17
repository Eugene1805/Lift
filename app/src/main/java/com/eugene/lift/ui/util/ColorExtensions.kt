package com.eugene.lift.ui.util

import androidx.compose.ui.graphics.Color

fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color.Gray
    }
}

val FolderColors = listOf(
    "#F44336", // Rojo
    "#E91E63", // Rosa
    "#9C27B0", // Púrpura
    "#2196F3", // Azul
    "#4CAF50", // Verde
    "#FF9800", // Naranja
    "#795548", // Café
    "#607D8B"  // Gris Azulado
)