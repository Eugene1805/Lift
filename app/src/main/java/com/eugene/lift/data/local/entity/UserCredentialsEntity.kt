package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

/**
 * Stores user credentials for email/password authentication.
 * Kept separate from UserProfileEntity for security isolation.
 */
@Entity(
    tableName = "user_credentials",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserCredentialsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastPasswordChangeAt: LocalDateTime? = null
)
