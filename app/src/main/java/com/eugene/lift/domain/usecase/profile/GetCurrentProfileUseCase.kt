package com.eugene.lift.domain.usecase.profile

import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get the current user profile as a flow.
 * Creates a new local profile if none exists (first app launch).
 */
class GetCurrentProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    operator fun invoke(): Flow<UserProfile?> = repository.getCurrentProfile()

    suspend fun getOrCreate(): UserProfile = repository.getOrCreateProfile()
}
