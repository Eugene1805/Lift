package com.eugene.lift.data.remote

interface ExerciseRemoteDataSource {
    suspend fun fetchExercises(
        languageId: Int = DEFAULT_LANGUAGE_ID,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): RemoteExercisePage

    companion object {
        const val DEFAULT_LANGUAGE_ID = 2
        const val DEFAULT_PAGE_SIZE = 20
    }
}
