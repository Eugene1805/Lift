package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.AuthProvider
import com.eugene.lift.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface UserProfileRepository {

    fun getCurrentProfile(): Flow<UserProfile?>

    fun getProfileById(id: String): Flow<UserProfile?>

    suspend fun getCurrentProfileOnce(): UserProfile?

    suspend fun getOrCreateProfile(): UserProfile

    suspend fun updateProfile(profile: UserProfile)

    suspend fun updateDisplayName(id: String, displayName: String)

    suspend fun updateBio(id: String, bio: String?)

    suspend fun updateAvatarUrl(id: String, avatarUrl: String?)

    suspend fun updateUsername(id: String, username: String)

    suspend fun recordWorkoutCompleted(
        id: String,
        volume: Double,
        duration: Long,
        prCount: Int
    )
    suspend fun updateStreak(id: String, streak: Int)
    
    fun generateUsernameSuggestions(count: Int = 5): List<String>
}
