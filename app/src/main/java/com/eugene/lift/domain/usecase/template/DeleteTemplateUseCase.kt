package com.eugene.lift.domain.usecase.template

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.TemplateRepository
import javax.inject.Inject

class DeleteTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository,
    private val safeExecutor: SafeExecutor
) {
    suspend operator fun invoke(templateId: String): AppResult<Unit> {
        return safeExecutor.execute {
            repository.deleteTemplate(templateId)
        }
    }
}