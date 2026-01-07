package com.eugene.lift.data.repository

import com.eugene.lift.data.local.SettingsDataSource
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataSource: SettingsDataSource
) : SettingsRepository {

    override fun getSettings(): Flow<UserSettings> = dataSource.userSettings

    override suspend fun setTheme(theme: AppTheme) = dataSource.setTheme(theme)

    override suspend fun setWeightUnit(unit: WeightUnit) = dataSource.setWeightUnit(unit)

    override suspend fun setDistanceUnit(unit: DistanceUnit) = dataSource.setDistanceUnit(unit)
}