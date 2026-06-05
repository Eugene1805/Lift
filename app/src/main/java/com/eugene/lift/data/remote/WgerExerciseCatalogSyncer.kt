package com.eugene.lift.data.remote

import com.eugene.lift.core.util.Logger
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.remote.mapper.toExercise
import javax.inject.Inject

private const val CURRENT_SYNC_VERSION = 1

class WgerExerciseCatalogSyncer @Inject constructor(
    private val remoteDataSource: ExerciseRemoteDataSource,
    private val exerciseDao: ExerciseDao,
    private val exerciseSyncWriter: ExerciseSyncWriter,
    private val logger: Logger
) : ExerciseCatalogSyncer {

    override suspend fun syncExercises(): Int {
        val payloads = remoteDataSource.syncExercises()
            .distinctBy { payload -> payload.remoteId }

        if (payloads.isEmpty()) {
            return 0
        }

        val existingByRemoteId = exerciseDao
            .getExercisesByRemoteIds(payloads.map { payload -> payload.remoteId })
            .associateBy { entity -> requireNotNull(entity.remoteId) }

        val syncedAt = System.currentTimeMillis()
        val mappedExercises = buildList {
            payloads.forEach { payload ->
                try {
                    add(
                        payload.toExercise(
                            existingLocalId = existingByRemoteId[payload.remoteId]?.id,
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

        exerciseSyncWriter.applySyncedExercises(mappedExercises)
        return mappedExercises.size
    }
}
