package com.eugene.lift.data.remote

interface ExerciseSyncDataSource {
    suspend fun syncExercises(): List<RemoteExercisePayload>

    companion object {
        const val DEFAULT_PAGE_SIZE = 100
    }
}
