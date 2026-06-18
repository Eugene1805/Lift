package com.eugene.lift.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeTest {

    @Test
    fun `fromStorageValue maps legacy entries to default palette`() {
        assertEquals(AppTheme.ORCA, AppTheme.fromStorageValue("LIGHT"))
        assertEquals(AppTheme.ORCA, AppTheme.fromStorageValue("DARK"))
        assertEquals(AppTheme.ORCA, AppTheme.fromStorageValue("SYSTEM"))
    }

    @Test
    fun `fromStorageValue keeps new palette values`() {
        assertEquals(AppTheme.MAKO, AppTheme.fromStorageValue("MAKO"))
        assertEquals(AppTheme.VIPER, AppTheme.fromStorageValue("VIPER"))
        assertEquals(AppTheme.JELLYFISH, AppTheme.fromStorageValue("JELLYFISH"))
    }

    @Test
    fun `fromStorageValue falls back for null or unknown values`() {
        assertEquals(AppTheme.ORCA, AppTheme.fromStorageValue(null))
        assertEquals(AppTheme.ORCA, AppTheme.fromStorageValue("UNKNOWN"))
    }
}
