package com.eugene.lift.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class AuthProvider {
    LOCAL
}

/**
 * Represents a user profile in the app.
 * Supports an offline-first approach with optional cloud synchronization.
 *
 * @property id Unique identifier for the user.
 * @property username Unique handle for the user.
 * @property displayName Full name or chosen display name.
 * @property email Primary email address.
 * @property avatarUrl URL or local path to the profile image.
 * @property avatarColor Hex color code for the default generated avatar.
 * @property bio Short user biography.
 * @property createdAt Timestamp of account creation.
 * @property updatedAt Timestamp of last profile update.
 * @property totalWorkouts Total number of completed workout sessions.
 * @property totalVolume Total weight lifted across all sessions (usually in KG).
 * @property totalDuration Total time spent working out (in seconds).
 * @property totalPRs Total number of Personal Records achieved.
 * @property currentStreak Current consecutive weeks of activity.
 * @property longestStreak All-time record for consecutive active weeks.
 * @property lastWorkoutDate Date of the most recent workout session.
 * @property followersCount Number of users following this profile.
 * @property followingCount Number of profiles this user follows.
 * @property isPublic Whether the profile is visible to other users.
 */
data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val avatarColor: String = "#6200EE",
    val bio: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0,
    val totalDuration: Long = 0L,
    val totalPRs: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastWorkoutDate: LocalDate? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isPublic: Boolean = false
)
