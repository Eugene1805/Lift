package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.repository.TemplateRepository
import javax.inject.Inject

class DeleteTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(templateId: String) {
        repository.deleteTemplate(templateId)
    }
}