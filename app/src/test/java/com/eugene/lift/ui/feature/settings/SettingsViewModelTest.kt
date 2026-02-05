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

    // ========== DEFECT: Multiple concurrent updates ==========
    @Test
    fun `DEFECT CHECK - rapid consecutive theme updates should all be processed`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateThemeUseCase(any()) } returns Unit

        // WHEN - Rapid updates
        repeat(5) {
            viewModel.updateTheme(AppTheme.DARK)
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - All should be processed (potential race condition if not handled properly)
        coVerify(exactly = 5) { updateThemeUseCase(AppTheme.DARK) }
    }

    @Test
    fun `DEFECT CHECK - updating different settings concurrently should not interfere`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateThemeUseCase(any()) } returns Unit
        coEvery { updateWeightUnitUseCase(any()) } returns Unit
        coEvery { updateLanguageUseCase(any()) } returns Unit

        // WHEN - Update different settings rapidly
        viewModel.updateTheme(AppTheme.DARK)
        viewModel.updateWeightUnit(WeightUnit.LBS)
        viewModel.updateLanguage("es")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - All should be called correctly (no interference)
        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.DARK) }
        coVerify(exactly = 1) { updateWeightUnitUseCase(WeightUnit.LBS) }
        coVerify(exactly = 1) { updateLanguageUseCase("es") }
    }

    // ========== DEFECT: Edge cases for language codes ==========
    @Test
    fun `DEFECT CHECK - empty language code should still be processed`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateLanguageUseCase("") } returns Unit

        // WHEN - Empty string (should validation be in ViewModel or UseCase?)
        viewModel.updateLanguage("")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - Should still call use case (validation should be in domain layer)
        coVerify(exactly = 1) { updateLanguageUseCase("") }
    }

    @Test
    fun `DEFECT CHECK - invalid language code format should still be processed`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateLanguageUseCase("invalid-lang-code") } returns Unit

        // WHEN
        viewModel.updateLanguage("invalid-lang-code")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - Should delegate validation to domain layer
        coVerify(exactly = 1) { updateLanguageUseCase("invalid-lang-code") }
    }

    // ========== DEFECT: Settings state consistency ==========
    @Test
    fun `DEFECT CHECK - settings flow should emit updated values after changes`() = runTest {
        // GIVEN
        val updatedSettings = defaultSettings.copy(theme = AppTheme.DARK)
        every { getSettingsUseCase() } returns flowOf(defaultSettings, updatedSettings)
        createViewModel()

        // THEN - Should see both initial and updated values
        viewModel.settings.test {
            val initial = awaitItem()
            assertEquals(AppTheme.SYSTEM, initial.theme)

            val updated = awaitItem()
            assertEquals(AppTheme.DARK, updated.theme)
        }
    }

    @Test
    fun `DEFECT CHECK - multiple setting changes should all reflect in flow`() = runTest {
        // GIVEN
        val settings1 = defaultSettings
        val settings2 = settings1.copy(weightUnit = WeightUnit.LBS)
        val settings3 = settings2.copy(distanceUnit = DistanceUnit.MILES)
        val settings4 = settings3.copy(languageCode = "es")

        every { getSettingsUseCase() } returns flowOf(settings1, settings2, settings3, settings4)
        createViewModel()

        // THEN - Should see all transitions
        viewModel.settings.test {
            assertEquals(WeightUnit.KG, awaitItem().weightUnit)
            assertEquals(WeightUnit.LBS, awaitItem().weightUnit)
            assertEquals(DistanceUnit.MILES, awaitItem().distanceUnit)
            assertEquals("es", awaitItem().languageCode)
        }
    }

    // ========== DEFECT: getCurrentLanguageCode behavior ==========
    @Test
    fun `DEFECT CHECK - getCurrentLanguageCode should return Spanish when set`() {
        // GIVEN
        every { getCurrentLanguageUseCase() } returns "es"
        createViewModel()

        // WHEN
        val result = viewModel.getCurrentLanguageCode()

        // THEN
        assertEquals("es", result)
    }

    @Test
    fun `DEFECT CHECK - getCurrentLanguageCode should be callable multiple times`() {
        // GIVEN
        every { getCurrentLanguageUseCase() } returns "en"
        createViewModel()

        // WHEN - Call multiple times
        val result1 = viewModel.getCurrentLanguageCode()
        val result2 = viewModel.getCurrentLanguageCode()
        val result3 = viewModel.getCurrentLanguageCode()

        // THEN - Should always return same value
        assertEquals("en", result1)
        assertEquals("en", result2)
        assertEquals("en", result3)
    }

    // ========== DEFECT: All theme variations ==========
    @Test
    fun `DEFECT CHECK - all AppTheme values should be updateable`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateThemeUseCase(any()) } returns Unit

        // WHEN - Try all theme values
        viewModel.updateTheme(AppTheme.LIGHT)
        viewModel.updateTheme(AppTheme.DARK)
        viewModel.updateTheme(AppTheme.SYSTEM)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - All should be processed
        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.LIGHT) }
        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.DARK) }
        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.SYSTEM) }
    }

    // ========== DEFECT: Switching back and forth ==========
    @Test
    fun `DEFECT CHECK - toggling between weight units should work correctly`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateWeightUnitUseCase(any()) } returns Unit

        // WHEN - Toggle back and forth
        viewModel.updateWeightUnit(WeightUnit.LBS)
        viewModel.updateWeightUnit(WeightUnit.KG)
        viewModel.updateWeightUnit(WeightUnit.LBS)
        viewModel.updateWeightUnit(WeightUnit.KG)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - Each call should be processed
        coVerify(exactly = 2) { updateWeightUnitUseCase(WeightUnit.LBS) }
        coVerify(exactly = 2) { updateWeightUnitUseCase(WeightUnit.KG) }
    }

    @Test
    fun `DEFECT CHECK - toggling between distance units should work correctly`() = runTest {
        // GIVEN
        createViewModel()
        coEvery { updateDistanceUnitUseCase(any()) } returns Unit

        // WHEN - Toggle back and forth
        viewModel.updateDistanceUnit(DistanceUnit.MILES)
        viewModel.updateDistanceUnit(DistanceUnit.KM)
        viewModel.updateDistanceUnit(DistanceUnit.MILES)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - Each call should be processed
        coVerify(exactly = 2) { updateDistanceUnitUseCase(DistanceUnit.MILES) }
        coVerify(exactly = 1) { updateDistanceUnitUseCase(DistanceUnit.KM) }
    }
}
