package com.eugene.lift.ui.util

import com.eugene.lift.domain.model.WeightUnit
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Centralized formatting for weight values shown in the UI.
 *
 * Requirements:
 * - Prevent floating-point artifacts like 24.99.
 * - When displaying LBS (derived from KG), prefer whole numbers unless the user intentionally
 *   entered a fractional value (e.g. 0.5 lbs should remain 0.5).
 */
object WeightFormatters {

    private val LBS_INTEGER_TOLERANCE = BigDecimal("0.05")
    private val LBS_INCREMENT_TOLERANCE = BigDecimal("0.02")
    private val LBS_COMMON_INCREMENTS = listOf(
        BigDecimal("0.25"),
        BigDecimal("0.50"),
        BigDecimal("0.75")
    )

    fun formatWeight(value: Double, unit: WeightUnit): String {
        if (value == 0.0) return "0"

        // Normalize to avoid values like 24.999999.
        val normalized = BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        return when (unit) {
            WeightUnit.KG -> normalized.toPlainString()
            WeightUnit.LBS -> formatLbs(normalized)
        }
    }

    private fun formatLbs(value: BigDecimal): String {
        // Prefer displaying realistic plate increments (e.g., 5 lb, 2.5 lb, 1.25 lb per side).
        // This avoids UI artifacts like x.1 / x.9 that often happen after conversions.
        //
        // Rules (in order):
        // 1) Snap to nearest integer if extremely close.
        // 2) Snap to nearest quarter/half/three-quarter if extremely close.
        // 3) Otherwise show up to 2 decimals.

        // 1) Near integer -> integer
        val nearestInt = value.setScale(0, RoundingMode.HALF_UP)
        val diffToInt = value.subtract(nearestInt).abs()
        if (diffToInt <= LBS_INTEGER_TOLERANCE) return nearestInt.toPlainString()

        // 2) Near common increments -> snap
        val raw = value.setScale(2, RoundingMode.HALF_UP)
        val intPart = raw.setScale(0, RoundingMode.FLOOR)
        val fractional = raw.subtract(intPart)

        val snappedFractional = LBS_COMMON_INCREMENTS
            .minByOrNull { inc -> fractional.subtract(inc).abs() }
            ?.takeIf { inc -> fractional.subtract(inc).abs() <= LBS_INCREMENT_TOLERANCE }

        if (snappedFractional != null) {
            return intPart.add(snappedFractional).stripTrailingZeros().toPlainString()
        }

        // 3) Fallback: show normalized value
        return raw.stripTrailingZeros().toPlainString()
    }
}


