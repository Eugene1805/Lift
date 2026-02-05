package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface WorkoutRepository {
    fun getHistory(from: LocalDate?, to: LocalDate?): Flow<List<WorkoutSession>>

    fun getSessionDetails(id: String): Flow<WorkoutSession?>

    suspend fun saveSession(session: WorkoutSession)

    suspend fun deleteSession(sessionId: String)

    suspend fun getPersonalRecord(exerciseId: String): Flow<WorkoutSet?>


    fun getExerciseHistory(exerciseId: String): Flow<List<WorkoutSession>>

    suspend fun getLastHistoryForExercise(exerciseId: String): WorkoutSession?

    suspend fun getExerciseUsageCount(): Map<String, Int>

    suspend fun getExerciseLastUsedDates(): Map<String, LocalDateTime>
}