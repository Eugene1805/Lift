package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val avatarColor: String = "#6200EE",
    val bio: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // Authentication
    val authProvider: String = "LOCAL", // Stored as string for Room
    val authProviderId: String? = null,
    val isEmailVerified: Boolean = false,
    val lastSyncedAt: LocalDateTime? = null,

    // Stats
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0,
    val totalDuration: Long = 0L,
    val totalPRs: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastWorkoutDate: LocalDate? = null,

    // Social
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isPublic: Boolean = false
)
