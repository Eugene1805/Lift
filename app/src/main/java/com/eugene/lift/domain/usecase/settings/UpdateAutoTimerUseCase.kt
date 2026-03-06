package com.eugene.lift.domain.usecase.settings

import com.eugene.lift.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateAutoTimerUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAutoTimerEnabled(enabled)
}
