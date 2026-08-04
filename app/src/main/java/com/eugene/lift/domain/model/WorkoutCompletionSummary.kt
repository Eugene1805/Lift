package com.eugene.lift.domain.model

data class WorkoutCompletionSummary(
    val workoutName: String,
    val durationSeconds: Long,
    val completedExerciseCount: Int,
    val completedSetCount: Int,
    val totalVolume: Double,
    val totalVolumeKg: Double,
    val weightUnit: WeightUnit,
    val personalRecordCount: Int
)
