package com.eugene.lift.data.local.dao

import androidx.room.*
import com.eugene.lift.data.local.entity.*
import kotlinx.coroutines.flow.Flow

// 1. DTO: Un TemplateExercise + Su Ejercicio Base (Nombre, categoria...)
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

// 2. DTO: El Template completo + Lista de ejercicios detallados
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
    @Query("SELECT * FROM workout_templates WHERE isArchived = :isArchived ORDER BY lastPerformedAt DESC")
    fun getTemplates(isArchived: Boolean): Flow<List<TemplateWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE id = :id")
    fun getTemplateById(id: String): Flow<TemplateWithExercises?>

    // --- Escritura (Sin cambios) ---
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

    @Query("DELETE FROM workout_templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)
}