package com.eugene.lift.domain.usecase.profile

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case to update user profile information.
 */
class UpdateProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository,
    private val safeExecutor: SafeExecutor
) {
    suspend fun updateDisplayName(id: String, displayName: String): AppResult<Unit> {
        return safeExecutor.execute {
            repository.updateDisplayName(id, displayName)
        }
    }

    suspend fun updateBio(id: String, bio: String?): AppResult<Unit> {
        return safeExecutor.execute {
            repository.updateBio(id, bio)
        }
    }

    suspend fun updateAvatarUrl(id: String, avatarUrl: String?): AppResult<Unit> {
        return safeExecutor.execute {
            repository.updateAvatarUrl(id, avatarUrl)
        }
    }

    suspend fun updateProfile(profile: UserProfile): AppResult<Unit> {
        return safeExecutor.execute {
            repository.updateProfile(profile)
        }
    }
}
