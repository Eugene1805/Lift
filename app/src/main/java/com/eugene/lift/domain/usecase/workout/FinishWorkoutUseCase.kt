package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.UserProfileRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class FinishWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(activeSession: WorkoutSession) {
        val duration = calculateDuration(activeSession)
        val processedExercises = processExercises(activeSession.exercises)

        validateSession(processedExercises)

        val finalSession = activeSession.copy(
            durationSeconds = duration,
            exercises = processedExercises
        )

        repository.saveSession(finalSession)
        recordUserStats(finalSession, duration)
    }

    private fun calculateDuration(session: WorkoutSession): Long {
        val endTime = LocalDateTime.now()
        return Duration.between(session.date, endTime).seconds
    }

    private suspend fun processExercises(
        exercises: List<SessionExercise>
    ): List<SessionExercise> {
        return exercises
            .map { processExercise(it) }
            .filter { it.sets.isNotEmpty() }
    }

    private suspend fun processExercise(
        exercise: SessionExercise
    ): SessionExercise {
        val completedSets = exercise.sets.filter { it.completed }
        if (completedSets.isEmpty()) return exercise

        val sessionBestWeight = completedSets.maxOf { it.weight }
        val previousRecordWeight = getPreviousRecordWeight(exercise)

        if (sessionBestWeight <= previousRecordWeight) return exercise

        return exercise.copy(
            sets = markPrSets(exercise.sets, sessionBestWeight)
        )
    }

    private suspend fun getPreviousRecordWeight(
        exercise: SessionExercise
    ): Double {
        return repository
            .getPersonalRecord(exercise.exercise.id)
            .first()
            ?.weight ?: 0.0
    }

    private fun markPrSets(
        sets: List<WorkoutSet>,
        prWeight: Double
    ): List<WorkoutSet> {
        return sets.map { set ->
            if (set.completed && set.weight == prWeight) {
                set.copy(isPr = true)
            } else {
                set
            }
        }
    }
    private fun validateSession(exercises: List<SessionExercise>) {
        check(exercises.isNotEmpty()) {
            "No se puede guardar un entrenamiento vacío"
        }
    }

    private suspend fun recordUserStats(
        session: WorkoutSession,
        duration: Long
    ) {
        val completedSets = session.exercises
            .flatMap { it.sets }
            .filter { it.completed }

        val totalVolume = completedSets.sumOf { it.weight * it.reps }
        val totalPRs = completedSets.count { it.isPr }

        val profile = userProfileRepository.getCurrentProfileOnce() ?: return

        userProfileRepository.recordWorkoutCompleted(
            id = profile.id,
            volume = totalVolume,
            duration = duration,
            prCount = totalPRs
        )
    }

}