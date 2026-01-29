package com.eugene.lift.domain.usecase.settings

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.eugene.lift.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit test for UpdateLanguageUseCase
 * Tests language update functionality
 */
class UpdateLanguageUseCaseTest {

    private lateinit var repository: SettingsRepository
    private lateinit var useCase: UpdateLanguageUseCase

    @Before
    fun setup() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Mock LocaleListCompat
        mockkStatic(LocaleListCompat::class)
        val mockLocaleList = mockk<LocaleListCompat>(relaxed = true)
        every { LocaleListCompat.forLanguageTags(any()) } returns mockLocaleList

        // Mock AppCompatDelegate
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.setApplicationLocales(any()) } returns Unit

        repository = mockk(relaxed = true)
        useCase = UpdateLanguageUseCase(repository)
    }

    @Test
    fun `invoke updates language to English`() = runTest {
        // WHEN
        useCase("en")

        // THEN
        coVerify(exactly = 1) { repository.setLanguageCode("en") }
    }

    @Test
    fun `invoke updates language to Spanish`() = runTest {
        // WHEN
        useCase("es")

        // THEN
        coVerify(exactly = 1) { repository.setLanguageCode("es") }
    }

    @Test
    fun `invoke updates language to French`() = runTest {
        // WHEN
        useCase("fr")

        // THEN
        coVerify(exactly = 1) { repository.setLanguageCode("fr") }
    }

    @Test
    fun `invoke handles multiple language updates`() = runTest {
        // WHEN
        useCase("en")
        useCase("es")
        useCase("en")

        // THEN
        coVerify(exactly = 2) { repository.setLanguageCode("en") }
        coVerify(exactly = 1) { repository.setLanguageCode("es") }
    }

    @Test
    fun `invoke handles language codes with region`() = runTest {
        // WHEN
        useCase("en-US")

        // THEN
        coVerify(exactly = 1) { repository.setLanguageCode("en-US") }
    }
}
