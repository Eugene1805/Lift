package com.eugene.lift.domain.util

import java.text.Normalizer
import javax.inject.Inject

class ExerciseNameNormalizer @Inject constructor() {

    fun normalize(name: String): String {
        val normalized = Normalizer.normalize(name.lowercase().trim(), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .replace("[^a-z0-9]+".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()

        return canonicalAliases[normalized]
            ?: pluralAliases[normalized]
            ?: normalized
    }

    private companion object {
        val canonicalAliases = mapOf(
            "bench press barbell" to "bench press",
            "press de banca barra" to "bench press",
            "back squat" to "barbell squat",
            "sentadilla trasera" to "barbell squat",
            "squats" to "barbell squat",
            "sentadillas" to "barbell squat",
            "hip thrust barbell" to "barbell hip thrust",
            "barbell hip thrust" to "barbell hip thrust",
            "hip thrust con barra" to "barbell hip thrust",
            "empuje de cadera barra" to "barbell hip thrust",
            "hip thrust" to "barbell hip thrust",
            "lateral raise dumbbell" to "dumbbell lateral raise",
            "lateral raises" to "dumbbell lateral raise",
            "elevacion lateral mancuernas" to "dumbbell lateral raise",
            "elevacion lateral con mancuernas" to "dumbbell lateral raise",
            "pull ups" to "pull up"
        )

        val pluralAliases = mapOf(
            "dips" to "dip"
        )
    }
}
