package com.eugene.lift.domain.model

data class TimerState(
    val isRunning: Boolean = false,
    val timeRemainingSeconds: Long = 0,
    val totalTimeSeconds: Long = 0, // Para calcular el progreso (barra circular)
    val progress: Float = 0f // 0.0 a 1.0
)