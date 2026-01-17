package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoldersUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke(): Flow<List<Folder>> {
        return repository.getAllFolders()
    }
}