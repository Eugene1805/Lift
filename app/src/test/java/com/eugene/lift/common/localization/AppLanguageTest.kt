package com.eugene.lift.common.localization

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `supported language codes are preserved`() {
        assertEquals("es", resolveSupportedLanguageCode("es"))
        assertEquals("en", resolveSupportedLanguageCode("en"))
    }

    @Test
    fun `unsupported or malformed language codes fall back to english`() {
        assertEquals("en", resolveSupportedLanguageCode("fr"))
        assertEquals("en", resolveSupportedLanguageCode("pt-BR"))
        assertEquals("en", resolveSupportedLanguageCode(""))
    }
}
