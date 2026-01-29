package com.eugene.lift.domain.usecase.workout

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit test for StartEmptyWorkoutUseCase
 * Tests the creation of empty workout sessions
 */
class StartEmptyWorkoutUseCaseTest {

    private lateinit var useCase: StartEmptyWorkoutUseCase

    @Before
    fun setup() {
        useCase = StartEmptyWorkoutUseCase()
    }

    @Test
    fun `invoke creates session with default name`() = runTest {
        // WHEN
        val result = useCase()

        // THEN
        assertNotNull(result)
        assertEquals("Quick Workout", result.name)
    }

    @Test
    fun `invoke creates session with no template`() = runTest {
        // WHEN
        val result = useCase()

        // THEN
        assertNotNull(result)
        assertNull("Session should not have templateId", result.templateId)
    }

    @Test
    fun `invoke creates session with empty exercises list`() = runTest {
        // WHEN
        val result = useCase()

        // THEN
        assertNotNull(result)
        assertTrue("Session should have empty exercises list", result.exercises.isEmpty())
    }

    @Test
    fun `invoke creates session with zero duration`() = runTest {
        // WHEN
        val result = useCase()

        // THEN
        assertNotNull(result)
        assertEquals(0L, result.durationSeconds)
    }

    @Test
    fun `invoke creates session with current timestamp`() = runTest {
        // GIVEN
        val beforeTime = LocalDateTime.now()

        // WHEN
        val result = useCase()

        // THEN
        val afterTime = LocalDateTime.now()
        assertNotNull(result)
        assertTrue("Session date should be after beforeTime",
            result.date.isAfter(beforeTime.minusSeconds(1)))
        assertTrue("Session date should be before afterTime",
            result.date.isBefore(afterTime.plusSeconds(1)))
    }

    @Test
    fun `invoke generates unique session IDs`() = runTest {
        // WHEN
        val result1 = useCase()
        val result2 = useCase()

        // THEN
        assertNotNull(result1)
        assertNotNull(result2)
        assertTrue("Session IDs should be different", result1.id != result2.id)
    }

    @Test
    fun `invoke creates multiple sessions independently`() = runTest {
        // WHEN
        val sessions = (1..10).map { useCase() }

        // THEN
        assertEquals(10, sessions.size)

        // All IDs should be unique
        val uniqueIds = sessions.map { it.id }.distinct()
        assertEquals(10, uniqueIds.size)

        // All should have default properties
        assertTrue(sessions.all { it.name == "Quick Workout" })
        assertTrue(sessions.all { it.templateId == null })
        assertTrue(sessions.all { it.exercises.isEmpty() })
        assertTrue(sessions.all { it.durationSeconds == 0L })
    }
}
