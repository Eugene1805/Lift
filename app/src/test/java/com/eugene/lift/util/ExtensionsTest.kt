package com.eugene.lift.util

import com.eugene.lift.domain.model.BodyPart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for utility functions
 * Tests helper functions and extensions
 */
class StringExtensionsTest {

    @Test
    fun `isBlank returns true for empty string`() {
        // GIVEN
        val text = ""

        // WHEN & THEN
        assertTrue(text.isBlank())
    }

    @Test
    fun `isBlank returns true for whitespace string`() {
        // GIVEN
        val text = "   "

        // WHEN & THEN
        assertTrue(text.isBlank())
    }

    @Test
    fun `isBlank returns false for non-empty string`() {
        // GIVEN
        val text = "Bench Press"

        // WHEN & THEN
        assertFalse(text.isBlank())
    }

    @Test
    fun `trim removes leading and trailing spaces`() {
        // GIVEN
        val text = "  Exercise Name  "

        // WHEN
        val result = text.trim()

        // THEN
        assertEquals("Exercise Name", result)
    }

    @Test
    fun `string truncation works correctly`() {
        // GIVEN
        val longText = "This is a very long exercise name"
        val maxLength = 10

        // WHEN
        val result = longText.take(maxLength)

        // THEN
        assertEquals(10, result.length)
        assertEquals("This is a ", result)
    }
}

class CollectionExtensionsTest {

    @Test
    fun `list contains checks item presence`() {
        // GIVEN
        val bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS)

        // WHEN & THEN
        assertTrue(bodyParts.contains(BodyPart.CHEST))
        assertFalse(bodyParts.contains(BodyPart.BACK))
    }

    @Test
    fun `set conversion preserves unique elements`() {
        // GIVEN
        val list = listOf(BodyPart.CHEST, BodyPart.CHEST, BodyPart.TRICEPS)

        // WHEN
        val set = list.toSet()

        // THEN
        assertEquals(2, set.size)
        assertTrue(set.contains(BodyPart.CHEST))
        assertTrue(set.contains(BodyPart.TRICEPS))
    }

    @Test
    fun `empty list check works correctly`() {
        // GIVEN
        val emptyList = emptyList<BodyPart>()
        val nonEmptyList = listOf(BodyPart.CHEST)

        // WHEN & THEN
        assertTrue(emptyList.isEmpty())
        assertFalse(nonEmptyList.isEmpty())
    }

    @Test
    fun `list size property is correct`() {
        // GIVEN
        val bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)

        // WHEN & THEN
        assertEquals(3, bodyParts.size)
    }
}
