package com.eugene.lift.domain.usecase

import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<UserSettings> {
        return repository.getSettings()
    }
}