package com.eugene.lift.domain.model

data class TemplateExercise(
    val id: String,
    val exercise: Exercise,
    val orderIndex: Int,
    val targetSets: Int = 3,
    val targetReps: String = "6-10",
    val restTimerSeconds: Int = 90,
    val note: String = ""
)