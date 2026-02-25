package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MoveTemplateToFolderUseCase @Inject constructor(
    private val repository: TemplateRepository,
    private val safeExecutor: SafeExecutor
) {
    /**
     * @param folderId: The destination folder ID, or NULL to move to root.
     */
    suspend operator fun invoke(templateId: String, folderId: String?): AppResult<Unit> {
        return safeExecutor.execute {
            val template = repository.getTemplate(templateId).first() ?: return@execute
            val updatedTemplate = template.copy(folderId = folderId)
            repository.saveTemplate(updatedTemplate)
        }
    }
}