package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.WorkoutSession
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class StartEmptyWorkoutUseCase @Inject constructor() {
    operator fun invoke(): WorkoutSession {
        return WorkoutSession(
            id = UUID.randomUUID().toString(),
            templateId = null,
            name = "Quick Workout", // TODO: Usar String Resource o fecha
            date = LocalDateTime.now(),
            durationSeconds = 0,
            exercises = emptyList()
        )
    }
}