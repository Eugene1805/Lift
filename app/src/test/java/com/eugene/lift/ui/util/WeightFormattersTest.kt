package com.eugene.lift.ui.util

import com.eugene.lift.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightFormattersTest {

    @Test
    fun `rounds to nearest integer or half`() {
        // Values closer to integers
        assertEquals("55", WeightFormatters.formatWeight(54.95, WeightUnit.LBS))
        assertEquals("55", WeightFormatters.formatWeight(55.04, WeightUnit.LBS))
        assertEquals("100", WeightFormatters.formatWeight(99.96, WeightUnit.LBS))
        
        // Values closer to .5
        assertEquals("10.5", WeightFormatters.formatWeight(10.49, WeightUnit.LBS))
        assertEquals("10.5", WeightFormatters.formatWeight(10.51, WeightUnit.LBS))
        assertEquals("10.5", WeightFormatters.formatWeight(10.3, WeightUnit.LBS))
        assertEquals("10.5", WeightFormatters.formatWeight(10.7, WeightUnit.LBS))
    }

    @Test
    fun `handles kg identically`() {
        assertEquals("55", WeightFormatters.formatWeight(54.9, WeightUnit.KG))
        assertEquals("25", WeightFormatters.formatWeight(25.0, WeightUnit.KG))
        assertEquals("25.5", WeightFormatters.formatWeight(25.6, WeightUnit.KG))
    }
}
