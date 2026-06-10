package com.eugene.lift.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseNameNormalizerTest {

    private val normalizer = ExerciseNameNormalizer()

    @Test
    fun `maps curated seed and remote aliases onto the same canonical key`() {
        assertEquals("barbell squat", normalizer.normalize("Back Squat"))
        assertEquals("barbell squat", normalizer.normalize("Squats"))
        assertEquals("barbell squat", normalizer.normalize("Sentadilla Trasera"))
        assertEquals("barbell squat", normalizer.normalize("Sentadillas"))

        assertEquals("barbell hip thrust", normalizer.normalize("Hip Thrust (Barbell)"))
        assertEquals("barbell hip thrust", normalizer.normalize("Hip Thrust"))
        assertEquals("barbell hip thrust", normalizer.normalize("Hip Thrust con Barra"))

        assertEquals("dumbbell lateral raise", normalizer.normalize("Lateral Raise (Dumbbell)"))
        assertEquals("dumbbell lateral raise", normalizer.normalize("Lateral Raises"))
        assertEquals("dumbbell lateral raise", normalizer.normalize("Elevación lateral con mancuernas"))
    }
}
