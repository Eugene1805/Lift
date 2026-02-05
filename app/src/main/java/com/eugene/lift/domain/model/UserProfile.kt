package com.eugene.lift.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Authentication provider types supported by the app.
 */
enum class AuthProvider {
    LOCAL,      // Offline/local account (no sync)
    EMAIL,      // Email + password
    GOOGLE,     // Google OAuth
    FACEBOOK    // Facebook OAuth
}

/**
 * Represents a user profile in the app.
 * Supports offline-first approach with optional cloud sync.
 */
data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val avatarColor: String = "#6200EE", // Default profile color for generated avatar
    val bio: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // Authentication
    val authProvider: AuthProvider = AuthProvider.LOCAL,
    val authProviderId: String? = null, // External auth provider user ID
    val isEmailVerified: Boolean = false,
    val lastSyncedAt: LocalDateTime? = null,

    // Stats
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0, // in kg
    val totalDuration: Long = 0L, // in seconds
    val totalPRs: Int = 0,
    val currentStreak: Int = 0, // consecutive weeks
    val longestStreak: Int = 0,
    val lastWorkoutDate: LocalDate? = null,

    // Social (for future followers/following feature)
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isPublic: Boolean = false
)
