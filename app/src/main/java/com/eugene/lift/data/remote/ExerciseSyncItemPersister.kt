package com.eugene.lift.data.remote

import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.mapper.toCrossRefs
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.Exercise
import javax.inject.Inject

interface ExerciseSyncItemPersister {
    suspend fun persist(exercise: Exercise)
    suspend fun deleteExercise(exerciseId: String)
}

class RoomExerciseSyncItemPersister @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseSyncItemPersister {
    override suspend fun persist(exercise: Exercise) {
        exerciseDao.saveExerciseComplete(
            exercise = exercise.toEntity(),
            refs = exercise.toCrossRefs()
        )
    }

    override suspend fun deleteExercise(exerciseId: String) {
        exerciseDao.deleteExerciseComplete(exerciseId)
    }
}
