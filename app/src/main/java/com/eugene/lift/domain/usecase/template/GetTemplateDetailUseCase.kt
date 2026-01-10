package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTemplateDetailUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    operator fun invoke(templateId: String): Flow<WorkoutTemplate?> {
        return repository.getTemplate(templateId)
    }
}