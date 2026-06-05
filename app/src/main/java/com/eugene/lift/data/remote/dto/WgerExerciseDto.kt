package com.eugene.lift.data.remote.dto

data class WgerExerciseDto(
    val id: Int,
    val uuid: String? = null,
    val category: WgerNamedResourceDto? = null,
    val muscles: List<WgerMuscleDto> = emptyList(),
    val muscles_secondary: List<WgerMuscleDto> = emptyList(),
    val equipment: List<WgerNamedResourceDto> = emptyList(),
    val images: List<WgerImageDto> = emptyList(),
    val translations: List<WgerExerciseTranslationDto> = emptyList(),
    val variations: Int? = null
)

data class WgerNamedResourceDto(
    val id: Int,
    val name: String
)

data class WgerMuscleDto(
    val id: Int,
    val name: String,
    val name_en: String? = null
)

data class WgerImageDto(
    val id: Int,
    val image: String,
    val is_main: Boolean? = null
)

data class WgerExerciseTranslationDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val language: Int? = null
)
