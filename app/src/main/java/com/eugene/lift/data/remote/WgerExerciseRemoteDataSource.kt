package com.eugene.lift.data.remote

import com.eugene.lift.data.remote.api.WgerApiService
import com.eugene.lift.data.remote.mapper.toRemoteExercisePage
import javax.inject.Inject

class WgerExerciseRemoteDataSource @Inject constructor(
    private val apiService: WgerApiService
) : ExerciseRemoteDataSource {

    override suspend fun fetchExercises(
        languageId: Int,
        limit: Int,
        offset: Int
    ): RemoteExercisePage {
        return apiService.getExercises(
            limit = limit,
            offset = offset,
            languageId = languageId
        ).toRemoteExercisePage()
    }
}
