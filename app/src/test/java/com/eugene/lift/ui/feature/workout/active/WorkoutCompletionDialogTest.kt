package com.eugene.lift.ui.feature.workout.active

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutCompletionDialogTest {

    @Test
    fun volumeMilestones_useKilogramReferenceWeights() {
        assertEquals(
            VolumeMilestone(VolumeMilestoneKind.BODYWEIGHT, 0),
            calculateVolumeMilestone(0.0)
        )
        assertEquals(
            VolumeMilestone(VolumeMilestoneKind.CATS, 100),
            calculateVolumeMilestone(450.0)
        )
        assertEquals(
            VolumeMilestone(VolumeMilestoneKind.CARS, 1),
            calculateVolumeMilestone(1_500.0)
        )
        assertEquals(
            VolumeMilestone(VolumeMilestoneKind.COWS, 5),
            calculateVolumeMilestone(3_000.0)
        )
        assertEquals(
            VolumeMilestone(VolumeMilestoneKind.YACHTS, 1),
            calculateVolumeMilestone(10_000.0)
        )
        assertEquals(
            VolumeMilestone(VolumeMilestoneKind.WHALES, 1),
            calculateVolumeMilestone(30_000.0)
        )
    }

    @Test
    fun volumeMilestones_rejectInvalidVolume() {
        assertEquals(
            VolumeMilestone(VolumeMilestoneKind.BODYWEIGHT, 0),
            calculateVolumeMilestone(Double.NaN)
        )
        assertEquals(
            VolumeMilestone(VolumeMilestoneKind.BODYWEIGHT, 0),
            calculateVolumeMilestone(-100.0)
        )
    }
}
