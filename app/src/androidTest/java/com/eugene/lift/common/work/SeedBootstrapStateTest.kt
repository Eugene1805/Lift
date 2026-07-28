package com.eugene.lift.common.work

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SeedBootstrapStateTest {

    @Test
    fun completedVersion_isSkippedUntilWorkNameChanges() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val workName = "seed_test_${UUID.randomUUID()}"

        assertTrue(SeedBootstrapState.shouldEnqueue(context, workName))
        assertTrue(SeedBootstrapState.markCompleted(context, workName))
        assertFalse(SeedBootstrapState.shouldEnqueue(context, workName))
        assertTrue(SeedBootstrapState.shouldEnqueue(context, "${workName}_next"))
    }
}
