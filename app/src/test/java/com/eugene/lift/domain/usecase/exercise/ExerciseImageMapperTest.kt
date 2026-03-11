package com.eugene.lift.domain.usecase.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import com.eugene.lift.data.local.ExerciseImageMapper

class ExerciseImageMapperTest {

    @Test
    fun `getDrawable returns correct drawable for bench press`() {
        val result = ExerciseImageMapper.getDrawable("Bench Press (Barbell)")
        assertEquals("bench_press", result)
    }

    @Test
    fun `getDrawable returns correct drawable for deadlift`() {
        val result = ExerciseImageMapper.getDrawable("Deadlift (Barbell)")
        assertEquals("deadlift", result)
    }

    @Test
    fun `getDrawable returns correct drawable for pull-ups`() {
        val result = ExerciseImageMapper.getDrawable("Pull-ups")
        assertEquals("pull_up", result)
    }

    @Test
    fun `getDrawable returns correct drawable for hip thrust`() {
        val result = ExerciseImageMapper.getDrawable("Hip Thrust (Barbell)")
        assertEquals("hip_thrust", result)
    }

    @Test
    fun `getDrawable returns correct drawable for leg extension`() {
        val result = ExerciseImageMapper.getDrawable("Leg Extension (Machine)")
        assertEquals("leg_extension", result)
    }

    @Test
    fun `getDrawable returns correct drawable for cable lateral raise`() {
        val result = ExerciseImageMapper.getDrawable("Cable Lateral Raise")
        assertEquals("cable_lateral_raise", result)
    }

    @Test
    fun `getDrawable is case-insensitive`() {
        val lower = ExerciseImageMapper.getDrawable("bench press (barbell)")
        val upper = ExerciseImageMapper.getDrawable("BENCH PRESS (BARBELL)")
        val mixed = ExerciseImageMapper.getDrawable("Bench Press (Barbell)")
        assertEquals("bench_press", lower)
        assertEquals("bench_press", upper)
        assertEquals("bench_press", mixed)
    }

    @Test
    fun `getDrawable trims whitespace`() {
        val result = ExerciseImageMapper.getDrawable("  Barbell Row  ")
        assertEquals("barbell_row", result)
    }

    @Test
    fun `getDrawable returns null for unknown exercise`() {
        val result = ExerciseImageMapper.getDrawable("Totally Unknown Exercise XYZ")
        assertNull(result)
    }

    @Test
    fun `getDrawable returns null for empty string`() {
        val result = ExerciseImageMapper.getDrawable("")
        assertNull(result)
    }
}
