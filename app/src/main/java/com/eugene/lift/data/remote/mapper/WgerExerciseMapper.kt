package com.eugene.lift.data.remote.mapper

import com.eugene.lift.data.remote.RemoteExercisePage
import com.eugene.lift.data.remote.RemoteExercisePayload
import com.eugene.lift.data.remote.dto.WgerExerciseDto
import com.eugene.lift.data.remote.dto.WgerPaginatedResponseDto

fun WgerPaginatedResponseDto<WgerExerciseDto>.toRemoteExercisePage(): RemoteExercisePage {
    return RemoteExercisePage(
        totalCount = count,
        nextPageUrl = next,
        previousPageUrl = previous,
        exercises = results.map { it.toRemoteExercisePayload() }
    )
}

fun WgerExerciseDto.toRemoteExercisePayload(): RemoteExercisePayload {
    val mainImageUrl = images.firstOrNull { it.is_main == true }?.image
        ?: images.firstOrNull()?.image
        ?: variations.firstOrNull()?.images?.firstOrNull()?.image

    return RemoteExercisePayload(
        remoteId = id,
        name = name,
        description = description,
        categoryName = category?.name,
        primaryImageUrl = mainImageUrl,
        equipmentNames = equipment.map { it.name },
        primaryMuscleNames = muscles.map { it.name },
        secondaryMuscleNames = muscles_secondary.map { it.name }
    )
}
