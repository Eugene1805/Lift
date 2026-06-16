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
    fun `getDrawable resolves localized spanish names`() {
        assertEquals("bench_press", ExerciseImageMapper.getDrawable("Press de Banca (Barra)"))
        assertEquals("back_squat", ExerciseImageMapper.getDrawable("Sentadilla Trasera"))
        assertEquals("lat_pulldown", ExerciseImageMapper.getDrawable("Jalón al Pecho (Polea)"))
        assertEquals("incline_bench_press", ExerciseImageMapper.getDrawable("Press de Banca Inclinado (Barra)"))
        assertEquals("weighted_dips", ExerciseImageMapper.getDrawable("Fondos con Peso"))
        assertEquals("wrist_curl", ExerciseImageMapper.getDrawable("Curl de Muñeca (Barra)"))
        assertEquals("adductors", ExerciseImageMapper.getDrawable("Aducción de Cadera (Máquina)"))
        assertEquals("machine_chest_supported_row", ExerciseImageMapper.getDrawable("Remo en Máquina (Sentado)"))
    }

    @Test
    fun `getDrawableForSeedKey resolves locale independent seed keys`() {
        assertEquals("bench_press", ExerciseImageMapper.getDrawableForSeedKey("seed_bench_press"))
        assertEquals("pull_up", ExerciseImageMapper.getDrawableForSeedKey("seed_pullup"))
        assertEquals("hack_squat", ExerciseImageMapper.getDrawableForSeedKey("seed_hack_squat"))
        assertEquals("smith_machine_bulgarian_split_squat", ExerciseImageMapper.getDrawableForSeedKey("seed_smith_machine_bulgarian_split_squat"))
        assertEquals("cable_fly", ExerciseImageMapper.getDrawableForSeedKey("seed_cable_fly"))
        assertEquals("weighted_pull_up", ExerciseImageMapper.getDrawableForSeedKey("seed_weighted_pullup"))
        assertEquals("smith_machine_flat_chest_press", ExerciseImageMapper.getDrawableForSeedKey("seed_smith_machine_bench"))
        assertEquals("cable_pullover_bar", ExerciseImageMapper.getDrawableForSeedKey("seed_cable_pullover_bar"))
        assertEquals("smith_machine_hip_thrust", ExerciseImageMapper.getDrawableForSeedKey("seed_smith_machine_hip_thrust"))
        assertEquals("arnold_press", ExerciseImageMapper.getDrawableForSeedKey("seed_arnold_press"))
        assertEquals("smith_machine_incline_chest_press", ExerciseImageMapper.getDrawableForSeedKey("seed_smith_machine_incline_bench"))
        assertEquals("ab_wheel", ExerciseImageMapper.getDrawableForSeedKey("seed_ab_wheel"))
        assertEquals("dumbell_chest_fly", ExerciseImageMapper.getDrawableForSeedKey("seed_dumbbell_fly"))
        assertEquals("dumbell_front_raise", ExerciseImageMapper.getDrawableForSeedKey("seed_front_raise"))
        assertEquals("sissy_squat", ExerciseImageMapper.getDrawableForSeedKey("seed_sissy_squat"))
    }

    @Test
    fun `getDrawable returns correct drawable for new mapped local assets`() {
        assertEquals("lat_pulldown", ExerciseImageMapper.getDrawable("Lat Pulldown (Cable)"))
        assertEquals("dumbbell_lateral_raise", ExerciseImageMapper.getDrawable("Lateral Raise (Dumbbell)"))
        assertEquals("machine_shoulder_press", ExerciseImageMapper.getDrawable("Machine Shoulder Press"))
        assertEquals("chest_press_machine", ExerciseImageMapper.getDrawable("Chest Press (Machine)"))
        assertEquals("standing_overhead_shoulder_press", ExerciseImageMapper.getDrawable("Overhead Press (Barbell)"))
        assertEquals("dumbbell_shoulder_press", ExerciseImageMapper.getDrawable("Dumbbell Shoulder Press"))
        assertEquals("reverse_fly", ExerciseImageMapper.getDrawable("Reverse Pec Deck (Machine)"))
        assertEquals("machine_pullover", ExerciseImageMapper.getDrawable("Machine Pullover"))
        assertEquals("cable_pullover_bar", ExerciseImageMapper.getDrawable("Cable Pullover with Bar"))
        assertEquals("smith_machine_hip_thrust", ExerciseImageMapper.getDrawable("Smith Machine Hip Thrust"))
        assertEquals("smith_machine_romanian_deadlift", ExerciseImageMapper.getDrawable("Smith Machine Romanian Deadlift"))
        assertEquals("arnold_press", ExerciseImageMapper.getDrawable("Arnold Press (Dumbbell)"))
        assertEquals("ab_wheel", ExerciseImageMapper.getDrawable("Ab Wheel Rollout"))
        assertEquals("dumbell_chest_fly", ExerciseImageMapper.getDrawable("Dumbbell Fly"))
        assertEquals("dumbell_front_raise", ExerciseImageMapper.getDrawable("Front Raise (Dumbbell)"))
        assertEquals("sissy_squat", ExerciseImageMapper.getDrawable("Sissy Squat"))
        assertEquals("upright_row", ExerciseImageMapper.getDrawable("Upright Row (Barbell)"))
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
