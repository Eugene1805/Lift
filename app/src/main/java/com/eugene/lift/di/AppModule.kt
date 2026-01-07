package com.eugene.lift.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.SettingsDataSource
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.repository.ExerciseRepositoryImpl
import com.eugene.lift.data.repository.SettingsRepositoryImpl
import com.eugene.lift.domain.repository.ExerciseRepository
import com.eugene.lift.domain.repository.SettingsRepository
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
            .fallbackToDestructiveMigration(false)
            .build()
    }
    @Provides
    @Singleton
    fun provideExerciseDao(db: AppDatabase): ExerciseDao {
        return db.exerciseDao()
    }

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
    fun provideSettingsRepository(dataSource: SettingsDataSource): SettingsRepository {
        return SettingsRepositoryImpl(dataSource)
    }
}