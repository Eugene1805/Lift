package com.eugene.lift.domain.model

/**
 * Represents a physical exercise within the system.
 *
 * @property id Unique identifier for the exercise.
 * @property name Descriptive name of the exercise.
 * @property category The equipment or modality category.
 * @property measureType How the progress is measured (e.g., weight/reps, time).
 * @property instructions Step-by-step guide on how to perform the exercise.
 * @property imagePath Optional path or URL to a visual demonstration.
 * @property bodyParts List of muscle groups targeted by this exercise.
 * @property remoteId Optional stable identifier from a remote catalog.
 * @property source Origin of the exercise record.
 * @property lastSyncedAt UTC epoch millis of the latest remote sync.
 * @property syncVersion Optional schema/version marker for remote sync bookkeeping.
 */
data class Exercise(
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val measureType: MeasureType,
    val instructions: String,
    val imagePath: String?,
    val bodyParts: List<BodyPart>,
    val remoteId: Int? = null,
    val source: ExerciseSource = ExerciseSource.LOCAL,
    val lastSyncedAt: Long? = null,
    val syncVersion: Int? = null
)
