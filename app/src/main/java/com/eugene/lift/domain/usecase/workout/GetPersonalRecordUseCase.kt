package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.WorkoutRepository
import com.eugene.lift.domain.util.ExercisePerformanceEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPersonalRecordUseCase @Inject constructor(
    private val repository: WorkoutRepository,
    private val performanceEvaluator: ExercisePerformanceEvaluator
) {
    suspend operator fun invoke(exerciseId: String, measureType: MeasureType): WorkoutSet? {
        if (!performanceEvaluator.supportsPrTracking(measureType)) {
            return null
        }

        return repository.getExerciseHistory(exerciseId)
            .collectBestSet(exerciseId, measureType, performanceEvaluator)
    }
}

private suspend fun Flow<List<WorkoutSession>>.collectBestSet(
    exerciseId: String,
    measureType: MeasureType,
    performanceEvaluator: ExercisePerformanceEvaluator
): WorkoutSet? {
    return first()
        .flatMap { session ->
            session.exercises.filter { sessionExercise -> sessionExercise.exercise.id == exerciseId }
        }
        .flatMap { sessionExercise -> sessionExercise.sets }
        .filter { set -> set.completed }
        .maxByOrNull { set -> performanceEvaluator.performanceValue(set, measureType) }
}
