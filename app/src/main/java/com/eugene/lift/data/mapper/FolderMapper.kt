package com.eugene.lift.data.mapper

import com.eugene.lift.data.local.entity.FolderEntity
import com.eugene.lift.domain.model.Folder

fun FolderEntity.toDomain() = Folder(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)

fun Folder.toEntity() = FolderEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)