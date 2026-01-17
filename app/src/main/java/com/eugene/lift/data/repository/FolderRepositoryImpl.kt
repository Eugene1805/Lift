package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.FolderDao
import com.eugene.lift.data.local.entity.toEntity
import com.eugene.lift.data.local.entity.toDomain
import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val dao: FolderDao
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> {
        return dao.getAllFolders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createFolder(folder: Folder) {
        dao.insertFolder(folder.toEntity())
    }

    override suspend fun updateFolder(folder: Folder) {
        dao.updateFolder(folder.toEntity())
    }

    override suspend fun deleteFolder(folderId: String) {
        dao.deleteFolder(folderId)
    }
}