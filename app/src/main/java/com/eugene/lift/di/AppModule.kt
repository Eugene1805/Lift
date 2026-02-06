package com.eugene.lift.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.ExerciseSeeder
import com.eugene.lift.data.local.SettingsDataSource
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.dao.FolderDao
import com.eugene.lift.data.local.dao.TemplateDao
import com.eugene.lift.data.local.dao.UserCredentialsDao
import com.eugene.lift.data.local.dao.UserProfileDao
import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.data.repository.ExerciseRepositoryImpl
import com.eugene.lift.data.repository.FolderRepositoryImpl
import com.eugene.lift.data.repository.SettingsRepositoryImpl
import com.eugene.lift.data.repository.TemplateRepositoryImpl
import com.eugene.lift.data.repository.UserProfileRepositoryImpl
import com.eugene.lift.data.repository.WorkoutRepositoryImpl
import com.eugene.lift.domain.repository.ExerciseRepository
import com.eugene.lift.domain.repository.FolderRepository
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.repository.TemplateRepository
import com.eugene.lift.domain.repository.UserProfileRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import com.eugene.lift.domain.usecase.workout.StartEmptyWorkoutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "lift_db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
    @Provides
    @Singleton
    fun provideExerciseDao(db: AppDatabase): ExerciseDao {
        return db.exerciseDao()
    }
    @Provides
    @Singleton
    fun provideTemplateDao(db: AppDatabase): TemplateDao = db.templateDao()

    @Provides
    @Singleton
    fun provideFolderDao(db: AppDatabase): FolderDao = db.folderDao()

    @Provides
    @Singleton
    fun provideFolderRepository(dao: FolderDao): FolderRepository {
        return FolderRepositoryImpl(dao)
    }
    @Provides
    @Singleton
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()

    @Provides
    @Singleton
    fun provideExerciseRepository(dao: ExerciseDao): ExerciseRepository {
        return ExerciseRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSettingsDataSource(@ApplicationContext context: Context): SettingsDataSource {
        return SettingsDataSource(context)
    }
    @Provides
    @Singleton
    fun provideTemplateRepository(dao: TemplateDao): TemplateRepository {
        return TemplateRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(dao: WorkoutDao): WorkoutRepository {
        return WorkoutRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataSource: SettingsDataSource): SettingsRepository {
        return SettingsRepositoryImpl(dataSource)
    }

    @Provides
    @Singleton
    fun provideExerciseSeeder(
        repository: ExerciseRepository,
        @ApplicationContext context: Context
    ): ExerciseSeeder {
        return ExerciseSeeder(repository, context)
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    @Singleton
    fun provideUserCredentialsDao(db: AppDatabase): UserCredentialsDao = db.userCredentialsDao()

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        userProfileDao: UserProfileDao,
        userCredentialsDao: UserCredentialsDao
    ): UserProfileRepository {
        return UserProfileRepositoryImpl(userProfileDao, userCredentialsDao)
    }
    @Provides
    @Singleton
    fun provideStartEmptyWorkoutUseCase(
        @ApplicationContext context: Context
    ): StartEmptyWorkoutUseCase {
        return StartEmptyWorkoutUseCase(context)
    }

}