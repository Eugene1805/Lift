package com.eugene.lift.data.remote.dto

data class WgerPaginatedResponseDto<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)
