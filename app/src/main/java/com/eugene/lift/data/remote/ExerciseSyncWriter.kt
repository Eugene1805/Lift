package com.eugene.lift.data.remote

import androidx.room.withTransaction
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.domain.model.Exercise
import javax.inject.Inject

interface ExerciseSyncWriter {
    suspend fun applySyncedExercises(exercises: List<Exercise>)
}

class RoomExerciseSyncWriter @Inject constructor(
    private val database: AppDatabase,
    private val itemPersister: ExerciseSyncItemPersister
) : ExerciseSyncWriter {
    override suspend fun applySyncedExercises(exercises: List<Exercise>) {
        database.withTransaction {
            exercises.forEach { exercise ->
                itemPersister.persist(exercise)
            }
        }
    }
}
