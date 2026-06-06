package com.eugene.lift.data.remote.api

import com.eugene.lift.data.remote.dto.WgerExerciseDto
import com.eugene.lift.data.remote.dto.WgerLanguageDto
import com.eugene.lift.data.remote.dto.WgerPaginatedResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WgerApiService {
    @GET("language/")
    suspend fun getLanguages(
        @Query("limit") limit: Int = 100
    ): WgerPaginatedResponseDto<WgerLanguageDto>

    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): WgerPaginatedResponseDto<WgerExerciseDto>
}
