package com.eugene.lift.data.remote

data class RemoteExercisePage(
    val totalCount: Int,
    val nextPageUrl: String?,
    val previousPageUrl: String?,
    val exercises: List<RemoteExercisePayload>
)

data class RemoteExercisePayload(
    val remoteId: Int,
    val name: String,
    val description: String?,
    val categoryName: String?,
    val primaryImageUrl: String?,
    val equipmentNames: List<String>,
    val primaryMuscleNames: List<String>,
    val secondaryMuscleNames: List<String>
)
