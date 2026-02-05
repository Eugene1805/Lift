package com.eugene.lift.domain.usecase.history

import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkoutSessionDetailsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(sessionId: String): Flow<WorkoutSession?> {
        return repository.getSessionDetails(sessionId)
    }
}
