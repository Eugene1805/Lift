package com.eugene.lift.domain.usecase.template

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import javax.inject.Inject

class SaveTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository,
    private val safeExecutor: SafeExecutor
) {
    suspend operator fun invoke(template: WorkoutTemplate): AppResult<Unit> {
        if (template.name.isBlank()) {
            return AppResult.Error(AppError.Validation)
        }

        return safeExecutor.execute {
            repository.saveTemplate(template)
        }
    }
}