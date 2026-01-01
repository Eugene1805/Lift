package com.eugene.lift.domain.model

data class Exercise(
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val measureType: MeasureType,
    val instructions: String,
    val imagePath: String?,
    val bodyParts: List<BodyPart>
)
