package com.eugene.lift.data.remote

import androidx.room.withTransaction
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.dao.TemplateDao
import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.domain.model.Exercise
import javax.inject.Inject

data class ExerciseCatalogMerge(
    val canonicalExerciseId: String,
    val duplicateExerciseIds: List<String>
)

interface ExerciseSyncWriter {
    suspend fun applySyncedExercises(
        exercises: List<Exercise>,
        merges: List<ExerciseCatalogMerge> = emptyList()
    )
}

class RoomExerciseSyncWriter @Inject constructor(
    private val database: AppDatabase,
    private val itemPersister: ExerciseSyncItemPersister,
    private val templateDao: TemplateDao,
    private val workoutDao: WorkoutDao
) : ExerciseSyncWriter {
    override suspend fun applySyncedExercises(
        exercises: List<Exercise>,
        merges: List<ExerciseCatalogMerge>
    ) {
        database.withTransaction {
            merges.forEach { merge ->
                merge.duplicateExerciseIds
                    .distinct()
                    .filter { duplicateId -> duplicateId != merge.canonicalExerciseId }
                    .forEach { duplicateId ->
                        templateDao.reassignExerciseReferences(duplicateId, merge.canonicalExerciseId)
                        workoutDao.reassignExerciseReferences(duplicateId, merge.canonicalExerciseId)
                        itemPersister.deleteExercise(duplicateId)
                    }
            }

            exercises.forEach { exercise ->
                itemPersister.persist(exercise)
            }
        }
    }
}
