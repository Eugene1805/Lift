package com.eugene.lift.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.ActiveWorkoutDraftDataSource
import com.eugene.lift.data.local.ExerciseBootstrapDataSource
import com.eugene.lift.data.local.ExerciseImageResolverImpl
import com.eugene.lift.data.local.ExerciseSeeder
import com.eugene.lift.data.local.MIGRATION_10_11
import com.eugene.lift.data.local.MIGRATION_11_12
import com.eugene.lift.data.local.MIGRATION_12_13
import com.eugene.lift.data.local.MIGRATION_8_9
import com.eugene.lift.data.local.MIGRATION_9_10
import com.eugene.lift.data.local.SettingsDataSource
import com.eugene.lift.data.backup.AppBackupLocalStore
import com.eugene.lift.data.backup.RoomAppBackupLocalStore
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.dao.FolderDao
import com.eugene.lift.data.local.dao.TemplateDao
import com.eugene.lift.data.local.dao.UserProfileDao
import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.data.repository.AppDataTransferRepositoryImpl
import com.eugene.lift.data.repository.ActiveWorkoutDraftRepositoryImpl
import com.eugene.lift.data.repository.ExerciseRepositoryImpl
import com.eugene.lift.data.repository.FolderRepositoryImpl
import com.eugene.lift.data.repository.ImageRepositoryImpl
import com.eugene.lift.data.repository.SettingsRepositoryImpl
import com.eugene.lift.data.repository.TemplateRepositoryImpl
import com.eugene.lift.data.repository.UserProfileRepositoryImpl
import com.eugene.lift.data.repository.WorkoutRepositoryImpl
import com.eugene.lift.domain.repository.ActiveWorkoutDraftRepository
import com.eugene.lift.domain.repository.AppDataTransferRepository
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
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "lift_db"
        )
            .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)
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
    fun provideExerciseRepository(
        dao: ExerciseDao,
        settingsDataSource: SettingsDataSource,
        @ApplicationContext context: Context
    ): ExerciseRepository = ExerciseRepositoryImpl(dao, settingsDataSource, context)

    @Provides
    @Singleton
    fun provideSettingsDataSource(@ApplicationContext context: Context): SettingsDataSource = SettingsDataSource(context)

    @Provides
    @Singleton
    fun provideActiveWorkoutDraftDataSource(
        @ApplicationContext context: Context
    ): ActiveWorkoutDraftDataSource = ActiveWorkoutDraftDataSource(context)

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
    fun provideAppBackupLocalStore(
        impl: RoomAppBackupLocalStore
    ): AppBackupLocalStore = impl

    @Provides
    @Singleton
    fun provideAppDataTransferRepository(
        impl: AppDataTransferRepositoryImpl
    ): AppDataTransferRepository = impl

    @Provides
    @Singleton
    fun provideActiveWorkoutDraftRepository(
        dataSource: ActiveWorkoutDraftDataSource
    ): ActiveWorkoutDraftRepository = ActiveWorkoutDraftRepositoryImpl(dataSource)

    @Provides
    @Singleton
    fun provideExerciseSeeder(
        repository: ExerciseRepository,
        @ApplicationContext context: Context
    ): ExerciseSeeder = ExerciseSeeder(repository, context)

    @Provides
    @Singleton
    fun provideExerciseBootstrapDataSource(
        seeder: ExerciseSeeder
    ): ExerciseBootstrapDataSource = seeder

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
