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

    fun formatWeight(value: Double, unit: WeightUnit): String {
        if (value == 0.0) return "0"

        // Scale to nearest 0.5 as requested by the user
        val rounded = Math.round(value * 2.0) / 2.0

        return if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            rounded.toString()
        }
    }
}


