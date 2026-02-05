package com.eugene.lift.data.local.dao

import androidx.room.*
import com.eugene.lift.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

// DTOs anidados para el Historial
data class SessionExerciseWithSets(
    @Embedded val sessionExercise: SessionExerciseEntity,

    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val bodyPartRefs: List<ExerciseBodyPartCrossRef>,

    @Relation(parentColumn = "id", entityColumn = "sessionExerciseId")
    val sets: List<WorkoutSetEntity>
)

data class SessionComplete(
    @Embedded val session: WorkoutSessionEntity,

    @Relation(
        entity = SessionExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val exercises: List<SessionExerciseWithSets>
)

@Dao
interface WorkoutDao {
    // Filtrado por fecha
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getHistory(from: LocalDateTime, to: LocalDateTime): Flow<List<SessionComplete>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun getSessionById(id: String): Flow<SessionComplete?>

    // --- Escritura ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercises(exercises: List<SessionExerciseEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Transaction
    suspend fun saveSessionComplete(
        session: WorkoutSessionEntity,
        exercises: List<SessionExerciseEntity>,
        sets: List<WorkoutSetEntity>
    ) {
        insertSession(session)
        // Nota: En una edición real, deberíamos borrar previos igual que en TemplateDao
        // Por simplicidad asumo inserción nueva. Si soportas "Editar Historial", añade deletes.
        insertSessionExercises(exercises)
        insertSets(sets)
    }

    @Transaction
    @Query("""
        SELECT * FROM workout_sessions 
        WHERE id IN (SELECT sessionId FROM session_exercises WHERE exerciseId = :exerciseId)
        AND date < :currentDate
        ORDER BY date DESC LIMIT 1
    """)
    suspend fun getLastSessionWithExercise(exerciseId: String, currentDate: LocalDateTime = LocalDateTime.now()): SessionComplete?
    // --- Estadísticas y PRs ---

    // Obtener el set con mayor peso para un ejercicio dado
    @Query("""
        SELECT s.* FROM workout_sets s
        JOIN session_exercises se ON s.sessionExerciseId = se.id
        WHERE se.exerciseId = :exerciseId AND s.completed = 1
        ORDER BY s.weight DESC LIMIT 1
    """)
    suspend fun getPersonalRecordSet(exerciseId: String): WorkoutSetEntity?

    @Transaction
    @Query("""
        SELECT * FROM workout_sessions 
        WHERE id IN (SELECT sessionId FROM session_exercises WHERE exerciseId = :exerciseId)
        ORDER BY date DESC
    """)
    fun getHistoryForExercise(exerciseId: String): Flow<List<SessionComplete>>

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    // Get the count of how many times each exercise has been used
    @Query("""
        SELECT exerciseId, COUNT(*) as useCount
        FROM session_exercises
        GROUP BY exerciseId
    """)
    suspend fun getExerciseUsageCount(): List<ExerciseUsageCount>

    // Get the most recent date each exercise was performed
    @Query("""
        SELECT se.exerciseId, MAX(ws.date) as lastUsedDate
        FROM session_exercises se
        JOIN workout_sessions ws ON se.sessionId = ws.id
        GROUP BY se.exerciseId
    """)
    suspend fun getExerciseLastUsedDates(): List<ExerciseLastUsed>

}

data class ExerciseUsageCount(
    val exerciseId: String,
    val useCount: Int
)

data class ExerciseLastUsed(
    val exerciseId: String,
    val lastUsedDate: LocalDateTime?
)
