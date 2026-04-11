package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.model.ExerciseProgression
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.PrRecord
import com.eugene.lift.domain.model.ProgressionDataPoint
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Computes the progression of a specific exercise across all workout history.
 *
 * For REPS_AND_WEIGHT / BODYWEIGHT exercises:
 *   - Best set per session = highest estimated 1RM (Epley formula)
 *   - Chart Y axis = estimated 1RM
 *
 * For REPS_ONLY exercises:
 *   - Best set per session = highest rep count
 *   - Chart Y axis = max reps
 *
 * PR history = subset of sessions where the best set exceeded any previous best.
 */
class GetExerciseProgressionUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * Returns a cold [Flow] that emits [ExerciseProgression] whenever history changes.
     *
     * @param exerciseId  ID of the exercise to track.
     * @param exerciseName Display name (passed in to avoid an extra repository lookup).
     * @param measureType  Determines whether weight or reps is used for comparison.
     */
    operator fun invoke(
        exerciseId: String,
        exerciseName: String,
        measureType: MeasureType
    ): Flow<ExerciseProgression> {
        return workoutRepository.getExerciseHistory(exerciseId).map { sessions ->
            buildProgression(exerciseId, exerciseName, measureType, sessions)
        }
    }

    fun buildProgression(
        exerciseId: String,
        exerciseName: String,
        measureType: MeasureType,
        sessions: List<WorkoutSession>
    ): ExerciseProgression {
        val isWeightBased = measureType == MeasureType.REPS_AND_WEIGHT

        // Sort chronologically for correct PR detection
        val chronological = sessions.sortedBy { it.date }

        val dataPoints = mutableListOf<ProgressionDataPoint>()
        val prHistory = mutableListOf<PrRecord>()
        var allTimeBestValue = Double.MIN_VALUE

        for (session in chronological) {
            val sessionExercise = session.exercises
                .firstOrNull { it.exercise.id == exerciseId }
                ?: continue

            val completedSets = sessionExercise.sets.filter { it.completed }
            if (completedSets.isEmpty()) continue

            val bestSet = if (isWeightBased) {
                completedSets.maxByOrNull { estimatedOneRepMax(it.weight, it.reps) }
            } else {
                completedSets.maxByOrNull { it.reps }
            } ?: continue

            val e1RM = if (isWeightBased) estimatedOneRepMax(bestSet.weight, bestSet.reps) else 0.0
            val comparisonValue = if (isWeightBased) e1RM else bestSet.reps.toDouble()

            val date = session.date.toLocalDate()
            dataPoints.add(
                ProgressionDataPoint(
                    date = date,
                    weight = bestSet.weight,
                    reps = bestSet.reps,
                    estimatedOneRepMax = e1RM,
                    sessionName = session.name
                )
            )

            if (comparisonValue > allTimeBestValue) {
                allTimeBestValue = comparisonValue
                prHistory.add(
                    PrRecord(
                        date = date,
                        weight = bestSet.weight,
                        reps = bestSet.reps,
                        sessionName = session.name
                    )
                )
            }
        }

        val currentPr = prHistory.lastOrNull()

        return ExerciseProgression(
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            dataPoints = dataPoints,
            prHistory = prHistory.asReversed(),
            currentPr = currentPr
        )
    }

    /**
     * Epley formula: e1RM = weight × (1 + reps / 30).
     * Returns [weight] unchanged when [reps] == 1 (already a 1RM attempt).
     */
    fun estimatedOneRepMax(weight: Double, reps: Int): Double {
        if (reps <= 0 || weight <= 0.0) return 0.0
        return weight * (1.0 + reps / 30.0)
    }

    companion object {
        /** Maximum number of exercises that can be tracked simultaneously. */
        const val MAX_TRACKED = 5
    }
}
