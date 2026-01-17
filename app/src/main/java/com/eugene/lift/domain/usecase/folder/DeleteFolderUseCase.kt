package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.domain.repository.FolderRepository
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(folderId: String) {
        repository.deleteFolder(folderId)
    }
}