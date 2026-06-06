package com.eugene.lift.data.remote

import com.eugene.lift.core.util.Logger
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.remote.mapper.toExercise
import com.eugene.lift.domain.model.ExerciseSource
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.util.ExerciseNameNormalizer
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private const val CURRENT_SYNC_VERSION = 1

class WgerExerciseCatalogSyncer @Inject constructor(
    private val remoteDataSource: ExerciseRemoteDataSource,
    private val exerciseDao: ExerciseDao,
    private val exerciseSyncWriter: ExerciseSyncWriter,
    private val settingsRepository: SettingsRepository,
    private val nameNormalizer: ExerciseNameNormalizer,
    private val logger: Logger
) : ExerciseCatalogSyncer {

    override suspend fun syncExercises(): Int {
        val payloads = remoteDataSource.syncExercises()
            .distinctBy { payload -> payload.remoteId }

        if (payloads.isEmpty()) {
            return 0
        }

        val existingExercises = exerciseDao.getAllExerciseEntities()
        val existingByRemoteId = existingExercises
            .filter { entity -> entity.remoteId != null }
            .associateBy { entity -> requireNotNull(entity.remoteId) }
        val existingByNormalizedName = existingExercises
            .groupBy { entity ->
                nameNormalizer.normalize(entity.name).ifBlank { entity.id }
            }
        val trackedIds = settingsRepository.getTrackedExerciseIds().first().toSet()

        val syncedAt = System.currentTimeMillis()
        val mergeMappings = linkedMapOf<String, MutableSet<String>>()
        val claimedCanonicalIds = mutableSetOf<String>()
        val mappedExercises = buildList {
            payloads.forEach { payload ->
                try {
                    val resolution = resolveExistingExercise(
                        payload = payload,
                        existingByRemoteId = existingByRemoteId,
                        existingByNormalizedName = existingByNormalizedName,
                        claimedCanonicalIds = claimedCanonicalIds,
                        trackedIds = trackedIds
                    )
                    val canonicalId = resolution.canonicalExercise?.id
                    canonicalId?.let {
                        claimedCanonicalIds += canonicalId
                    }
                    resolution.duplicateExerciseIds.forEach { duplicateId ->
                        if (canonicalId != null && duplicateId != canonicalId) {
                            mergeMappings
                                .getOrPut(canonicalId) { linkedSetOf() }
                                .add(duplicateId)
                        }
                    }
                    add(
                        payload.toExercise(
                            existingExercise = resolution.canonicalExercise,
                            syncedAt = syncedAt,
                            syncVersion = CURRENT_SYNC_VERSION
                        )
                    )
                } catch (exception: Exception) {
                    logger.log(
                        IllegalStateException(
                            "Failed to map remote exercise ${payload.remoteId}",
                            exception
                        )
                    )
                }
            }
        }

        val merges = mergeMappings.map { (canonicalId, duplicateIds) ->
            ExerciseCatalogMerge(
                canonicalExerciseId = canonicalId,
                duplicateExerciseIds = duplicateIds.toList()
            )
        }

        exerciseSyncWriter.applySyncedExercises(mappedExercises, merges)
        reconcileTrackedExerciseIds(merges)
        return mappedExercises.size
    }

    private suspend fun reconcileTrackedExerciseIds(merges: List<ExerciseCatalogMerge>) {
        if (merges.isEmpty()) return

        val replacements = merges.flatMap { merge ->
            merge.duplicateExerciseIds.map { duplicateId ->
                duplicateId to merge.canonicalExerciseId
            }
        }.toMap()
        if (replacements.isEmpty()) return

        val trackedIds = settingsRepository.getTrackedExerciseIds().first()
        val updatedTrackedIds = trackedIds
            .map { trackedId -> replacements[trackedId] ?: trackedId }
            .distinct()

        if (updatedTrackedIds != trackedIds) {
            settingsRepository.setTrackedExerciseIds(updatedTrackedIds)
        }
    }

    private fun resolveExistingExercise(
        payload: RemoteExercisePayload,
        existingByRemoteId: Map<Int, com.eugene.lift.data.local.entity.ExerciseEntity>,
        existingByNormalizedName: Map<String, List<com.eugene.lift.data.local.entity.ExerciseEntity>>,
        claimedCanonicalIds: Set<String>,
        trackedIds: Set<String>
    ): ExerciseSyncResolution {
        existingByRemoteId[payload.remoteId]?.let { exactMatch ->
            return ExerciseSyncResolution(
                canonicalExercise = exactMatch,
                duplicateExerciseIds = emptyList()
            )
        }

        val normalizedName = nameNormalizer.normalize(payload.name).ifBlank { payload.remoteId.toString() }
        val sameNameCandidates = existingByNormalizedName[normalizedName]
            .orEmpty()
            .filter { entity -> entity.remoteId == null || entity.remoteId == payload.remoteId }
            .filter { entity -> entity.id !in claimedCanonicalIds }
        if (sameNameCandidates.isEmpty()) {
            return ExerciseSyncResolution(
                canonicalExercise = null,
                duplicateExerciseIds = emptyList()
            )
        }

        val canonical = sameNameCandidates
            .sortedWith(
                compareByDescending<com.eugene.lift.data.local.entity.ExerciseEntity> { entity ->
                    entity.id in trackedIds
                }
                    .thenByDescending { entity -> entity.source == ExerciseSource.WGER }
                    .thenByDescending { entity -> entity.lastSyncedAt ?: Long.MIN_VALUE }
                    .thenBy { entity -> entity.id }
            )
            .first()

        return ExerciseSyncResolution(
            canonicalExercise = canonical,
            duplicateExerciseIds = sameNameCandidates.map { entity -> entity.id }
        )
    }
}

private data class ExerciseSyncResolution(
    val canonicalExercise: com.eugene.lift.data.local.entity.ExerciseEntity?,
    val duplicateExerciseIds: List<String>
)
