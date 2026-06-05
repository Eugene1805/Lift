package com.eugene.lift.data.remote

import com.eugene.lift.data.remote.api.WgerApiService
import com.eugene.lift.data.remote.mapper.toRemoteExercisePage
import javax.inject.Inject

class WgerExerciseRemoteDataSource @Inject constructor(
    private val apiService: WgerApiService
) : ExerciseRemoteDataSource {

    override suspend fun syncExercises(): List<RemoteExercisePayload> {
        val exercises = mutableListOf<RemoteExercisePayload>()
        var offset = 0

        do {
            val page = apiService.getExercises(
                limit = ExerciseSyncDataSource.DEFAULT_PAGE_SIZE,
                offset = offset
            ).toRemoteExercisePage()

            exercises += page.exercises
            offset += page.exercises.size
        } while (page.nextPageUrl != null && page.exercises.isNotEmpty())

        return exercises
    }
}
