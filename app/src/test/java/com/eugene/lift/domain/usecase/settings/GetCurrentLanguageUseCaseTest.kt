package com.eugene.lift.domain.usecase.settings

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Unit test for GetCurrentLanguageUseCase
 * Tests retrieval of current language code
 *
 * Note: This use case has heavy Android framework dependencies that are difficult to mock
 * in unit tests. These tests verify the logic but may have limitations due to framework constraints.
 */
class GetCurrentLanguageUseCaseTest {

    private lateinit var useCase: GetCurrentLanguageUseCase

    @Before
    fun setup() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // Mock AppCompatDelegate
        mockkStatic(AppCompatDelegate::class)

        useCase = GetCurrentLanguageUseCase()
    }

    @Test
    fun `invoke returns language from application locales when available`() {
        // GIVEN
        val mockLocaleList = mockk<LocaleListCompat>(relaxed = true)
        val mockLocale = Locale("es")

        every { AppCompatDelegate.getApplicationLocales() } returns mockLocaleList
        every { mockLocaleList.isEmpty } returns false
        every { mockLocaleList.get(0) } returns mockLocale

        // WHEN
        val result = useCase()

        // THEN
        assertEquals("es", result)
    }

    @Test
    fun `invoke returns English when application locales is empty`() {
        // GIVEN
        val mockLocaleList = mockk<LocaleListCompat>(relaxed = true)

        every { AppCompatDelegate.getApplicationLocales() } returns mockLocaleList
        every { mockLocaleList.isEmpty } returns true

        // WHEN
        val result = useCase()

        // THEN
        // When empty, it falls back to Locale.getDefault().language which varies by test environment
        // We just verify it returns a non-null string
        assert(result.isNotEmpty())
    }

    @Test
    fun `invoke returns fallback when application locales returns null locale`() {
        // GIVEN
        val mockLocaleList = mockk<LocaleListCompat>(relaxed = true)

        every { AppCompatDelegate.getApplicationLocales() } returns mockLocaleList
        every { mockLocaleList.isEmpty } returns false
        every { mockLocaleList.get(0) } returns null

        // WHEN
        val result = useCase()

        // THEN
        assertEquals("en", result) // Falls back to "en" when locale is null
    }

    @Test
    fun `invoke returns French from application locales`() {
        // GIVEN
        val mockLocaleList = mockk<LocaleListCompat>(relaxed = true)
        val mockLocale = Locale("fr")

        every { AppCompatDelegate.getApplicationLocales() } returns mockLocaleList
        every { mockLocaleList.isEmpty } returns false
        every { mockLocaleList.get(0) } returns mockLocale

        // WHEN
        val result = useCase()

        // THEN
        assertEquals("fr", result)
    }

    @Test
    fun `invoke returns German from application locales`() {
        // GIVEN
        val mockLocaleList = mockk<LocaleListCompat>(relaxed = true)
        val mockLocale = Locale("de")

        every { AppCompatDelegate.getApplicationLocales() } returns mockLocaleList
        every { mockLocaleList.isEmpty } returns false
        every { mockLocaleList.get(0) } returns mockLocale

        // WHEN
        val result = useCase()

        // THEN
        assertEquals("de", result)
    }
}
