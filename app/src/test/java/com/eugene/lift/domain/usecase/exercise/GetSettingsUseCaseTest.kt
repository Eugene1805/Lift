package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Unit test for GetSettingsUseCase
 * Tests retrieval of user settings
 */
class GetSettingsUseCaseTest {

    private lateinit var repository: SettingsRepository
    private lateinit var useCase: GetSettingsUseCase

    private val defaultSettings = UserSettings(
        theme = AppTheme.SYSTEM,
        weightUnit = WeightUnit.KG,
        distanceUnit = DistanceUnit.KM,
        languageCode = "en"
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetSettingsUseCase(repository)
    }

    @Test
    fun `invoke returns user settings from repository`() = runTest {
        // GIVEN
        coEvery { repository.getSettings() } returns flowOf(defaultSettings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals(AppTheme.SYSTEM, result.theme)
        Assert.assertEquals(WeightUnit.KG, result.weightUnit)
        Assert.assertEquals(DistanceUnit.KM, result.distanceUnit)
        Assert.assertEquals("en", result.languageCode)
    }

    @Test
    fun `invoke returns settings with LIGHT theme`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(theme = AppTheme.LIGHT)
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals(AppTheme.LIGHT, result.theme)
    }

    @Test
    fun `invoke returns settings with DARK theme`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(theme = AppTheme.DARK)
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals(AppTheme.DARK, result.theme)
    }

    @Test
    fun `invoke returns settings with LBS weight unit`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(weightUnit = WeightUnit.LBS)
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals(WeightUnit.LBS, result.weightUnit)
    }

    @Test
    fun `invoke returns settings with MILES distance unit`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(distanceUnit = DistanceUnit.MILES)
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals(DistanceUnit.MILES, result.distanceUnit)
    }

    @Test
    fun `invoke returns settings with Spanish language`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(languageCode = "es")
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals("es", result.languageCode)
    }

    @Test
    fun `invoke returns settings with French language`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(languageCode = "fr")
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals("fr", result.languageCode)
    }

    @Test
    fun `invoke returns settings with all custom values`() = runTest {
        // GIVEN
        val customSettings = UserSettings(
            theme = AppTheme.DARK,
            weightUnit = WeightUnit.LBS,
            distanceUnit = DistanceUnit.MILES,
            languageCode = "es"
        )
        coEvery { repository.getSettings() } returns flowOf(customSettings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals(AppTheme.DARK, result.theme)
        Assert.assertEquals(WeightUnit.LBS, result.weightUnit)
        Assert.assertEquals(DistanceUnit.MILES, result.distanceUnit)
        Assert.assertEquals("es", result.languageCode)
    }

    @Test
    fun `invoke returns flow that emits multiple times`() = runTest {
        // GIVEN
        val flow = flow {
            emit(defaultSettings)
            delay(100)
            emit(defaultSettings.copy(theme = AppTheme.DARK))
            delay(100)
            emit(defaultSettings.copy(theme = AppTheme.LIGHT))
        }
        coEvery { repository.getSettings() } returns flow

        // WHEN
        val results = mutableListOf<UserSettings>()
        useCase().collect {
            results.add(it)
            if (results.size >= 3) return@collect
        }

        // THEN
        Assert.assertEquals(3, results.size)
        Assert.assertEquals(AppTheme.SYSTEM, results[0].theme)
        Assert.assertEquals(AppTheme.DARK, results[1].theme)
        Assert.assertEquals(AppTheme.LIGHT, results[2].theme)
    }

    @Test
    fun `invoke handles language code with region`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(languageCode = "en-US")
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals("en-US", result.languageCode)
    }

    @Test
    fun `invoke returns settings reflecting multiple updates`() = runTest {
        // GIVEN - Simulating settings after multiple user updates
        val updatedSettings = UserSettings(
            theme = AppTheme.LIGHT,
            weightUnit = WeightUnit.LBS,
            distanceUnit = DistanceUnit.KM,
            languageCode = "es"
        )
        coEvery { repository.getSettings() } returns flowOf(updatedSettings)

        // WHEN
        val result = useCase().first()

        // THEN
        Assert.assertEquals(AppTheme.LIGHT, result.theme)
        Assert.assertEquals(WeightUnit.LBS, result.weightUnit)
        Assert.assertEquals(DistanceUnit.KM, result.distanceUnit)
        Assert.assertEquals("es", result.languageCode)
    }
}