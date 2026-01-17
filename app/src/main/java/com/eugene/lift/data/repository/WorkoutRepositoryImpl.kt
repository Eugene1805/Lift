package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.data.local.entity.SessionExerciseEntity
import com.eugene.lift.data.local.entity.WorkoutSetEntity
import com.eugene.lift.data.mapper.toDomain
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao
) : WorkoutRepository {

    override fun getHistory(from: LocalDate?, to: LocalDate?): Flow<List<WorkoutSession>> {
        // Si son null, ponemos rangos por defecto (ej: últimos 100 años o lógica de negocio)
        val fromDate = from?.atStartOfDay() ?: LocalDateTime.of(2000, 1, 1, 0, 0)
        val toDate = to?.atTime(LocalTime.MAX) ?: LocalDateTime.of(3000, 12, 31, 23, 59)

        return dao.getHistory(fromDate, toDate).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSessionDetails(id: String): Flow<WorkoutSession?> {
        return dao.getSessionById(id).map { it?.toDomain() }
    }

    override suspend fun saveSession(session: WorkoutSession) {
        val sessionEntity = session.toEntity()
        val exerciseEntities = mutableListOf<SessionExerciseEntity>()
        val setEntities = mutableListOf<WorkoutSetEntity>()

        session.exercises.forEachIndexed { index, ex ->
            exerciseEntities.add(
                SessionExerciseEntity(
                    id = ex.id,
                    sessionId = session.id,
                    exerciseId = ex.exercise.id,
                    orderIndex = index
                )
            )
            ex.sets.forEachIndexed { setIndex, s ->
                setEntities.add(
                    WorkoutSetEntity(
                        id = s.id,
                        sessionExerciseId = ex.id,
                        orderIndex = setIndex,
                        weight = s.weight,
                        reps = s.reps,
                        completed = s.completed,
                        rpe = s.rpe,
                        rir = s.rir,
                        isPr = s.isPr
                    )
                )
            }
        }

        dao.saveSessionComplete(sessionEntity, exerciseEntities, setEntities)
    }

    override suspend fun deleteSession(sessionId: String) {
        dao.deleteSession(sessionId)
    }

    override suspend fun getPersonalRecord(exerciseId: String): Flow<WorkoutSet?> {
        return flow {
            val entity = dao.getPersonalRecordSet(exerciseId)
            emit(entity?.toDomain())
        }
    }

    override fun getExerciseHistory(exerciseId: String): Flow<List<WorkoutSession>> {
        return dao.getHistoryForExercise(exerciseId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getLastHistoryForExercise(exerciseId: String): WorkoutSession? {
        // Mapeamos el resultado del DAO al Dominio
        val sessionComplete = dao.getLastSessionWithExercise(exerciseId) ?: return null
        return sessionComplete.toDomain()
    }
}