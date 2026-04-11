package com.eugene.lift.domain.usecase.profile

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository,
    private val safeExecutor: SafeExecutor
) {

    suspend fun updateProfile(profile: UserProfile): AppResult<Unit> {
        return safeExecutor.execute {
            repository.updateProfile(profile)
        }
    }
}
