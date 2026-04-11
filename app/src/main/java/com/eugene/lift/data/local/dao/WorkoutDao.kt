package com.eugene.lift.data.local.dao

import androidx.room.*
import com.eugene.lift.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

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
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getHistory(from: LocalDateTime, to: LocalDateTime): Flow<List<SessionComplete>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun getSessionById(id: String): Flow<SessionComplete?>

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
        insertSessionExercises(exercises)
        insertSets(sets)
    }

    @Transaction
    @Query("""
        SELECT * FROM workout_sessions 
        WHERE id IN (SELECT sessionId FROM session_exercises WHERE exerciseId = :exerciseId)
        AND (:templateId IS NULL OR templateId = :templateId)
        AND date < :currentDate
        ORDER BY date DESC LIMIT 1
    """)
    suspend fun getLastSessionWithExercise(
        exerciseId: String,
        templateId: String?,
        currentDate: LocalDateTime = LocalDateTime.now()
    ): SessionComplete?

    /**
     * Same as [getLastSessionWithExercise] but only returns sessions where the exercise has at least
     * one completed set (i.e., it was actually performed).
     */
    @Transaction
    @Query("""
        SELECT * FROM workout_sessions
        WHERE id IN (
            SELECT se.sessionId
            FROM session_exercises se
            WHERE se.exerciseId = :exerciseId
            AND EXISTS (
                SELECT 1
                FROM workout_sets s
                WHERE s.sessionExerciseId = se.id
                AND s.completed = 1
            )
        )
        AND (:templateId IS NULL OR templateId = :templateId)
        AND date < :currentDate
        ORDER BY date DESC LIMIT 1
    """)
    suspend fun getLastPerformedSessionWithExercise(
        exerciseId: String,
        templateId: String?,
        currentDate: LocalDateTime = LocalDateTime.now()
    ): SessionComplete?

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

    @Query("""
        SELECT exerciseId, COUNT(*) as useCount
        FROM session_exercises
        GROUP BY exerciseId
    """)
    suspend fun getExerciseUsageCount(): List<ExerciseUsageCount>

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
