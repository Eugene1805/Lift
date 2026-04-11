package com.eugene.lift.ui.util

import com.eugene.lift.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightFormattersTest {

    @Test
    fun `lbs snaps near integers to integer`() {
        assertEquals("55", WeightFormatters.formatWeight(54.95, WeightUnit.LBS))
        assertEquals("55", WeightFormatters.formatWeight(55.04, WeightUnit.LBS))
        assertEquals("100", WeightFormatters.formatWeight(99.96, WeightUnit.LBS))
    }

    @Test
    fun `lbs preserves intentional quarter increments`() {
        assertEquals("0.5", WeightFormatters.formatWeight(0.5, WeightUnit.LBS))
        assertEquals("10.25", WeightFormatters.formatWeight(10.25, WeightUnit.LBS))
        assertEquals("10.5", WeightFormatters.formatWeight(10.5, WeightUnit.LBS))
        assertEquals("10.75", WeightFormatters.formatWeight(10.75, WeightUnit.LBS))
    }

    @Test
    fun `lbs snaps near quarter increments to quarter increments`() {
        // Typical conversion noise like x.24/x.26 should display as x.25
        assertEquals("10.25", WeightFormatters.formatWeight(10.24, WeightUnit.LBS))
        assertEquals("10.25", WeightFormatters.formatWeight(10.26, WeightUnit.LBS))
        // ...and similarly for halves
        assertEquals("10.5", WeightFormatters.formatWeight(10.49, WeightUnit.LBS))
        assertEquals("10.5", WeightFormatters.formatWeight(10.51, WeightUnit.LBS))
    }

    @Test
    fun `kg does not apply lbs snapping`() {
        assertEquals("54.9", WeightFormatters.formatWeight(54.9, WeightUnit.KG))
        assertEquals("25", WeightFormatters.formatWeight(25.0, WeightUnit.KG))
    }
}

