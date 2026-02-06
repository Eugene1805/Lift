package com.eugene.lift.data.mapper

import com.eugene.lift.data.local.entity.UserProfileEntity
import com.eugene.lift.domain.model.AuthProvider
import com.eugene.lift.domain.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        username = username,
        displayName = displayName,
        email = email,
        avatarUrl = avatarUrl,
        avatarColor = avatarColor,
        bio = bio,
        createdAt = createdAt,
        updatedAt = updatedAt,
        authProvider = try {
            AuthProvider.valueOf(authProvider)
        } catch (_: IllegalArgumentException) {
            AuthProvider.LOCAL
        },
        authProviderId = authProviderId,
        isEmailVerified = isEmailVerified,
        lastSyncedAt = lastSyncedAt,
        totalWorkouts = totalWorkouts,
        totalVolume = totalVolume,
        totalDuration = totalDuration,
        totalPRs = totalPRs,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastWorkoutDate = lastWorkoutDate,
        followersCount = followersCount,
        followingCount = followingCount,
        isPublic = isPublic
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        username = username,
        displayName = displayName,
        email = email,
        avatarUrl = avatarUrl,
        avatarColor = avatarColor,
        bio = bio,
        createdAt = createdAt,
        updatedAt = updatedAt,
        authProvider = authProvider.name,
        authProviderId = authProviderId,
        isEmailVerified = isEmailVerified,
        lastSyncedAt = lastSyncedAt,
        totalWorkouts = totalWorkouts,
        totalVolume = totalVolume,
        totalDuration = totalDuration,
        totalPRs = totalPRs,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastWorkoutDate = lastWorkoutDate,
        followersCount = followersCount,
        followingCount = followingCount,
        isPublic = isPublic
    )
}
