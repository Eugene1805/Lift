package com.eugene.lift.data.remote.mapper

import com.eugene.lift.data.remote.RemoteExercisePage
import com.eugene.lift.data.remote.RemoteExercisePayload
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.ExerciseSource
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.data.remote.dto.WgerExerciseDto
import com.eugene.lift.data.remote.dto.WgerMuscleDto
import com.eugene.lift.data.remote.dto.WgerPaginatedResponseDto
import java.util.UUID

fun WgerPaginatedResponseDto<WgerExerciseDto>.toRemoteExercisePage(): RemoteExercisePage {
    return RemoteExercisePage(
        totalCount = count,
        nextPageUrl = next,
        previousPageUrl = previous,
        exercises = results.map { it.toRemoteExercisePayload() }
    )
}

fun WgerExerciseDto.toRemoteExercisePayload(): RemoteExercisePayload {
    val translation = translations.firstOrNull { it.name.isNotBlank() }
    val mainImageUrl = images.firstOrNull { it.is_main == true }?.image
        ?: images.firstOrNull()?.image

    return RemoteExercisePayload(
        remoteId = id,
        name = translation?.name ?: "Exercise #$id",
        description = translation?.description,
        categoryName = category?.name,
        primaryImageUrl = mainImageUrl,
        equipmentNames = equipment.map { it.name },
        primaryMuscleNames = muscles.map { it.displayName() },
        secondaryMuscleNames = muscles_secondary.map { it.displayName() }
    )
}

fun RemoteExercisePayload.toExercise(
    existingLocalId: String? = null,
    syncedAt: Long? = null,
    syncVersion: Int? = null
): Exercise {
    val category = mapExerciseCategory(categoryName, equipmentNames)

    return Exercise(
        id = existingLocalId ?: UUID.randomUUID().toString(),
        name = name,
        category = category,
        measureType = mapMeasureType(category),
        instructions = description.orEmpty(),
        imagePath = primaryImageUrl,
        bodyParts = mapBodyParts(primaryMuscleNames + secondaryMuscleNames),
        remoteId = remoteId,
        source = ExerciseSource.WGER,
        lastSyncedAt = syncedAt,
        syncVersion = syncVersion
    )
}

private fun WgerMuscleDto.displayName(): String {
    return name_en?.takeIf { it.isNotBlank() } ?: name
}

private fun mapExerciseCategory(
    categoryName: String?,
    equipmentNames: List<String>
): ExerciseCategory {
    val categoryToken = categoryName.orEmpty().lowercase()
    val equipmentTokens = equipmentNames.map { it.lowercase() }
    val combinedTokens = buildList {
        add(categoryToken)
        addAll(equipmentTokens)
    }

    return when {
        combinedTokens.any { it.contains("cardio") || it.contains("bike") || it.contains("treadmill") || it.contains("rower") || it.contains("ergometer") } -> ExerciseCategory.CARDIO
        combinedTokens.any { it.contains("barbell") || it.contains("ez bar") || it.contains("olympic bar") || it.contains("trap bar") } -> ExerciseCategory.BARBELL
        combinedTokens.any { it.contains("dumbbell") || it.contains("kettlebell") } -> ExerciseCategory.DUMBBELL
        combinedTokens.any { it.contains("body weight") || it.contains("bodyweight") || it == "none" } -> ExerciseCategory.BODYWEIGHT
        combinedTokens.any { it.contains("machine") || it.contains("cable") || it.contains("band") || it.contains("bench") || it.contains("rack") || it.contains("box") || it.contains("ball") || it.contains("roller") || it.contains("rope") } -> ExerciseCategory.MACHINE
        categoryToken.contains("stretch") || categoryToken.contains("mobility") || categoryToken.contains("warm") -> ExerciseCategory.DURATION
        equipmentNames.isEmpty() -> ExerciseCategory.BODYWEIGHT
        else -> ExerciseCategory.REPS_ONLY
    }
}

private fun mapMeasureType(category: ExerciseCategory): MeasureType {
    return when (category) {
        ExerciseCategory.CARDIO -> MeasureType.DISTANCE_TIME
        ExerciseCategory.DURATION -> MeasureType.TIME
        ExerciseCategory.BODYWEIGHT,
        ExerciseCategory.REPS_ONLY -> MeasureType.REPS_ONLY
        else -> MeasureType.REPS_AND_WEIGHT
    }
}

private fun mapBodyParts(muscleNames: List<String>): List<BodyPart> {
    val mapped = muscleNames.mapNotNull(::mapBodyPart)
        .distinct()

    return if (mapped.isEmpty()) {
        listOf(BodyPart.OTHER)
    } else {
        mapped
    }
}

private fun mapBodyPart(rawName: String): BodyPart? {
    val normalized = rawName.lowercase()

    return when {
        normalized.contains("biceps femoris") || normalized.contains("hamstring") || normalized.contains("semitendinosus") || normalized.contains("semimembranosus") -> BodyPart.HAMSTRINGS
        normalized.contains("pectoralis") || normalized.contains("chest") -> BodyPart.CHEST
        normalized.contains("latissimus") || normalized.contains("lats") -> BodyPart.LATS
        normalized.contains("trapezius") || normalized.contains("trap") -> BodyPart.TRAPS
        normalized.contains("anterior deltoid") || normalized.contains("front deltoid") -> BodyPart.FRONT_DELTS
        normalized.contains("lateral deltoid") || normalized.contains("middle deltoid") || normalized.contains("side deltoid") -> BodyPart.SIDE_DELTS
        normalized.contains("posterior deltoid") || normalized.contains("rear deltoid") -> BodyPart.REAR_DELTS
        normalized.contains("deltoid") || normalized.contains("shoulder") -> BodyPart.SHOULDERS
        normalized.contains("triceps") -> BodyPart.TRICEPS
        normalized.contains("biceps") -> BodyPart.BICEPS
        normalized.contains("forearm") || normalized.contains("brachioradialis") || normalized.contains("wrist") -> BodyPart.FOREARMS
        normalized.contains("quadriceps") -> BodyPart.QUADRICEPS
        normalized.contains("glute") -> BodyPart.GLUTES
        normalized.contains("gastrocnemius") || normalized.contains("soleus") || normalized.contains("calf") || normalized.contains("calves") -> BodyPart.CALVES
        normalized.contains("adductor") -> BodyPart.ADDUCTORS
        normalized.contains("abductor") -> BodyPart.ABDUCTORS
        normalized.contains("erector spinae") || normalized.contains("lower back") || normalized.contains("lumbar") -> BodyPart.LOWER_BACK
        normalized.contains("back") -> BodyPart.BACK
        normalized.contains("oblique") || normalized.contains("abdom") || normalized.contains("core") || normalized.contains("transverse") -> BodyPart.CORE
        normalized.contains("neck") || normalized.contains("sternocleidomastoid") -> BodyPart.NECK
        normalized.contains("cardio") -> BodyPart.CARDIO
        normalized.contains("full body") -> BodyPart.FULL_BODY
        else -> null
    }
}
