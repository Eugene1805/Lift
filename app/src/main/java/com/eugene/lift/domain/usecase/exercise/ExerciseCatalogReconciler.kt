package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseSource
import com.eugene.lift.domain.util.ExerciseNameNormalizer
import javax.inject.Inject

data class CatalogDuplicateSuggestion(
    val visibleExercise: Exercise,
    val hiddenExercises: List<Exercise>
)

class ExerciseCatalogReconciler @Inject constructor(
    private val nameNormalizer: ExerciseNameNormalizer
) {

    fun reconcileVisibleExercises(exercises: List<Exercise>): List<Exercise> {
        return exercises
            .groupBy(::catalogKey)
            .values
            .flatMap { group ->
                val suggestion = buildDuplicateSuggestion(group)
                if (suggestion == null) group else listOf(suggestion.visibleExercise)
            }
    }

    fun findPotentialDuplicateSuggestions(exercises: List<Exercise>): List<CatalogDuplicateSuggestion> {
        return exercises
            .groupBy(::catalogKey)
            .values
            .mapNotNull(::buildDuplicateSuggestion)
    }

    private fun buildDuplicateSuggestion(group: List<Exercise>): CatalogDuplicateSuggestion? {
        if (group.size < 2) {
            return null
        }

        val localRepresentative = group
            .filter(::isPlainLocal)
            .firstOrNull()
            ?: return null

        val remoteRepresentative = group
            .filter(::isRemoteBacked)
            .sortedWith(
                compareByDescending<Exercise> { it.imagePath != null }
                    .thenByDescending { it.lastSyncedAt ?: Long.MIN_VALUE }
                    .thenByDescending { it.bodyParts.size }
                    .thenBy { it.id }
            )
            .firstOrNull()
            ?: return null

        return CatalogDuplicateSuggestion(
            visibleExercise = localRepresentative.copy(
                name = localRepresentative.name.ifBlank { remoteRepresentative.name },
                category = remoteRepresentative.category,
                measureType = remoteRepresentative.measureType,
                instructions = remoteRepresentative.instructions.ifBlank { localRepresentative.instructions },
                imagePath = remoteRepresentative.imagePath ?: localRepresentative.imagePath,
                bodyParts = if (remoteRepresentative.bodyParts.isNotEmpty()) {
                    remoteRepresentative.bodyParts
                } else {
                    localRepresentative.bodyParts
                },
                remoteId = remoteRepresentative.remoteId,
                source = remoteRepresentative.source,
                lastSyncedAt = remoteRepresentative.lastSyncedAt,
                syncVersion = remoteRepresentative.syncVersion
            ),
            hiddenExercises = group.filterNot { it.id == localRepresentative.id }
        )
    }

    private fun catalogKey(exercise: Exercise): String {
        val normalized = nameNormalizer.normalize(exercise.name)

        return normalized.ifBlank { exercise.id }
    }

    private fun isPlainLocal(exercise: Exercise): Boolean {
        return exercise.source == ExerciseSource.LOCAL && exercise.remoteId == null
    }

    private fun isRemoteBacked(exercise: Exercise): Boolean {
        return exercise.remoteId != null || exercise.source == ExerciseSource.WGER
    }
}
