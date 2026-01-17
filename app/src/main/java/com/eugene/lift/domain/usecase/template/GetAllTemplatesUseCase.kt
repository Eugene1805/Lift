package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAllTemplatesUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    operator fun invoke(): Flow<List<WorkoutTemplate>> {
        val activeFlow = repository.getTemplates(isArchived = false)

        val archivedFlow = repository.getTemplates(isArchived = true)

        return combine(activeFlow, archivedFlow) { active, archived ->
            active + archived
        }
    }
}