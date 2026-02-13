package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.data.local.entity.SessionExerciseEntity
import com.eugene.lift.data.local.entity.WorkoutSetEntity
import com.eugene.lift.data.mapper.toDomain
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.WorkoutRepository
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.util.WeightConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao,
    // Acceso a preferencias del usuario para unidad de peso
    private val settingsRepository: SettingsRepository
) : WorkoutRepository {

    override fun getHistory(from: LocalDate?, to: LocalDate?): Flow<List<WorkoutSession>> {
        // Si son null, ponemos rangos por defecto (ej: últimos 100 años o lógica de negocio)
        val fromDate = from?.atStartOfDay() ?: LocalDateTime.of(2000, 1, 1, 0, 0)
        val toDate = to?.atTime(LocalTime.MAX) ?: LocalDateTime.of(3000, 12, 31, 23, 59)

        return dao.getHistory(fromDate, toDate).map { list ->
            // Convertimos desde KG a la preferencia actual
            list.map { it.toDomain() }
        }.map { sessions ->
            // Aplicar conversión de presentación según preferencia actual
            val unit = currentWeightUnit()
            sessions.map { convertSessionToPreference(it, unit) }
        }
    }

    override fun getSessionDetails(id: String): Flow<WorkoutSession?> {
        return dao.getSessionById(id).map { it?.toDomain() }.map { session ->
            val unit = currentWeightUnit()
            session?.let { convertSessionToPreference(it, unit) }
        }
    }

    override suspend fun saveSession(session: WorkoutSession) {
        val sessionEntity = session.toEntity()
        val exerciseEntities = mutableListOf<SessionExerciseEntity>()
        val setEntities = mutableListOf<WorkoutSetEntity>()

        // Siempre guardamos en KG en la base de datos
        val unit = currentWeightUnit()

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
                val weightInKg = when (unit) {
                    WeightUnit.KG -> s.weight
                    WeightUnit.LBS -> WeightConverter.lbsToKg(s.weight)
                }
                setEntities.add(
                    WorkoutSetEntity(
                        id = s.id,
                        sessionExerciseId = ex.id,
                        orderIndex = setIndex,
                        weight = weightInKg,
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

    override fun getPersonalRecord(exerciseId: String): Flow<WorkoutSet?> {
        return flow {
            val entity = dao.getPersonalRecordSet(exerciseId)
            val unit = currentWeightUnit()
            val domain = entity?.toDomain()
            emit(domain?.let { convertSetToPreference(it, unit) })
        }
    }

    override fun getExerciseHistory(exerciseId: String): Flow<List<WorkoutSession>> {
        return dao.getHistoryForExercise(exerciseId).map { list ->
            list.map { it.toDomain() }
        }.map { sessions ->
            val unit = currentWeightUnit()
            sessions.map { convertSessionToPreference(it, unit) }
        }
    }

    override suspend fun getLastHistoryForExercise(exerciseId: String): WorkoutSession? {
        // Mapeamos el resultado del DAO al Dominio
        val sessionComplete = dao.getLastSessionWithExercise(exerciseId) ?: return null
        val unit = currentWeightUnit()
        return convertSessionToPreference(sessionComplete.toDomain(), unit)
    }

    override suspend fun getExerciseUsageCount(): Map<String, Int> {
        return dao.getExerciseUsageCount().associate { it.exerciseId to it.useCount }
    }

    override suspend fun getExerciseLastUsedDates(): Map<String, LocalDateTime> {
        return dao.getExerciseLastUsedDates()
            .filter { it.lastUsedDate != null }
            .associate { it.exerciseId to it.lastUsedDate!! }
    }

    // Helpers
    private suspend fun currentWeightUnit(): WeightUnit {
        return settingsRepository.getSettings().first().weightUnit
    }

    private fun convertSessionToPreference(session: WorkoutSession, unit: WeightUnit): WorkoutSession {
        // Las entidades vienen en KG; si preferencia es LBS, convertimos para presentación
        val convertedExercises = session.exercises.map { se ->
            val convertedSets = se.sets.map { convertSetToPreference(it, unit) }
            se.copy(sets = convertedSets)
        }
        return session.copy(exercises = convertedExercises)
    }

    private fun convertSetToPreference(set: WorkoutSet, unit: WeightUnit): WorkoutSet {
        val displayWeight = when (unit) {
            WeightUnit.KG -> set.weight
            WeightUnit.LBS -> WeightConverter.kgToLbs(set.weight)
        }
        return set.copy(weight = displayWeight)
    }
}