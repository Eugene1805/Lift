package com.eugene.lift.domain.model

data class SessionExercise(
    val id: String,
    val exercise: Exercise,
    val sets: List<WorkoutSet>
)