package com.eugene.lift.domain.model

data class ActiveWorkoutDraft(
    val session: WorkoutSession,
    val originalTemplateExercises: List<SessionExercise>,
    val startedAtEpochMillis: Long,
    val lastInteractedAtEpochMillis: Long
) {
    fun toSummary(): ActiveWorkoutDraftSummary {
        return ActiveWorkoutDraftSummary(
            sessionId = session.id,
            sessionName = session.name,
            exerciseCount = session.exercises.size,
            lastInteractedAtEpochMillis = lastInteractedAtEpochMillis
        )
    }
}

data class ActiveWorkoutDraftSummary(
    val sessionId: String,
    val sessionName: String,
    val exerciseCount: Int,
    val lastInteractedAtEpochMillis: Long
)
