package com.eugene.lift.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.eugene.lift.data.local.entity.ExerciseBodyPartCrossRef
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import kotlinx.coroutines.flow.Flow

data class ExerciseResult(
    @Embedded val exercise: ExerciseEntity,
    val bodyParts: String // SQLite devolverá "CHEST,ARMS" usando GROUP_CONCAT
)
@Dao
interface ExerciseDao {

    @Query("""
        SELECT e.*, GROUP_CONCAT(cref.bodyPart) as bodyParts
        FROM exercises e
        LEFT JOIN exercise_body_part_cross_ref cref ON e.id = cref.exerciseId
        GROUP BY e.id
    """)
    fun getAllExercisesRaw(): Flow<List<ExerciseResult>>
    // INSERTAR: Requiere Transacción porque tocamos 2 tablas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseBase(exercise: ExerciseEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<ExerciseBodyPartCrossRef>)
    @Query("DELETE FROM exercise_body_part_cross_ref WHERE exerciseId = :exerciseId")
    suspend fun deleteOldCrossRefs(exerciseId: String)
    // Esta es la función pública que llamará el Repo
    @Transaction
    suspend fun saveExerciseWithParts(exercise: ExerciseEntity, parts: List<BodyPart>) {
        // 1. Guardamos la info base
        insertExerciseBase(exercise)

        // 2. Borramos relaciones viejas (por si es una edición)
        deleteOldCrossRefs(exercise.id)

        // 3. Insertamos las nuevas relaciones
        val refs = parts.map { part ->
            ExerciseBodyPartCrossRef(exerciseId = exercise.id, bodyPart = part)
        }
        insertCrossRefs(refs)
    }
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
}