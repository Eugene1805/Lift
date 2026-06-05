package com.eugene.lift.data.remote.dto

data class WgerExerciseDto(
    val id: Int,
    val uuid: String? = null,
    val name: String,
    val description: String? = null,
    val category: WgerNamedResourceDto? = null,
    val muscles: List<WgerNamedResourceDto> = emptyList(),
    val muscles_secondary: List<WgerNamedResourceDto> = emptyList(),
    val equipment: List<WgerNamedResourceDto> = emptyList(),
    val images: List<WgerImageDto> = emptyList(),
    val variations: List<WgerExerciseVariationDto> = emptyList()
)

data class WgerNamedResourceDto(
    val id: Int,
    val name: String
)

data class WgerImageDto(
    val id: Int,
    val image: String,
    val is_main: Boolean? = null
)

data class WgerExerciseVariationDto(
    val id: Int,
    val images: List<WgerImageDto> = emptyList()
)
