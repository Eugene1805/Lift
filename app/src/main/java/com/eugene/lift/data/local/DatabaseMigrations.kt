package com.eugene.lift.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workout_templates ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS user_credentials")

        db.execSQL(
            """
                CREATE TABLE user_profiles_new (
                    id TEXT NOT NULL PRIMARY KEY,
                    username TEXT NOT NULL,
                    displayName TEXT NOT NULL,
                    email TEXT,
                    avatarUrl TEXT,
                    avatarColor TEXT NOT NULL,
                    bio TEXT,
                    createdAt TEXT NOT NULL,
                    updatedAt TEXT NOT NULL,
                    totalWorkouts INTEGER NOT NULL,
                    totalVolume REAL NOT NULL,
                    totalDuration INTEGER NOT NULL,
                    totalPRs INTEGER NOT NULL,
                    currentStreak INTEGER NOT NULL,
                    longestStreak INTEGER NOT NULL,
                    lastWorkoutDate TEXT,
                    followersCount INTEGER NOT NULL,
                    followingCount INTEGER NOT NULL,
                    isPublic INTEGER NOT NULL
                )
            """.trimIndent()
        )

        db.execSQL(
            """
                INSERT INTO user_profiles_new (
                    id, username, displayName, email, avatarUrl, avatarColor, bio,
                    createdAt, updatedAt, totalWorkouts, totalVolume, totalDuration,
                    totalPRs, currentStreak, longestStreak, lastWorkoutDate,
                    followersCount, followingCount, isPublic
                )
                SELECT
                    id, username, displayName, email, avatarUrl, avatarColor, bio,
                    createdAt, updatedAt, totalWorkouts, totalVolume, totalDuration,
                    totalPRs, currentStreak, longestStreak, lastWorkoutDate,
                    followersCount, followingCount, isPublic
                FROM user_profiles
            """.trimIndent()
        )

        db.execSQL("DROP TABLE user_profiles")
        db.execSQL("ALTER TABLE user_profiles_new RENAME TO user_profiles")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercises ADD COLUMN remoteId INTEGER")
        db.execSQL("ALTER TABLE exercises ADD COLUMN source TEXT NOT NULL DEFAULT 'LOCAL'")
        db.execSQL("ALTER TABLE exercises ADD COLUMN lastSyncedAt INTEGER")
        db.execSQL("ALTER TABLE exercises ADD COLUMN syncVersion INTEGER")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_exercises_remoteId` ON `exercises` (`remoteId`)"
        )
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `exercises_new` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `measureType` TEXT NOT NULL,
                    `instructions` TEXT NOT NULL,
                    `imagePath` TEXT,
                    PRIMARY KEY(`id`)
                )
            """.trimIndent()
        )
        db.execSQL(
            """
                INSERT INTO `exercises_new` (`id`, `name`, `category`, `measureType`, `instructions`, `imagePath`)
                SELECT `id`, `name`, `category`, `measureType`, `instructions`, `imagePath`
                FROM `exercises`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `exercises`")
        db.execSQL("ALTER TABLE `exercises_new` RENAME TO `exercises`")
    }
}
