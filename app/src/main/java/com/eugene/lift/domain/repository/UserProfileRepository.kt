package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.AuthProvider
import com.eugene.lift.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Repository interface for user profile operations.
 * Following clean architecture, this is in the domain layer and
 * is implemented by the data layer.
 */
interface UserProfileRepository {

    fun getCurrentProfile(): Flow<UserProfile?>

    fun getProfileById(id: String): Flow<UserProfile?>

    suspend fun getCurrentProfileOnce(): UserProfile?

    suspend fun getOrCreateProfile(): UserProfile

    suspend fun updateProfile(profile: UserProfile)

    suspend fun updateDisplayName(id: String, displayName: String)

    suspend fun updateBio(id: String, bio: String?)

    suspend fun updateAvatarUrl(id: String, avatarUrl: String?)

    // Stats
    suspend fun recordWorkoutCompleted(
        id: String,
        volume: Double,
        duration: Long,
        prCount: Int
    )

    suspend fun updateStreak(id: String, streak: Int)

    // Auth
    suspend fun linkEmailAuth(id: String, email: String, password: String): Boolean

    suspend fun linkSocialAuth(
        id: String,
        provider: AuthProvider,
        providerId: String,
        email: String?
    )

    suspend fun verifyPassword(userId: String, password: String): Boolean

    suspend fun changePassword(userId: String, newPassword: String)

    fun generateUsernameSuggestions(count: Int = 5): List<String>
}
