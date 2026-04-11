package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.UserProfileDao
import com.eugene.lift.data.mapper.toDomain
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    override fun getCurrentProfile(): Flow<UserProfile?> {
        return userProfileDao.getCurrentProfile().map { it?.toDomain() }
    }

    override fun getProfileById(id: String): Flow<UserProfile?> {
        return userProfileDao.getProfileById(id).map { it?.toDomain() }
    }

    override suspend fun getCurrentProfileOnce(): UserProfile? {
        return userProfileDao.getCurrentProfileOnce()?.toDomain()
    }

    private suspend fun createLocalProfile(): UserProfile {
        val username = generateFunUsername()
        val displayName = username.replace("_", " ").replaceFirstChar { it.uppercase() }
        val avatarColor = AVATAR_COLORS.random()

        val profile = UserProfile(
            id = UUID.randomUUID().toString(),
            username = username,
            displayName = displayName,
            avatarColor = avatarColor
        )

        userProfileDao.insertProfile(profile.toEntity())
        return profile
    }

    override suspend fun getOrCreateProfile(): UserProfile {
        return getCurrentProfileOnce() ?: createLocalProfile()
    }

    override suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.updateProfile(profile.copy(updatedAt = LocalDateTime.now()).toEntity())
    }

    override suspend fun updateDisplayName(id: String, displayName: String) {
        userProfileDao.updateDisplayName(id, displayName, LocalDateTime.now())
    }

    override suspend fun updateBio(id: String, bio: String?) {
        userProfileDao.updateBio(id, bio, LocalDateTime.now())
    }

    override suspend fun updateAvatarUrl(id: String, avatarUrl: String?) {
        userProfileDao.updateAvatarUrl(id, avatarUrl, LocalDateTime.now())
    }

    override suspend fun updateUsername(id: String, username: String) {
        userProfileDao.updateUsername(id, username, LocalDateTime.now())
    }

    override suspend fun recordWorkoutCompleted(id: String, volume: Double, duration: Long, prCount: Int) {
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        userProfileDao.incrementWorkoutCount(id, today, now)
        if (volume > 0) userProfileDao.addVolume(id, volume, now)
        if (duration > 0) userProfileDao.addDuration(id, duration, now)
        if (prCount > 0) userProfileDao.addPRs(id, prCount, now)
    }

    override suspend fun updateStreak(id: String, streak: Int) {
        userProfileDao.updateStreak(id, streak, LocalDateTime.now())
    }

    override fun generateUsernameSuggestions(count: Int): List<String> {
        return (1..count).map { generateFunUsername() }
    }

    private fun generateFunUsername(): String {
        val adjective = ADJECTIVES.random()
        val animal = ANIMALS.random()
        val number = (10..99).random()
        return "${adjective}_${animal}_$number"
    }

    companion object {
        private val ADJECTIVES = listOf(
            "mighty", "swift", "brave", "fierce", "strong",
            "agile", "bold", "quick", "epic", "power",
            "iron", "steel", "golden", "silver", "titan",
            "turbo", "mega", "ultra", "super", "hyper",
            "alpha", "beast", "prime", "elite", "apex",
            "cosmic", "thunder", "blazing", "shadow", "storm"
        )

        private val ANIMALS = listOf(
            "lion", "tiger", "bear", "wolf", "eagle",
            "hawk", "panther", "bull", "rhino", "gorilla",
            "shark", "falcon", "cheetah", "cobra", "dragon",
            "phoenix", "griffin", "leopard", "bison", "mustang",
            "ox", "ram", "stag", "boar", "jaguar",
            "viper", "mantis", "scorpion", "raven", "condor"
        )

        private val AVATAR_COLORS = listOf(
            "#F44336", // Red
            "#E91E63", // Pink
            "#9C27B0", // Purple
            "#673AB7", // Deep Purple
            "#3F51B5", // Indigo
            "#2196F3", // Blue
            "#03A9F4", // Light Blue
            "#00BCD4", // Cyan
            "#009688", // Teal
            "#4CAF50", // Green
            "#8BC34A", // Light Green
            "#FF9800", // Orange
            "#FF5722", // Deep Orange
            "#795548", // Brown
            "#607D8B"  // Blue Grey
        )
    }
}
