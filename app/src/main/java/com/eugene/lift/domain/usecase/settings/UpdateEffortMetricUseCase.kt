package com.eugene.lift.domain.usecase.settings

import com.eugene.lift.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateEffortMetricUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(metric: String?) = repository.setEffortMetric(metric)
}
