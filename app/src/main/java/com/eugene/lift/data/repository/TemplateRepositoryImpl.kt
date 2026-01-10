package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.TemplateDao
import com.eugene.lift.data.local.entity.TemplateExerciseEntity
import com.eugene.lift.data.mapper.toDomain
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val dao: TemplateDao
) : TemplateRepository {

    override fun getTemplates(isArchived: Boolean): Flow<List<WorkoutTemplate>> {
        return dao.getTemplates(isArchived).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTemplate(id: String): Flow<WorkoutTemplate?> {
        return dao.getTemplateById(id).map { it?.toDomain() }
    }

    override suspend fun saveTemplate(template: WorkoutTemplate) {
        val entity = template.toEntity()
        val exerciseEntities = template.exercises.map {
            TemplateExerciseEntity(
                id = it.id,
                templateId = template.id,
                exerciseId = it.exercise.id,
                orderIndex = it.orderIndex,
                targetSets = it.targetSets,
                targetReps = it.targetReps,
                restTimerSeconds = it.restTimerSeconds,
                note = it.note
            )
        }
        dao.saveTemplateComplete(entity, exerciseEntities)
    }

    override suspend fun archiveTemplate(id: String, isArchived: Boolean) {
        dao.setArchived(id, isArchived)
    }

    override suspend fun deleteTemplate(id: String) {
        dao.deleteTemplate(id)
    }
}