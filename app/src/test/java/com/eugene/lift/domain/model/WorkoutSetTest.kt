package com.eugene.lift.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutSetTest {

    @Test
    fun rpe_acceptsHalfStepsWithinRange() {
        assertTrue(WorkoutSet.isValidRpe(1.0))
        assertTrue(WorkoutSet.isValidRpe(7.5))
        assertTrue(WorkoutSet.isValidRpe(10.0))
        assertFalse(WorkoutSet.isValidRpe(0.5))
        assertFalse(WorkoutSet.isValidRpe(7.25))
        assertFalse(WorkoutSet.isValidRpe(10.5))
    }

    @Test
    fun rir_acceptsIntegersWithinRange() {
        assertTrue(WorkoutSet.isValidRir(0))
        assertTrue(WorkoutSet.isValidRir(5))
        assertTrue(WorkoutSet.isValidRir(10))
        assertFalse(WorkoutSet.isValidRir(-1))
        assertFalse(WorkoutSet.isValidRir(11))
    }
}
