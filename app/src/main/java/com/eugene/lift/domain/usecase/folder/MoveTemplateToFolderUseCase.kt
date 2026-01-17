package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MoveTemplateToFolderUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    /**
     * @param folderId: El ID de la carpeta destino, o NULL para mover a la raíz.
     */
    suspend operator fun invoke(templateId: String, folderId: String?) {

        val template = repository.getTemplate(templateId).first() ?: return

        val updatedTemplate = template.copy(folderId = folderId)

        repository.saveTemplate(updatedTemplate)
    }
}