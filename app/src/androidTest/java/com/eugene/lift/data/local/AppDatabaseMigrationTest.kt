package com.eugene.lift.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @After
    fun cleanUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(TEST_DB_NAME)
    }

    @Test
    fun migrate10To11_preservesExerciseRowsAndAddsRemoteMetadataColumns() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(TEST_DB_NAME)
        createVersion10Database(context.getDatabasePath(TEST_DB_NAME).path)

        val database = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_NAME)
            .addMigrations(MIGRATION_10_11)
            .build()

        database.openHelper.writableDatabase

        database.openHelper.readableDatabase.query(
            "SELECT id, name, remoteId, source, lastSyncedAt, syncVersion FROM exercises WHERE id = 'local-1'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("local-1", cursor.getString(cursor.getColumnIndexOrThrow("id")))
            assertEquals("Bench Press", cursor.getString(cursor.getColumnIndexOrThrow("name")))
            assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("remoteId")))
            assertEquals("LOCAL", cursor.getString(cursor.getColumnIndexOrThrow("source")))
            assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("lastSyncedAt")))
            assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("syncVersion")))
        }

        database.openHelper.readableDatabase.query("PRAGMA index_list(`exercises`)").use { cursor ->
            var foundRemoteIdIndex = false
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == "index_exercises_remoteId") {
                    foundRemoteIdIndex = true
                }
            }
            assertTrue(foundRemoteIdIndex)
        }

        database.openHelper.readableDatabase.query("PRAGMA table_info(`exercises`)").use { cursor ->
            var hasRemoteId = false
            var hasSource = false
            var hasLastSyncedAt = false
            var hasSyncVersion = false

            while (cursor.moveToNext()) {
                when (cursor.getString(cursor.getColumnIndexOrThrow("name"))) {
                    "remoteId" -> hasRemoteId = true
                    "source" -> hasSource = true
                    "lastSyncedAt" -> hasLastSyncedAt = true
                    "syncVersion" -> hasSyncVersion = true
                }
            }

            assertTrue(hasRemoteId)
            assertTrue(hasSource)
            assertTrue(hasLastSyncedAt)
            assertTrue(hasSyncVersion)
        }

        database.close()
    }

    private fun createVersion10Database(path: String) {
        SQLiteDatabase.openOrCreateDatabase(path, null).use { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `exercises` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `measureType` TEXT NOT NULL, `instructions` TEXT NOT NULL, `imagePath` TEXT, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `exercise_body_part_cross_ref` (`exerciseId` TEXT NOT NULL, `bodyPart` TEXT NOT NULL, PRIMARY KEY(`exerciseId`, `bodyPart`))")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_body_part_cross_ref_bodyPart` ON `exercise_body_part_cross_ref` (`bodyPart`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `workout_folders` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `color` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `workout_templates` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `notes` TEXT NOT NULL, `isArchived` INTEGER NOT NULL, `lastPerformedAt` TEXT, `folderId` TEXT, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`folderId`) REFERENCES `workout_folders`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_templates_folderId` ON `workout_templates` (`folderId`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `template_exercises` (`id` TEXT NOT NULL, `templateId` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `targetSets` INTEGER NOT NULL, `targetReps` TEXT NOT NULL, `restTimerSeconds` INTEGER NOT NULL, `note` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_templateId` ON `template_exercises` (`templateId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_exerciseId` ON `template_exercises` (`exerciseId`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` TEXT NOT NULL, `templateId` TEXT, `name` TEXT NOT NULL, `date` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL, `note` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_templateId` ON `workout_sessions` (`templateId`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `session_exercises` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `note` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercises_sessionId` ON `session_exercises` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercises_exerciseId` ON `session_exercises` (`exerciseId`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `workout_sets` (`id` TEXT NOT NULL, `sessionExerciseId` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `weight` REAL NOT NULL, `reps` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `rpe` REAL, `rir` INTEGER, `isPr` INTEGER NOT NULL, `timeSeconds` INTEGER, `distance` REAL, PRIMARY KEY(`id`), FOREIGN KEY(`sessionExerciseId`) REFERENCES `session_exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_sessionExerciseId` ON `workout_sets` (`sessionExerciseId`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `user_profiles` (`id` TEXT NOT NULL, `username` TEXT NOT NULL, `displayName` TEXT NOT NULL, `email` TEXT, `avatarUrl` TEXT, `avatarColor` TEXT NOT NULL, `bio` TEXT, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, `totalWorkouts` INTEGER NOT NULL, `totalVolume` REAL NOT NULL, `totalDuration` INTEGER NOT NULL, `totalPRs` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `longestStreak` INTEGER NOT NULL, `lastWorkoutDate` TEXT, `followersCount` INTEGER NOT NULL, `followingCount` INTEGER NOT NULL, `isPublic` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("INSERT INTO `exercises` (`id`, `name`, `category`, `measureType`, `instructions`, `imagePath`) VALUES ('local-1', 'Bench Press', 'BARBELL', 'REPS_AND_WEIGHT', 'Press', NULL)")
            db.version = 10
        }
    }

    private companion object {
        const val TEST_DB_NAME = "migration-test-db"
    }
}
