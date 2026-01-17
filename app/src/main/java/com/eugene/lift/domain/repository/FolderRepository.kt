package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    fun getAllFolders(): Flow<List<Folder>>
    suspend fun createFolder(folder: Folder)
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folderId: String)
}