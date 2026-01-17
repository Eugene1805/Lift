package com.eugene.lift.domain.util

import java.math.BigDecimal
import java.math.RoundingMode

object WeightConverter {
    private const val KG_TO_LBS_FACTOR = 2.20462

    fun kgToLbs(kg: Double): Double {
        return BigDecimal(kg * KG_TO_LBS_FACTOR)
            .setScale(1, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun lbsToKg(lbs: Double): Double {
        return BigDecimal(lbs / KG_TO_LBS_FACTOR)
            .setScale(1, RoundingMode.HALF_UP)
            .toDouble()
    }
}