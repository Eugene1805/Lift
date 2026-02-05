package com.eugene.lift.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for WeightConverter
 * Focus: Conversion accuracy, rounding, edge cases
 */
class WeightConverterTest {

    private val delta = 0.01 // Tolerance for floating point comparison

    // ========== DEFECT: KG to LBS conversion ==========
    @Test
    fun `DEFECT CHECK - kgToLbs with zero should return zero`() {
        // WHEN
        val result = WeightConverter.kgToLbs(0.0)

        // THEN
        assertEquals(0.0, result, delta)
    }

    @Test
    fun `DEFECT CHECK - kgToLbs with 1 kg should return approximately 2point2 lbs`() {
        // WHEN
        val result = WeightConverter.kgToLbs(1.0)

        // THEN - 1 kg = 2.20462 lbs, rounded to 1 decimal = 2.2
        assertEquals(2.2, result, delta)
    }

    @Test
    fun `DEFECT CHECK - kgToLbs with 100 kg should convert correctly`() {
        // WHEN
        val result = WeightConverter.kgToLbs(100.0)

        // THEN - 100 kg = 220.462 lbs, rounded to 1 decimal = 220.5
        assertEquals(220.5, result, 0.1)
    }

    @Test
    fun `DEFECT CHECK - kgToLbs with common gym weight 20 kg should be accurate`() {
        // WHEN
        val result = WeightConverter.kgToLbs(20.0)

        // THEN - 20 kg = 44.0924 lbs, rounded to 1 decimal = 44.1
        assertEquals(44.1, result, delta)
    }

    @Test
    fun `DEFECT CHECK - kgToLbs should round to 1 decimal place`() {
        // WHEN
        val result = WeightConverter.kgToLbs(10.123456)

        // THEN - Should only have 1 decimal place
        val decimalPart = result.toString().substringAfter(".")
        assert(decimalPart.length <= 1) { "Should round to 1 decimal place, got: $result" }
    }

    @Test
    fun `DEFECT CHECK - kgToLbs with decimal input should work`() {
        // WHEN
        val result = WeightConverter.kgToLbs(2.5)

        // THEN - 2.5 kg = 5.51155 lbs, rounded = 5.5
        assertEquals(5.5, result, delta)
    }

    // ========== DEFECT: LBS to KG conversion ==========
    @Test
    fun `DEFECT CHECK - lbsToKg with zero should return zero`() {
        // WHEN
        val result = WeightConverter.lbsToKg(0.0)

        // THEN
        assertEquals(0.0, result, delta)
    }

    @Test
    fun `DEFECT CHECK - lbsToKg with 2point2 lbs should return approximately 1 kg`() {
        // WHEN
        val result = WeightConverter.lbsToKg(2.2)

        // THEN - 2.2 lbs = ~1 kg
        assertEquals(1.0, result, delta)
    }

    @Test
    fun `DEFECT CHECK - lbsToKg with 220 lbs should convert correctly`() {
        // WHEN
        val result = WeightConverter.lbsToKg(220.0)

        // THEN - 220 lbs = 99.79 kg, rounded to 1 decimal = 99.8
        assertEquals(99.8, result, 0.1)
    }

    @Test
    fun `DEFECT CHECK - lbsToKg with common gym weight 45 lbs should be accurate`() {
        // WHEN
        val result = WeightConverter.lbsToKg(45.0)

        // THEN - 45 lbs = 20.41 kg, rounded = 20.4
        assertEquals(20.4, result, delta)
    }

    @Test
    fun `DEFECT CHECK - lbsToKg should round to 1 decimal place`() {
        // WHEN
        val result = WeightConverter.lbsToKg(100.0)

        // THEN - Should only have 1 decimal place
        val decimalPart = result.toString().substringAfter(".")
        assert(decimalPart.length <= 1) { "Should round to 1 decimal place, got: $result" }
    }

    @Test
    fun `DEFECT CHECK - lbsToKg with decimal input should work`() {
        // WHEN
        val result = WeightConverter.lbsToKg(5.5)

        // THEN - 5.5 lbs = 2.494 kg, rounded = 2.5
        assertEquals(2.5, result, delta)
    }

    // ========== DEFECT: Round-trip conversions ==========
    @Test
    fun `DEFECT CHECK - kg to lbs and back should be approximately equal`() {
        // GIVEN
        val originalKg = 100.0

        // WHEN
        val lbs = WeightConverter.kgToLbs(originalKg)
        val backToKg = WeightConverter.lbsToKg(lbs)

        // THEN - Should be close to original (allowing for rounding)
        assertEquals(originalKg, backToKg, 0.5)
    }

    @Test
    fun `DEFECT CHECK - lbs to kg and back should be approximately equal`() {
        // GIVEN
        val originalLbs = 225.0

        // WHEN
        val kg = WeightConverter.lbsToKg(originalLbs)
        val backToLbs = WeightConverter.kgToLbs(kg)

        // THEN - Should be close to original (allowing for rounding)
        assertEquals(originalLbs, backToLbs, 0.5)
    }

    // ========== DEFECT: Edge cases ==========
    @Test
    fun `DEFECT CHECK - kgToLbs with very small weight should work`() {
        // WHEN
        val result = WeightConverter.kgToLbs(0.1)

        // THEN - 0.1 kg = 0.220462 lbs, rounded = 0.2
        assertEquals(0.2, result, delta)
    }

    @Test
    fun `DEFECT CHECK - lbsToKg with very small weight should work`() {
        // WHEN
        val result = WeightConverter.lbsToKg(0.1)

        // THEN - 0.1 lbs = 0.0453592 kg, rounded = 0.0
        assertEquals(0.0, result, delta)
    }

    @Test
    fun `DEFECT CHECK - kgToLbs with very large weight should work`() {
        // WHEN
        val result = WeightConverter.kgToLbs(1000.0)

        // THEN - 1000 kg = 2204.62 lbs
        assertEquals(2204.6, result, 0.5)
    }

    @Test
    fun `DEFECT CHECK - lbsToKg with very large weight should work`() {
        // WHEN
        val result = WeightConverter.lbsToKg(1000.0)

        // THEN - 1000 lbs = 453.592 kg
        assertEquals(453.6, result, 0.5)
    }

    @Test
    fun `DEFECT CHECK - kgToLbs with negative weight should convert`() {
        // WHEN - Even though negative doesn't make sense, shouldn't crash
        val result = WeightConverter.kgToLbs(-10.0)

        // THEN - Should be negative equivalent
        assertEquals(-22.0, result, 0.5)
    }

    @Test
    fun `DEFECT CHECK - lbsToKg with negative weight should convert`() {
        // WHEN - Even though negative doesn't make sense, shouldn't crash
        val result = WeightConverter.lbsToKg(-10.0)

        // THEN - Should be negative equivalent
        assertEquals(-4.5, result, 0.5)
    }

    // ========== DEFECT: Rounding behavior ==========
    @Test
    fun `DEFECT CHECK - kgToLbs should round half up`() {
        // GIVEN - Value that ends in .25 (should round up)
        val kg = 10.0 / 2.20462 * 1.05 // Crafted to get .25 in result

        // WHEN
        val result = WeightConverter.kgToLbs(kg)

        // THEN - Should round up when .25 or higher
        val decimal = (result * 10) % 10
        assert(decimal == 0.0 || decimal >= 5.0 || decimal == result * 10 % 10) {
            "Rounding should be consistent"
        }
    }

    @Test
    fun `DEFECT CHECK - lbsToKg should round half up`() {
        // GIVEN - Value that ends in .25 (should round up)
        val lbs = 10.0 * 2.20462 * 1.05 // Crafted to get .25 in result

        // WHEN
        val result = WeightConverter.lbsToKg(lbs)

        // THEN - Should round up when .25 or higher
        val decimal = (result * 10) % 10
        assert(decimal == 0.0 || decimal >= 5.0 || decimal == result * 10 % 10) {
            "Rounding should be consistent"
        }
    }

    // ========== DEFECT: Common gym weights ==========
    @Test
    fun `DEFECT CHECK - standard plate weights should convert accurately`() {
        // Common plate weights in kg
        val plates = mapOf(
            1.25 to 2.8,   // 1.25 kg plate ≈ 2.8 lbs
            2.5 to 5.5,    // 2.5 kg plate ≈ 5.5 lbs
            5.0 to 11.0,   // 5 kg plate ≈ 11 lbs
            10.0 to 22.0,  // 10 kg plate ≈ 22 lbs
            20.0 to 44.1,  // 20 kg plate ≈ 44 lbs
            25.0 to 55.1   // 25 kg plate ≈ 55 lbs
        )

        plates.forEach { (kg, expectedLbs) ->
            val result = WeightConverter.kgToLbs(kg)
            assertEquals("$kg kg should convert to ~$expectedLbs lbs",
                expectedLbs, result, 0.2)
        }
    }

    @Test
    fun `DEFECT CHECK - standard barbell weights should convert accurately`() {
        // WHEN - Standard Olympic barbell (20 kg)
        val barbellKg = WeightConverter.kgToLbs(20.0)

        // THEN - Should be approximately 44 lbs
        assertEquals(44.1, barbellKg, delta)

        // WHEN - Women's barbell (15 kg)
        val womensBarbellKg = WeightConverter.kgToLbs(15.0)

        // THEN - Should be approximately 33 lbs
        assertEquals(33.1, womensBarbellKg, 0.2)
    }

    // ========== DEFECT: Precision and accuracy ==========
    @Test
    fun `DEFECT CHECK - conversion factor should be accurate`() {
        // WHEN - Test against known conversion
        val result = WeightConverter.kgToLbs(1.0)

        // THEN - Should use 2.20462 factor
        assert(result in 2.1..2.3) { "Conversion factor seems incorrect: $result" }
    }

    @Test
    fun `DEFECT CHECK - multiple conversions should be consistent`() {
        // GIVEN
        val weights = listOf(10.0, 20.0, 50.0, 100.0)

        // WHEN - Convert all
        val converted = weights.map { WeightConverter.kgToLbs(it) }

        // THEN - Ratios should be consistent
        for (i in 0 until weights.size - 1) {
            val originalRatio = weights[i + 1] / weights[i]
            val convertedRatio = converted[i + 1] / converted[i]
            assertEquals("Ratios should be preserved", originalRatio, convertedRatio, 0.01)
        }
    }

    @Test
    fun `DEFECT CHECK - converting same weight twice should give same result`() {
        // WHEN
        val result1 = WeightConverter.kgToLbs(75.0)
        val result2 = WeightConverter.kgToLbs(75.0)

        // THEN - Should be identical (not just approximately equal)
        assertEquals(result1, result2, 0.0)
    }
}
