package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.repository.FolderRepository
import javax.inject.Inject

class CreateFolderUseCase @Inject constructor(
    private val repository: FolderRepository,
    private val safeExecutor: SafeExecutor
) {
    suspend operator fun invoke(name: String, color: String): AppResult<Unit> {
        if (name.isBlank()) return AppResult.Success(Unit) // silently ignore blank names

        val folder = Folder(
            name = name.trim(),
            color = color
        )
        return safeExecutor.execute {
            repository.createFolder(folder)
        }
    }
}