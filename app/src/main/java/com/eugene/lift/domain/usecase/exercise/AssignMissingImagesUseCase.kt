package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.data.local.ExerciseImageMapper
import com.eugene.lift.domain.repository.ExerciseRepository
import javax.inject.Inject

/**
 * Finds every exercise with no image and assigns a drawable name from
 * [ExerciseImageMapper] when a match exists.
 *
 * Idempotent: only exercises with [imagePath == null] are fetched, so
 * re-running has zero cost once all mappable exercises have been updated.
 */
class AssignMissingImagesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke() {
        val unassigned = repository.getExercisesWithoutImage()
        unassigned.forEach { exercise ->
            val drawable = ExerciseImageMapper.getDrawable(exercise.name)
            if (drawable != null) {
                repository.updateImagePath(exercise.id, drawable)
            }
        }
    }
}
