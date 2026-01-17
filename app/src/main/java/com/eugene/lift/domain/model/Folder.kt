package com.eugene.lift.domain.model

import java.util.UUID

data class Folder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String, // Ej: "#FF5722"
    val createdAt: Long = System.currentTimeMillis()
)