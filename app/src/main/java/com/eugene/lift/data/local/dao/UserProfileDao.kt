package com.eugene.lift.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.eugene.lift.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    fun getProfileById(id: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getCurrentProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun getCurrentProfileOnce(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE username = :username LIMIT 1")
    suspend fun getProfileByUsername(username: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)

    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getProfileCount(): Int

    // Stats updates
    @Query("UPDATE user_profiles SET totalWorkouts = totalWorkouts + 1, lastWorkoutDate = :date, updatedAt = :updatedAt WHERE id = :id")
    suspend fun incrementWorkoutCount(id: String, date: LocalDate, updatedAt: LocalDateTime)

    @Query("UPDATE user_profiles SET totalVolume = totalVolume + :volume, updatedAt = :updatedAt WHERE id = :id")
    suspend fun addVolume(id: String, volume: Double, updatedAt: LocalDateTime)

    @Query("UPDATE user_profiles SET totalDuration = totalDuration + :duration, updatedAt = :updatedAt WHERE id = :id")
    suspend fun addDuration(id: String, duration: Long, updatedAt: LocalDateTime)

    @Query("UPDATE user_profiles SET totalPRs = totalPRs + :count, updatedAt = :updatedAt WHERE id = :id")
    suspend fun addPRs(id: String, count: Int, updatedAt: LocalDateTime)

    @Query("UPDATE user_profiles SET currentStreak = :streak, longestStreak = CASE WHEN :streak > longestStreak THEN :streak ELSE longestStreak END, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStreak(id: String, streak: Int, updatedAt: LocalDateTime)

    // Auth updates
    @Query("UPDATE user_profiles SET authProvider = :provider, authProviderId = :providerId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateAuthProvider(id: String, provider: String, providerId: String?, updatedAt: LocalDateTime)

    @Query("UPDATE user_profiles SET email = :email, isEmailVerified = :verified, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEmail(id: String, email: String, verified: Boolean, updatedAt: LocalDateTime)

    @Query("UPDATE user_profiles SET lastSyncedAt = :syncedAt WHERE id = :id")
    suspend fun updateLastSynced(id: String, syncedAt: LocalDateTime)

    // Profile updates
    @Query("UPDATE user_profiles SET displayName = :displayName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDisplayName(id: String, displayName: String, updatedAt: LocalDateTime)

    @Query("UPDATE user_profiles SET avatarUrl = :avatarUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateAvatarUrl(id: String, avatarUrl: String?, updatedAt: LocalDateTime)

    @Query("UPDATE user_profiles SET bio = :bio, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBio(id: String, bio: String?, updatedAt: LocalDateTime)

    // Social updates (for future use)
    @Query("UPDATE user_profiles SET followersCount = followersCount + :delta WHERE id = :id")
    suspend fun updateFollowersCount(id: String, delta: Int)

    @Query("UPDATE user_profiles SET followingCount = followingCount + :delta WHERE id = :id")
    suspend fun updateFollowingCount(id: String, delta: Int)

    @Query("UPDATE user_profiles SET isPublic = :isPublic, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePrivacy(id: String, isPublic: Boolean, updatedAt: LocalDateTime)
}
