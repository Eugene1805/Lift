package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {

    fun getTemplates(isArchived: Boolean): Flow<List<WorkoutTemplate>>
    fun getTemplate(id: String): Flow<WorkoutTemplate?>
    suspend fun saveTemplate(template: WorkoutTemplate)
    suspend fun archiveTemplate(id: String, isArchived: Boolean)
    suspend fun deleteTemplate(id: String)
}