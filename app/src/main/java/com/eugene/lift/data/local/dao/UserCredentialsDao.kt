package com.eugene.lift.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.eugene.lift.data.local.entity.UserCredentialsEntity
import java.time.LocalDateTime

@Dao
interface UserCredentialsDao {

    @Query("SELECT * FROM user_credentials WHERE userId = :userId")
    suspend fun getCredentialsByUserId(userId: String): UserCredentialsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredentials(credentials: UserCredentialsEntity)

    @Update
    suspend fun updateCredentials(credentials: UserCredentialsEntity)

    @Query("DELETE FROM user_credentials WHERE userId = :userId")
    suspend fun deleteCredentials(userId: String)

    @Query("UPDATE user_credentials SET passwordHash = :passwordHash, salt = :salt, updatedAt = :updatedAt, lastPasswordChangeAt = :updatedAt WHERE userId = :userId")
    suspend fun updatePassword(userId: String, passwordHash: String, salt: String, updatedAt: LocalDateTime)
}
