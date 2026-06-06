package com.eugene.lift.data.remote

import com.eugene.lift.data.remote.api.WgerApiService
import com.eugene.lift.data.remote.mapper.toRemoteExercisePage
import com.eugene.lift.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class WgerExerciseRemoteDataSource @Inject constructor(
    private val apiService: WgerApiService,
    private val settingsRepository: SettingsRepository
) : ExerciseRemoteDataSource {

    override suspend fun syncExercises(): List<RemoteExercisePayload> {
        val preferredLanguageCode = settingsRepository.getSettings()
            .first()
            .languageCode
            .ifBlank { "en" }
        val languageIdByCode = apiService.getLanguages()
            .results
            .associate { language -> language.short_name to language.id }

        val exercises = mutableListOf<RemoteExercisePayload>()
        var offset = 0

        do {
            val page = apiService.getExercises(
                limit = ExerciseSyncDataSource.DEFAULT_PAGE_SIZE,
                offset = offset
            ).toRemoteExercisePage(
                preferredLanguageCode = preferredLanguageCode,
                languageIdByCode = languageIdByCode
            )

            exercises += page.exercises
            offset += page.exercises.size
        } while (page.nextPageUrl != null && page.exercises.isNotEmpty())

        return exercises
    }
}
