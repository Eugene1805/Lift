package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.repository.FolderRepository
import javax.inject.Inject

class CreateFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(name: String, color: String) {
        if (name.isBlank()) return

        val folder = Folder(
            name = name.trim(),
            color = color
        )
        repository.createFolder(folder)
    }
}