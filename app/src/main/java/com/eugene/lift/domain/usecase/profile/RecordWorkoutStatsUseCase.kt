package com.eugene.lift.domain.usecase.profile

import com.eugene.lift.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case to record workout stats when a workout is completed.
 * This is called from the active workout screen when finishing a workout.
 */
class RecordWorkoutStatsUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(
        profileId: String,
        volume: Double,
        duration: Long,
        prCount: Int
    ) {
        repository.recordWorkoutCompleted(profileId, volume, duration, prCount)
    }
}
