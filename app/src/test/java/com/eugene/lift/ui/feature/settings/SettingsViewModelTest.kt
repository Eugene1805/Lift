package com.eugene.lift.ui.feature.settings

import android.util.Log
import app.cash.turbine.test
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.usecase.GetSettingsUseCase
import com.eugene.lift.domain.usecase.settings.GetCurrentLanguageUseCase
import com.eugene.lift.domain.usecase.settings.UpdateDistanceUnitUseCase
import com.eugene.lift.domain.usecase.settings.UpdateLanguageUseCase
import com.eugene.lift.domain.usecase.settings.UpdateThemeUseCase
import com.eugene.lift.domain.usecase.settings.UpdateWeightUnitUseCase
import com.eugene.lift.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit test for SettingsViewModel
 * Tests settings management functionality
 */
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getSettingsUseCase: GetSettingsUseCase
    private lateinit var updateThemeUseCase: UpdateThemeUseCase
    private lateinit var updateWeightUnitUseCase: UpdateWeightUnitUseCase
    private lateinit var updateDistanceUnitUseCase: UpdateDistanceUnitUseCase
    private lateinit var updateLanguageUseCase: UpdateLanguageUseCase
    private lateinit var getCurrentLanguageUseCase: GetCurrentLanguageUseCase
    private lateinit var viewModel: SettingsViewModel

    private val defaultSettings = UserSettings(
        theme = AppTheme.SYSTEM,
        weightUnit = WeightUnit.KG,
        distanceUnit = DistanceUnit.KM,
        languageCode = "en"
    )

    @Before
    fun setup() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        getSettingsUseCase = mockk()
        updateThemeUseCase = mockk(relaxed = true)
        updateWeightUnitUseCase = mockk(relaxed = true)
        updateDistanceUnitUseCase = mockk(relaxed = true)
        updateLanguageUseCase = mockk(relaxed = true)
        getCurrentLanguageUseCase = mockk()

        coEvery { getSettingsUseCase() } returns flowOf(defaultSettings)
    }

    private fun createViewModel() {
        viewModel = SettingsViewModel(
            getSettingsUseCase,
            updateThemeUseCase,
            updateWeightUnitUseCase,
            updateDistanceUnitUseCase,
            updateLanguageUseCase,
            getCurrentLanguageUseCase
        )
    }

    @Test
    fun `settings StateFlow emits initial settings`() = runTest {
        // WHEN
        createViewModel()

        // THEN
        viewModel.settings.test {
            val settings = awaitItem()
            assertEquals(AppTheme.SYSTEM, settings.theme)
            assertEquals(WeightUnit.KG, settings.weightUnit)
            assertEquals(DistanceUnit.KM, settings.distanceUnit)
            assertEquals("en", settings.languageCode)
        }
    }

    @Test
    fun `updateTheme calls use case with theme`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.updateTheme(AppTheme.DARK)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.DARK) }
    }

    @Test
    fun `updateTheme handles multiple theme updates`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.updateTheme(AppTheme.LIGHT)
        viewModel.updateTheme(AppTheme.DARK)
        viewModel.updateTheme(AppTheme.SYSTEM)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.LIGHT) }
        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.DARK) }
        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.SYSTEM) }
    }

    @Test
    fun `updateWeightUnit calls use case with weight unit`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.updateWeightUnit(WeightUnit.LBS)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { updateWeightUnitUseCase(WeightUnit.LBS) }
    }

    @Test
    fun `updateWeightUnit handles multiple updates`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.updateWeightUnit(WeightUnit.LBS)
        viewModel.updateWeightUnit(WeightUnit.KG)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { updateWeightUnitUseCase(WeightUnit.LBS) }
        coVerify(exactly = 1) { updateWeightUnitUseCase(WeightUnit.KG) }
    }

    @Test
    fun `updateDistanceUnit calls use case with distance unit`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.updateDistanceUnit(DistanceUnit.MILES)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { updateDistanceUnitUseCase(DistanceUnit.MILES) }
    }

    @Test
    fun `updateDistanceUnit handles multiple updates`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.updateDistanceUnit(DistanceUnit.MILES)
        viewModel.updateDistanceUnit(DistanceUnit.KM)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { updateDistanceUnitUseCase(DistanceUnit.MILES) }
        coVerify(exactly = 1) { updateDistanceUnitUseCase(DistanceUnit.KM) }
    }

    @Test
    fun `updateLanguage calls use case with language code`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.updateLanguage("es")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { updateLanguageUseCase("es") }
    }

    @Test
    fun `updateLanguage handles multiple language updates`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.updateLanguage("es")
        viewModel.updateLanguage("fr")
        viewModel.updateLanguage("en")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { updateLanguageUseCase("es") }
        coVerify(exactly = 1) { updateLanguageUseCase("fr") }
        coVerify(exactly = 1) { updateLanguageUseCase("en") }
    }

    @Test
    fun `getCurrentLanguageCode returns language from use case`() = runTest {
        // GIVEN
        createViewModel()
        every { getCurrentLanguageUseCase() } returns "es"

        // WHEN
        val result = viewModel.getCurrentLanguageCode()

        // THEN
        assertEquals("es", result)
    }

    @Test
    fun `getCurrentLanguageCode handles different languages`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN/THEN
        every { getCurrentLanguageUseCase() } returns "en"
        assertEquals("en", viewModel.getCurrentLanguageCode())

        every { getCurrentLanguageUseCase() } returns "fr"
        assertEquals("fr", viewModel.getCurrentLanguageCode())

        every { getCurrentLanguageUseCase() } returns "de"
        assertEquals("de", viewModel.getCurrentLanguageCode())
    }

    @Test
    fun `settings StateFlow emits updated settings`() = runTest {
        // GIVEN
        val updatedSettings = defaultSettings.copy(theme = AppTheme.DARK)
        coEvery { getSettingsUseCase() } returns flowOf(defaultSettings, updatedSettings)

        // WHEN
        createViewModel()

        // THEN
        viewModel.settings.test {
            assertEquals(AppTheme.SYSTEM, awaitItem().theme)
            assertEquals(AppTheme.DARK, awaitItem().theme)
        }
    }

    @Test
    fun `updateTheme handles exception gracefully`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateThemeUseCase(any()) } throws Exception("Theme update failed")

        // WHEN - Should not crash
        viewModel.updateTheme(AppTheme.DARK)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - Exception logged but not thrown
        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.DARK) }
    }

    @Test
    fun `updateWeightUnit handles exception gracefully`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateWeightUnitUseCase(any()) } throws Exception("Weight unit update failed")

        // WHEN - Should not crash
        viewModel.updateWeightUnit(WeightUnit.LBS)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - Exception logged but not thrown
        coVerify(exactly = 1) { updateWeightUnitUseCase(WeightUnit.LBS) }
    }

    @Test
    fun `updateDistanceUnit handles exception gracefully`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateDistanceUnitUseCase(any()) } throws Exception("Distance unit update failed")

        // WHEN - Should not crash
        viewModel.updateDistanceUnit(DistanceUnit.MILES)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - Exception logged but not thrown
        coVerify(exactly = 1) { updateDistanceUnitUseCase(DistanceUnit.MILES) }
    }

    @Test
    fun `updateLanguage handles exception gracefully`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateLanguageUseCase(any()) } throws Exception("Language update failed")

        // WHEN - Should not crash
        viewModel.updateLanguage("es")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - Exception logged but not thrown
        coVerify(exactly = 1) { updateLanguageUseCase("es") }
    }
}
