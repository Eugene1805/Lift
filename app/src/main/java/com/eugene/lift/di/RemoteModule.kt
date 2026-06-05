package com.eugene.lift.di

import com.eugene.lift.data.remote.ExerciseRemoteDataSource
import com.eugene.lift.data.remote.WgerExerciseRemoteDataSource
import com.eugene.lift.data.remote.api.WgerApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val WGER_BASE_URL = "https://wger.de/api/v2/"

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteBindingsModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRemoteDataSource(
        impl: WgerExerciseRemoteDataSource
    ): ExerciseRemoteDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideRemoteOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRemoteRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(WGER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWgerApiService(retrofit: Retrofit): WgerApiService {
        return retrofit.create(WgerApiService::class.java)
    }
}
