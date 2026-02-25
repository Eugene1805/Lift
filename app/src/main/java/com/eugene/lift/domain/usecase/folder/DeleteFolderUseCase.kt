package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.FolderRepository
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor(
    private val repository: FolderRepository,
    private val safeExecutor: SafeExecutor
) {
    suspend operator fun invoke(folderId: String): AppResult<Unit> {
        return safeExecutor.execute {
            repository.deleteFolder(folderId)
        }
    }
}