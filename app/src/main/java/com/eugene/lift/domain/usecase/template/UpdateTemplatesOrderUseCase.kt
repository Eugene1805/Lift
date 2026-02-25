package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import javax.inject.Inject

class UpdateTemplatesOrderUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    suspend operator fun invoke(templates: List<WorkoutTemplate>) {
        templateRepository.updateTemplatesOrder(templates)
    }
}
