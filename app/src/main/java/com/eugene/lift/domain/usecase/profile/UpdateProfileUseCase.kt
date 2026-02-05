package com.eugene.lift.domain.usecase.profile

import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case to update user profile information.
 */
class UpdateProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend fun updateDisplayName(id: String, displayName: String) {
        repository.updateDisplayName(id, displayName)
    }

    suspend fun updateBio(id: String, bio: String?) {
        repository.updateBio(id, bio)
    }

    suspend fun updateAvatarUrl(id: String, avatarUrl: String?) {
        repository.updateAvatarUrl(id, avatarUrl)
    }

    suspend fun updateProfile(profile: UserProfile) {
        repository.updateProfile(profile)
    }
}
