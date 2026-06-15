package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.repository.ExerciseRepository
import javax.inject.Inject

/**
 * Orchestrates the automatic enrichment of exercise data with visual assets.
 *
 * This use case handles the post-seeding logic to ensure that exercises added without
 * explicit image paths (e.g., during the initial database creation or migration) are
 * correctly mapped to the available app drawables.
 *
 * It is designed to be idempotent and efficient by only querying for exercises that
 * are currently missing an image path, minimizing database I/O on subsequent app launches.
 */
class AssignMissingImagesUseCase @Inject constructor(
    private val repository: ExerciseRepository,
    private val imageResolver: ExerciseImageResolver
) {
    /**
     * Executes the mapping process for all unassigned exercises currently in the repository.
     */
    suspend operator fun invoke() {
        val unassigned = repository.getExercisesWithoutImage()
        unassigned.forEach { exercise ->
            val drawable = exercise.seedKey?.let(imageResolver::resolveDrawableForSeedKey)
                ?: imageResolver.resolveDrawable(exercise.name)
            if (drawable != null) {
                repository.updateImagePath(exercise.id, drawable)
            }
        }
    }
}
