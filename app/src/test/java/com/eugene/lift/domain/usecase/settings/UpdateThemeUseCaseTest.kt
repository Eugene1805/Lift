package com.eugene.lift.domain.usecase.settings

import android.util.Log
import com.eugene.lift.domain.model.AppTheme
import com.eugene.lift.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit test for UpdateThemeUseCase
 * Tests theme update functionality
 */
class UpdateThemeUseCaseTest {

    private lateinit var repository: SettingsRepository
    private lateinit var useCase: UpdateThemeUseCase

    @Before
    fun setup() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        repository = mockk(relaxed = true)
        useCase = UpdateThemeUseCase(repository)
    }

    @Test
    fun `invoke updates theme to ORCA`() = runTest {
        // WHEN
        useCase(AppTheme.ORCA)

        // THEN
        coVerify(exactly = 1) { repository.setTheme(AppTheme.ORCA) }
    }

    @Test
    fun `invoke updates theme to MAKO`() = runTest {
        // WHEN
        useCase(AppTheme.MAKO)

        // THEN
        coVerify(exactly = 1) { repository.setTheme(AppTheme.MAKO) }
    }

    @Test
    fun `invoke updates theme to FOX`() = runTest {
        // WHEN
        useCase(AppTheme.FOX)

        // THEN
        coVerify(exactly = 1) { repository.setTheme(AppTheme.FOX) }
    }

    @Test
    fun `invoke handles multiple theme updates`() = runTest {
        // WHEN
        useCase(AppTheme.ORCA)
        useCase(AppTheme.VIPER)
        useCase(AppTheme.LION)

        // THEN
        coVerify(exactly = 1) { repository.setTheme(AppTheme.ORCA) }
        coVerify(exactly = 1) { repository.setTheme(AppTheme.VIPER) }
        coVerify(exactly = 1) { repository.setTheme(AppTheme.LION) }
    }

    @Test
    fun `invoke can toggle between themes`() = runTest {
        // WHEN
        useCase(AppTheme.ORCA)
        useCase(AppTheme.MAKO)
        useCase(AppTheme.ORCA)

        // THEN
        coVerify(exactly = 2) { repository.setTheme(AppTheme.ORCA) }
        coVerify(exactly = 1) { repository.setTheme(AppTheme.MAKO) }
    }
}
