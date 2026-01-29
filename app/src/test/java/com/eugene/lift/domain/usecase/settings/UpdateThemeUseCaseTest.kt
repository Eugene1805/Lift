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
    fun `invoke updates theme to LIGHT`() = runTest {
        // WHEN
        useCase(AppTheme.LIGHT)

        // THEN
        coVerify(exactly = 1) { repository.setTheme(AppTheme.LIGHT) }
    }

    @Test
    fun `invoke updates theme to DARK`() = runTest {
        // WHEN
        useCase(AppTheme.DARK)

        // THEN
        coVerify(exactly = 1) { repository.setTheme(AppTheme.DARK) }
    }

    @Test
    fun `invoke updates theme to SYSTEM`() = runTest {
        // WHEN
        useCase(AppTheme.SYSTEM)

        // THEN
        coVerify(exactly = 1) { repository.setTheme(AppTheme.SYSTEM) }
    }

    @Test
    fun `invoke handles multiple theme updates`() = runTest {
        // WHEN
        useCase(AppTheme.LIGHT)
        useCase(AppTheme.DARK)
        useCase(AppTheme.SYSTEM)

        // THEN
        coVerify(exactly = 1) { repository.setTheme(AppTheme.LIGHT) }
        coVerify(exactly = 1) { repository.setTheme(AppTheme.DARK) }
        coVerify(exactly = 1) { repository.setTheme(AppTheme.SYSTEM) }
    }

    @Test
    fun `invoke can toggle between themes`() = runTest {
        // WHEN
        useCase(AppTheme.LIGHT)
        useCase(AppTheme.DARK)
        useCase(AppTheme.LIGHT)

        // THEN
        coVerify(exactly = 2) { repository.setTheme(AppTheme.LIGHT) }
        coVerify(exactly = 1) { repository.setTheme(AppTheme.DARK) }
    }
}
