package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.UserCredentialsDao
import com.eugene.lift.data.local.dao.UserProfileDao
import com.eugene.lift.data.local.entity.UserCredentialsEntity
import com.eugene.lift.data.mapper.toDomain
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.AuthProvider
import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val userCredentialsDao: UserCredentialsDao
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

    /**
     * Creates a new local profile with a fun auto-generated username.
     * Called on first app launch.
     */
    private suspend fun createLocalProfile(): UserProfile {
        val username = generateFunUsername()
        val displayName = username.replace("_", " ").replaceFirstChar { it.uppercase() }
        val avatarColor = AVATAR_COLORS.random()

        val profile = UserProfile(
            id = UUID.randomUUID().toString(),
            username = username,
            displayName = displayName,
            avatarColor = avatarColor,
            authProvider = AuthProvider.LOCAL
        )

        userProfileDao.insertProfile(profile.toEntity())
        return profile
    }

    /**
     * Creates or gets the current profile. If no profile exists, creates a local one.
     */
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

    // Stats methods
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

    // Auth methods
    override suspend fun linkEmailAuth(id: String, email: String, password: String): Boolean {
        val existingUser = userProfileDao.getProfileByEmail(email)
        if (existingUser != null && existingUser.id != id) {
            return false // Email already in use
        }

        val salt = generateSalt()
        val passwordHash = hashPassword(password, salt)

        userCredentialsDao.insertCredentials(
            UserCredentialsEntity(
                userId = id,
                passwordHash = passwordHash,
                salt = salt
            )
        )

        userProfileDao.updateAuthProvider(id, AuthProvider.EMAIL.name, null, LocalDateTime.now())
        userProfileDao.updateEmail(id, email, false, LocalDateTime.now())
        return true
    }

    override suspend fun linkSocialAuth(id: String, provider: AuthProvider, providerId: String, email: String?) {
        userProfileDao.updateAuthProvider(id, provider.name, providerId, LocalDateTime.now())
        if (email != null) {
            userProfileDao.updateEmail(id, email, true, LocalDateTime.now())
        }
    }

    override suspend fun verifyPassword(userId: String, password: String): Boolean {
        val credentials = userCredentialsDao.getCredentialsByUserId(userId) ?: return false
        val hashedInput = hashPassword(password, credentials.salt)
        return hashedInput == credentials.passwordHash
    }

    override suspend fun changePassword(userId: String, newPassword: String) {
        val salt = generateSalt()
        val passwordHash = hashPassword(newPassword, salt)
        userCredentialsDao.updatePassword(userId, passwordHash, salt, LocalDateTime.now())
    }

    /**
     * Generates suggested usernames for the user to pick from.
     */
    override fun generateUsernameSuggestions(count: Int): List<String> {
        return (1..count).map { generateFunUsername() }
    }

    private fun generateFunUsername(): String {
        val adjective = ADJECTIVES.random()
        val animal = ANIMALS.random()
        val number = (10..99).random()
        return "${adjective}_${animal}_$number"
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val saltedPassword = password + salt
        val digest = md.digest(saltedPassword.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        // Fitness-themed adjectives
        private val ADJECTIVES = listOf(
            "mighty", "swift", "brave", "fierce", "strong",
            "agile", "bold", "quick", "epic", "power",
            "iron", "steel", "golden", "silver", "titan",
            "turbo", "mega", "ultra", "super", "hyper",
            "alpha", "beast", "prime", "elite", "apex",
            "cosmic", "thunder", "blazing", "shadow", "storm"
        )

        // Gym/fitness-themed animals
        private val ANIMALS = listOf(
            "lion", "tiger", "bear", "wolf", "eagle",
            "hawk", "panther", "bull", "rhino", "gorilla",
            "shark", "falcon", "cheetah", "cobra", "dragon",
            "phoenix", "griffin", "leopard", "bison", "mustang",
            "ox", "ram", "stag", "boar", "jaguar",
            "viper", "mantis", "scorpion", "raven", "condor"
        )

        // Material design colors for default avatars
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
