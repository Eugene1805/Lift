package com.eugene.lift.domain.usecase

import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        assertEquals(AppTheme.SYSTEM, result.theme)
        assertEquals(WeightUnit.KG, result.weightUnit)
        assertEquals(DistanceUnit.KM, result.distanceUnit)
        assertEquals("en", result.languageCode)
    }

    @Test
    fun `invoke returns settings with LIGHT theme`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(theme = AppTheme.LIGHT)
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(AppTheme.LIGHT, result.theme)
    }

    @Test
    fun `invoke returns settings with DARK theme`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(theme = AppTheme.DARK)
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(AppTheme.DARK, result.theme)
    }

    @Test
    fun `invoke returns settings with LBS weight unit`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(weightUnit = WeightUnit.LBS)
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(WeightUnit.LBS, result.weightUnit)
    }

    @Test
    fun `invoke returns settings with MILES distance unit`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(distanceUnit = DistanceUnit.MILES)
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(DistanceUnit.MILES, result.distanceUnit)
    }

    @Test
    fun `invoke returns settings with Spanish language`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(languageCode = "es")
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals("es", result.languageCode)
    }

    @Test
    fun `invoke returns settings with French language`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(languageCode = "fr")
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals("fr", result.languageCode)
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
        assertEquals(AppTheme.DARK, result.theme)
        assertEquals(WeightUnit.LBS, result.weightUnit)
        assertEquals(DistanceUnit.MILES, result.distanceUnit)
        assertEquals("es", result.languageCode)
    }

    @Test
    fun `invoke returns flow that emits multiple times`() = runTest {
        // GIVEN
        val flow = kotlinx.coroutines.flow.flow {
            emit(defaultSettings)
            kotlinx.coroutines.delay(100)
            emit(defaultSettings.copy(theme = AppTheme.DARK))
            kotlinx.coroutines.delay(100)
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
        assertEquals(3, results.size)
        assertEquals(AppTheme.SYSTEM, results[0].theme)
        assertEquals(AppTheme.DARK, results[1].theme)
        assertEquals(AppTheme.LIGHT, results[2].theme)
    }

    @Test
    fun `invoke handles language code with region`() = runTest {
        // GIVEN
        val settings = defaultSettings.copy(languageCode = "en-US")
        coEvery { repository.getSettings() } returns flowOf(settings)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals("en-US", result.languageCode)
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
        assertEquals(AppTheme.LIGHT, result.theme)
        assertEquals(WeightUnit.LBS, result.weightUnit)
        assertEquals(DistanceUnit.KM, result.distanceUnit)
        assertEquals("es", result.languageCode)
    }
}
