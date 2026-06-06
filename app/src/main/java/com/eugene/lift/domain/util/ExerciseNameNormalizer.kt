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

        return normalized
    }
}
