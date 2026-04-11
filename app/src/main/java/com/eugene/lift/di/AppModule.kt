package com.eugene.lift.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.ExerciseImageResolverImpl
import com.eugene.lift.data.local.ExerciseSeeder
import com.eugene.lift.data.local.SettingsDataSource
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.dao.FolderDao
import com.eugene.lift.data.local.dao.TemplateDao
import com.eugene.lift.data.local.dao.UserProfileDao
import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.data.repository.ExerciseRepositoryImpl
import com.eugene.lift.data.repository.FolderRepositoryImpl
import com.eugene.lift.data.repository.ImageRepositoryImpl
import com.eugene.lift.data.repository.SettingsRepositoryImpl
import com.eugene.lift.data.repository.TemplateRepositoryImpl
import com.eugene.lift.data.repository.UserProfileRepositoryImpl
import com.eugene.lift.data.repository.WorkoutRepositoryImpl
import com.eugene.lift.domain.repository.ExerciseRepository
import com.eugene.lift.domain.repository.FolderRepository
import com.eugene.lift.domain.repository.ImageRepository
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.repository.TemplateRepository
import com.eugene.lift.domain.repository.UserProfileRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import com.eugene.lift.domain.usecase.exercise.ExerciseImageResolver
import com.eugene.lift.domain.usecase.workout.StartEmptyWorkoutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_templates ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop user_credentials table
                db.execSQL("DROP TABLE IF EXISTS user_credentials")

                // Handle user_profiles column removal
                // Standard approach: Create new table, copy data, drop old, rename new
                db.execSQL("""
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
                """.trimIndent())

                db.execSQL("""
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
                """.trimIndent())

                db.execSQL("DROP TABLE user_profiles")
                db.execSQL("ALTER TABLE user_profiles_new RENAME TO user_profiles")
            }
        }

        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "lift_db"
        )
            .addMigrations(MIGRATION_8_9, MIGRATION_9_10)
            .fallbackToDestructiveMigration(true)
            .build()
    }
    @Provides
    @Singleton
    fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    @Singleton
    fun provideTemplateDao(db: AppDatabase): TemplateDao = db.templateDao()

    @Provides
    @Singleton
    fun provideFolderDao(db: AppDatabase): FolderDao = db.folderDao()

    @Provides
    @Singleton
    fun provideFolderRepository(dao: FolderDao): FolderRepository = FolderRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()

    @Provides
    @Singleton
    fun provideExerciseRepository(dao: ExerciseDao): ExerciseRepository = ExerciseRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideSettingsDataSource(@ApplicationContext context: Context): SettingsDataSource = SettingsDataSource(context)

    @Provides
    @Singleton
    fun provideTemplateRepository(dao: TemplateDao): TemplateRepository = TemplateRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        dao: WorkoutDao,
        settingsRepository: SettingsRepository
    ): WorkoutRepository = WorkoutRepositoryImpl(dao, settingsRepository)

    @Provides
    @Singleton
    fun provideSettingsRepository(dataSource: SettingsDataSource): SettingsRepository = SettingsRepositoryImpl(dataSource)

    @Provides
    @Singleton
    fun provideExerciseSeeder(
        repository: ExerciseRepository,
        @ApplicationContext context: Context
    ): ExerciseSeeder = ExerciseSeeder(repository, context)

    @Provides
    @Singleton
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        userProfileDao: UserProfileDao
    ): UserProfileRepository = UserProfileRepositoryImpl(userProfileDao)

    @Provides
    @Singleton
    fun provideStartEmptyWorkoutUseCase(
        @ApplicationContext context: Context
    ): StartEmptyWorkoutUseCase {
        return StartEmptyWorkoutUseCase(context)
    }

    @Provides
    @Singleton
    fun provideDebugLogger(): com.eugene.lift.core.util.Logger {
        return com.eugene.lift.core.util.DebugLogger()
    }

    @Provides
    @Singleton
    fun provideSafeExecutor(logger: com.eugene.lift.core.util.Logger): com.eugene.lift.core.util.SafeExecutor {
        return com.eugene.lift.core.util.SafeExecutor(logger)
    }

    @Provides
    @Singleton
    fun provideImageRepository(
        @ApplicationContext context: Context
    ): ImageRepository {
        return ImageRepositoryImpl(context, Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun provideExerciseImageResolver(
        impl: ExerciseImageResolverImpl
    ): ExerciseImageResolver {
        return impl
    }

}