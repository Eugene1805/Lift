package com.eugene.lift.data.local.dao

import androidx.room.*
import com.eugene.lift.data.local.entity.*
import kotlinx.coroutines.flow.Flow

data class TemplateExerciseDetail(
    @Embedded val templateExercise: TemplateExerciseEntity,

    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercise: ExerciseEntity,

    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val bodyPartRefs: List<ExerciseBodyPartCrossRef>
)

data class TemplateWithExercises(
    @Embedded val template: WorkoutTemplateEntity,

    @Relation(
        entity = TemplateExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val exercises: List<TemplateExerciseDetail>
)

@Dao
interface TemplateDao {
    @Transaction
    @Query("SELECT * FROM workout_templates WHERE isArchived = :isArchived ORDER BY sortOrder ASC, lastPerformedAt DESC")
    fun getTemplates(isArchived: Boolean): Flow<List<TemplateWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE id = :id")
    fun getTemplateById(id: String): Flow<TemplateWithExercises?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercises(exercises: List<TemplateExerciseEntity>)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteTemplateExercises(templateId: String)

    @Transaction
    suspend fun saveTemplateComplete(template: WorkoutTemplateEntity, exercises: List<TemplateExerciseEntity>) {
        insertTemplate(template)
        deleteTemplateExercises(template.id)
        insertTemplateExercises(exercises)
    }

    @Query("UPDATE workout_templates SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: String, isArchived: Boolean)

    @Update
    suspend fun updateTemplatesOrder(templates: List<WorkoutTemplateEntity>)

    @Query("DELETE FROM workout_templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)
}