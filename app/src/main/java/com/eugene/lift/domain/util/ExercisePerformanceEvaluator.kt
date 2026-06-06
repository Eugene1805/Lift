package com.eugene.lift.domain.util

import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.WorkoutSet
import javax.inject.Inject
import kotlin.math.abs

class ExercisePerformanceEvaluator @Inject constructor() {

    fun supportsPrTracking(measureType: MeasureType): Boolean {
        return when (measureType) {
            MeasureType.REPS_AND_WEIGHT,
            MeasureType.REPS_ONLY -> true
            MeasureType.TIME,
            MeasureType.DISTANCE_TIME -> false
        }
    }

    fun bestCompletedSet(sets: List<WorkoutSet>, measureType: MeasureType): WorkoutSet? {
        if (!supportsPrTracking(measureType)) {
            return null
        }

        return sets
            .asSequence()
            .filter { it.completed }
            .filter { performanceValue(it, measureType) > 0.0 }
            .maxByOrNull { performanceValue(it, measureType) }
    }

    fun bestCompletedValue(sets: List<WorkoutSet>, measureType: MeasureType): Double {
        return bestCompletedSet(sets, measureType)
            ?.let { set -> performanceValue(set, measureType) }
            ?: 0.0
    }

    fun performanceValue(set: WorkoutSet, measureType: MeasureType): Double {
        return when (measureType) {
            MeasureType.REPS_AND_WEIGHT -> estimatedOneRepMax(set.weight, set.reps)
            MeasureType.REPS_ONLY -> set.reps.toDouble()
            MeasureType.TIME,
            MeasureType.DISTANCE_TIME -> 0.0
        }
    }

    fun matchesPerformance(set: WorkoutSet, expectedValue: Double, measureType: MeasureType): Boolean {
        return abs(performanceValue(set, measureType) - expectedValue) < EPSILON
    }

    fun estimatedOneRepMax(weight: Double, reps: Int): Double {
        if (reps <= 0 || weight <= 0.0) return 0.0
        return weight * (1.0 + reps / 30.0)
    }

    private companion object {
        const val EPSILON = 0.0001
    }
}
