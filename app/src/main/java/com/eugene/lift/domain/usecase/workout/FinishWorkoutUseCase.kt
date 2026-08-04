package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutCompletionSummary
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.UserProfileRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.util.ExercisePerformanceEvaluator
import com.eugene.lift.domain.util.WeightConverter
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class FinishWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
    private val userProfileRepository: UserProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val performanceEvaluator: ExercisePerformanceEvaluator,
    private val safeExecutor: SafeExecutor
) {
    suspend operator fun invoke(activeSession: WorkoutSession): AppResult<WorkoutCompletionSummary> {
        val duration = calculateDuration(activeSession)
        val processedExercises = processExercises(activeSession.exercises)

        if (processedExercises.isEmpty()) {
            return AppResult.Error(AppError.Validation)
        }

        val finalSession = activeSession.copy(
            durationSeconds = duration,
            exercises = processedExercises
        )

        return safeExecutor.execute {
            val unit = settingsRepository.getSettings().first().weightUnit
            val summary = buildCompletionSummary(finalSession, unit)
            repository.saveSession(finalSession)
            recordUserStats(summary)
            summary
        }
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
            .map { exercise -> exercise.copy(sets = exercise.sets.filter { it.completed }) }
            .filter { it.sets.isNotEmpty() }
    }

    private suspend fun processExercise(
        exercise: SessionExercise
    ): SessionExercise {
        val measureType = exercise.exercise.measureType
        if (!performanceEvaluator.supportsPrTracking(measureType)) {
            return exercise
        }

        val sessionBestValue = performanceEvaluator.bestCompletedValue(exercise.sets, measureType)
        if (sessionBestValue <= 0.0) {
            return exercise
        }

        val previousBestValue = getPreviousRecordValue(exercise)
        if (sessionBestValue <= previousBestValue) return exercise

        return exercise.copy(
            sets = markPrSets(exercise.sets, sessionBestValue, measureType)
        )
    }

    private suspend fun getPreviousRecordValue(
        exercise: SessionExercise
    ): Double {
        val measureType = exercise.exercise.measureType
        return repository.getExerciseHistory(exercise.exercise.id)
            .first()
            .flatMap { session ->
                session.exercises.filter { sessionExercise ->
                    sessionExercise.exercise.id == exercise.exercise.id
                }
            }
            .maxOfOrNull { sessionExercise ->
                performanceEvaluator.bestCompletedValue(sessionExercise.sets, measureType)
            } ?: 0.0
    }

    private fun markPrSets(
        sets: List<WorkoutSet>,
        prValue: Double,
        measureType: com.eugene.lift.domain.model.MeasureType
    ): List<WorkoutSet> {
        return sets.map { set ->
            if (set.completed && performanceEvaluator.matchesPerformance(set, prValue, measureType)) {
                set.copy(isPr = true)
            } else {
                set
            }
        }
    }

    private fun buildCompletionSummary(
        session: WorkoutSession,
        unit: WeightUnit
    ): WorkoutCompletionSummary {
        val completedSets = session.exercises
            .flatMap { it.sets }
            .filter { it.completed }
        val totalVolume = completedSets.sumOf { set -> set.weight * set.reps }
        val totalVolumeKg = when (unit) {
            WeightUnit.KG -> totalVolume
            WeightUnit.LBS -> WeightConverter.lbsToKg(totalVolume)
        }

        return WorkoutCompletionSummary(
            workoutName = session.name,
            durationSeconds = session.durationSeconds,
            completedExerciseCount = session.exercises.size,
            completedSetCount = completedSets.size,
            totalVolume = totalVolume,
            totalVolumeKg = totalVolumeKg,
            weightUnit = unit,
            personalRecordCount = completedSets.count { it.isPr }
        )
    }

    private suspend fun recordUserStats(summary: WorkoutCompletionSummary) {

        val profile = userProfileRepository.getCurrentProfileOnce() ?: return

        userProfileRepository.recordWorkoutCompleted(
            id = profile.id,
            volume = summary.totalVolumeKg,
            duration = summary.durationSeconds,
            prCount = summary.personalRecordCount
        )
    }
}
