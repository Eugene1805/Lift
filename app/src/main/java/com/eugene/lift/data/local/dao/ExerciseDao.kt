package com.eugene.lift.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.eugene.lift.data.local.entity.ExerciseBodyPartCrossRef
import com.eugene.lift.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

data class ExerciseResult(
    @Embedded val exercise: ExerciseEntity,
    val bodyParts: String
)
@Dao
interface ExerciseDao {

    @Query("""
        SELECT e.*, GROUP_CONCAT(cref.bodyPart) as bodyParts
        FROM exercises e
        LEFT JOIN exercise_body_part_cross_ref cref ON e.id = cref.exerciseId
        GROUP BY e.id
        ORDER BY e.name ASC
    """)
    fun getAllExercises(): Flow<List<ExerciseResult>>

    @Query("""
        SELECT e.*, GROUP_CONCAT(cref.bodyPart) as bodyParts
        FROM exercises e
        LEFT JOIN exercise_body_part_cross_ref cref ON e.id = cref.exerciseId
        WHERE e.id = :exerciseId
        GROUP BY e.id
    """)
    fun getExerciseById(exerciseId: String): Flow<ExerciseResult?>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<ExerciseBodyPartCrossRef>)

    @Query("DELETE FROM exercise_body_part_cross_ref WHERE exerciseId = :exerciseId")
    suspend fun deleteCrossRefs(exerciseId: String)

    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExerciseBase(exerciseId: String)

    @Transaction
    suspend fun saveExerciseComplete(
        exercise: ExerciseEntity,
        refs: List<ExerciseBodyPartCrossRef>
    ) {
        insertExercise(exercise)
        deleteCrossRefs(exercise.id)
        if (refs.isNotEmpty()) {
            insertCrossRefs(refs)
        }
    }

    @Transaction
    suspend fun deleteExerciseComplete(exerciseId: String) {
        deleteCrossRefs(exerciseId)
        deleteExerciseBase(exerciseId)
    }

    /**
     * Identifies exercises that are currently missing a visual representation.
     *
     * Used by the background assignment pipeline to determine which records need
     * a mapping lookup.
     */
    @Query("SELECT * FROM exercises WHERE imagePath IS NULL")
    suspend fun getExercisesWithoutImage(): List<ExerciseEntity>

    /**
     * Updates only the visual asset path for a specific exercise.
     *
     * This targeted update avoids the complexity of a full record replacement
     * and preserves all other exercise attributes (body parts, instructions, etc).
     */
    @Query("UPDATE exercises SET imagePath = :imagePath WHERE id = :exerciseId")
    suspend fun updateImagePath(exerciseId: String, imagePath: String)
}
