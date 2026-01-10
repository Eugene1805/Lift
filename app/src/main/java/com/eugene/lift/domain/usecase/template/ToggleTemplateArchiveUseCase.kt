package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.repository.TemplateRepository
import javax.inject.Inject

class ToggleTemplateArchiveUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(templateId: String, isArchived: Boolean) {
        repository.archiveTemplate(templateId, isArchived)
    }
}