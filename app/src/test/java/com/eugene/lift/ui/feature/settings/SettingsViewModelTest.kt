package com.eugene.lift.ui.feature.settings

import android.util.Log
import app.cash.turbine.test
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.usecase.settings.GetSettingsUseCase
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
        every { getCurrentLanguageUseCase() } returns "en"
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
    fun `uiState emits initial settings`() = runTest {
        createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            // It could be either the initialized version or the flow emitted version first
            if (state.theme == AppTheme.SYSTEM) {
                assertEquals(AppTheme.SYSTEM, state.theme)
                assertEquals(WeightUnit.KG, state.weightUnit)
                assertEquals(DistanceUnit.KM, state.distanceUnit)
                assertEquals("en", state.languageCode)
            } else {
                val state2 = awaitItem()
                assertEquals(AppTheme.SYSTEM, state2.theme)
                assertEquals(WeightUnit.KG, state2.weightUnit)
                assertEquals(DistanceUnit.KM, state2.distanceUnit)
                assertEquals("en", state2.languageCode)
            }
        }
    }

    @Test
    fun `ThemeChanged event calls use case`() = runTest {
        createViewModel()

        viewModel.onEvent(SettingsUiEvent.ThemeChanged(AppTheme.DARK))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { updateThemeUseCase(AppTheme.DARK) }
    }

    @Test
    fun `WeightUnitChanged event calls use case`() = runTest {
        createViewModel()

        viewModel.onEvent(SettingsUiEvent.WeightUnitChanged(WeightUnit.LBS))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { updateWeightUnitUseCase(WeightUnit.LBS) }
    }

    @Test
    fun `DistanceUnitChanged event calls use case`() = runTest {
        createViewModel()

        viewModel.onEvent(SettingsUiEvent.DistanceUnitChanged(DistanceUnit.MILES))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { updateDistanceUnitUseCase(DistanceUnit.MILES) }
    }

    @Test
    fun `LanguageChanged event calls use case`() = runTest {
        createViewModel()

        viewModel.onEvent(SettingsUiEvent.LanguageChanged("es"))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { updateLanguageUseCase("es") }
    }

    @Test
    fun `uiState emits updated settings`() = runTest {
        val updatedSettings = defaultSettings.copy(theme = AppTheme.DARK)
        coEvery { getSettingsUseCase() } returns flowOf(defaultSettings, updatedSettings)
        
        createViewModel()

        viewModel.uiState.test {
            val state1 = awaitItem() // Initial
            if (state1.theme != AppTheme.SYSTEM) {
                val state2 = awaitItem() // Wait for real emissions
            }
            val state3 = awaitItem() // The update
            assertEquals(AppTheme.DARK, state3.theme)
        }
    }
}
