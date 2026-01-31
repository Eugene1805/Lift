package com.eugene.lift.data.local

import android.content.Context
import com.eugene.lift.R
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.repository.ExerciseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class ExerciseSeeder @Inject constructor(
    private val repository: ExerciseRepository,
    @get:ApplicationContext private val context: Context
) {
    suspend fun populate() {
        if (repository.getCount() > 0) return

        val exercises = listOf(
            // Barbell Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_bench_press),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_bench_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_squat),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS, BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_deadlift),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_deadlift_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LOWER_BACK, BodyPart.GLUTES, BodyPart.HAMSTRINGS, BodyPart.TRAPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_overhead_press),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_overhead_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FRONT_DELTS, BodyPart.SIDE_DELTS, BodyPart.TRICEPS, BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_barbell_row),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_barbell_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),

            // Dumbbell Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_dumbbell_press),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_dumbbell_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_dumbbell_shoulder_press),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_dumbbell_shoulder_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FRONT_DELTS, BodyPart.SIDE_DELTS, BodyPart.TRICEPS)
            ),

            // Machine Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_lat_pulldown),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_lat_pulldown_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_fly),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_fly_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_leg_press),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_leg_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),

            // Bodyweight Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_pullup),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_pullup_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_pushup),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_pushup_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS, BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_plank),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.TIME,
                instructions = context.getString(R.string.seed_plank_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE, BodyPart.SHOULDERS)
            ),

            // Additional Dumbbell Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_dumbbell_row),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_dumbbell_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_bicep_curl),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_bicep_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.BICEPS, BodyPart.FOREARMS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_tricep_extension),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_tricep_extension_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_lateral_raise),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_lateral_raise_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.SIDE_DELTS)
            ),

            // Additional Barbell Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_romanian_deadlift),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_romanian_deadlift_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.HAMSTRINGS, BodyPart.GLUTES, BodyPart.LOWER_BACK)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_front_squat),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_front_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.CORE)
            ),

            // Additional Machine Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_leg_curl),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_leg_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.HAMSTRINGS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_leg_extension),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_leg_extension_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_face_pull),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_face_pull_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.REAR_DELTS, BodyPart.TRAPS)
            ),

            // Additional Bodyweight Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_dips),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_dips_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),

            // Glute-Focused
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_hip_thrust),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_hip_thrust_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.GLUTES, BodyPart.HAMSTRINGS, BodyPart.CORE)
            ),

            // Chest Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_incline_bench),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_incline_bench_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS, BodyPart.TRICEPS)
            ),

            // Calves
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_calf_raise),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_calf_raise_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CALVES)
            ),

            // More Triceps
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_skull_crusher),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_skull_crusher_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_tricep_pushdown),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_tricep_pushdown_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            ),

            // More Biceps
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_hammer_curl),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_hammer_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.BICEPS, BodyPart.FOREARMS)
            ),

            // Traps
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_shrugs),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_shrugs_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRAPS, BodyPart.NECK)
            ),

            // Unilateral Leg Work
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_bulgarian_split_squat),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_bulgarian_split_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_lunges),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_lunges_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),

            // Back Support
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_chest_supported_row),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_chest_supported_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.REAR_DELTS, BodyPart.BICEPS)
            ),

            // Core Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_crunch),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_crunch_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_hanging_leg_raise),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_hanging_leg_raise_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE, BodyPart.FOREARMS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_russian_twist),
                category = ExerciseCategory.WEIGHTED_BODYWEIGHT,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_russian_twist_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),

            // Rear Delts
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_reverse_pec_deck),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_reverse_pec_deck_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.REAR_DELTS, BodyPart.TRAPS)
            ),

            // Side Delts
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_lateral_raise),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_lateral_raise_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.SIDE_DELTS)
            ),

            // Additional Machine Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_pec_deck),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_pec_deck_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_seated_cable_row),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_seated_cable_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_machine_shoulder_press),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_machine_shoulder_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FRONT_DELTS, BodyPart.SIDE_DELTS, BodyPart.TRICEPS)
            ),

            // Cardio
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_rowing_machine),
                category = ExerciseCategory.CARDIO,
                measureType = MeasureType.DISTANCE_TIME,
                instructions = context.getString(R.string.seed_rowing_machine_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CARDIO, BodyPart.FULL_BODY)
            ),

            // Classic Missing Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_dumbbell_pullover),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_dumbbell_pullover_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.CHEST)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_preacher_curl),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_preacher_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.BICEPS)
            ),

            // Weighted Bodyweight Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_weighted_pullup),
                category = ExerciseCategory.WEIGHTED_BODYWEIGHT,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_weighted_pullup_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_weighted_dip),
                category = ExerciseCategory.WEIGHTED_BODYWEIGHT,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_weighted_dip_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),

            // Advanced Arm Work
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_concentration_curl),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_concentration_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.BICEPS)
            ),

            // Chest Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_decline_bench),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_decline_bench_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),

            // Advanced Shoulder Work
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_arnold_press),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_arnold_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FRONT_DELTS, BodyPart.SIDE_DELTS, BodyPart.TRICEPS)
            ),

            // Functional Movements
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_goblet_squat),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_goblet_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_farmers_walk),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.DISTANCE_TIME,
                instructions = context.getString(R.string.seed_farmers_walk_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FOREARMS, BodyPart.TRAPS, BodyPart.CORE, BodyPart.FULL_BODY)
            ),

            // Plyometric
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_box_jump),
                category = ExerciseCategory.REPS_ONLY,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_box_jump_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.CALVES, BodyPart.FULL_BODY)
            ),

            // Equipment Variations - Rows
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_row),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.REAR_DELTS, BodyPart.BICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_machine_row),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_machine_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.REAR_DELTS, BodyPart.BICEPS)
            ),

            // Equipment Variations - Chest
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_dumbbell_fly),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_dumbbell_fly_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_incline_dumbbell_press),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_incline_dumbbell_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS, BodyPart.TRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_crossover),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_crossover_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),

            // Equipment Variations - Biceps
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_ez_bar_curl),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_ez_bar_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.BICEPS, BodyPart.FOREARMS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_curl),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.BICEPS, BodyPart.FOREARMS)
            ),

            // Equipment Variations - Shoulders
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_dumbbell_overhead_press),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_dumbbell_overhead_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FRONT_DELTS, BodyPart.SIDE_DELTS, BodyPart.TRICEPS, BodyPart.CORE)
            ),

            // Equipment Variations - Traps
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_barbell_shrug),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_barbell_shrug_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRAPS, BodyPart.NECK)
            ),

            // Front Delts Isolation
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_front_raise),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_front_raise_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FRONT_DELTS)
            ),

            // Back Compound Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_t_bar_row),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_t_bar_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.REAR_DELTS, BodyPart.BICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_back_extension),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_back_extension_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LOWER_BACK, BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),

            // Unilateral Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_single_leg_deadlift),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_single_leg_deadlift_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.HAMSTRINGS, BodyPart.GLUTES, BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_single_arm_row),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_single_arm_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.REAR_DELTS, BodyPart.BICEPS, BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_single_leg_press),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_single_leg_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),

            // Pull-up Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_chin_up),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_chin_up_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),

            // Posterior Chain
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_good_morning),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_good_morning_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.HAMSTRINGS, BodyPart.GLUTES, BodyPart.LOWER_BACK)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_rack_pull),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_rack_pull_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRAPS, BodyPart.LOWER_BACK, BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),

            // Adductors & Abductors
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_hip_adduction),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_hip_adduction_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.ADDUCTORS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_hip_abduction),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_hip_abduction_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.ABDUCTORS, BodyPart.GLUTES)
            ),

            // Neck Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_neck_extension),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_neck_extension_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.NECK)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_neck_curl),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_neck_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.NECK)
            ),

            // Abdominal Exercises - Bodyweight
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_ab_wheel),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_ab_wheel_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_sit_up),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_sit_up_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_decline_sit_up),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_decline_sit_up_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_side_plank),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.TIME,
                instructions = context.getString(R.string.seed_side_plank_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),

            // Abdominal Exercises - Weighted
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_weighted_crunch),
                category = ExerciseCategory.WEIGHTED_BODYWEIGHT,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_weighted_crunch_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),

            // Grip Variations - Back (Lat Pulldown)
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_wide_grip_lat_pulldown),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_wide_grip_lat_pulldown_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_close_grip_lat_pulldown),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_close_grip_lat_pulldown_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_straight_arm_pulldown),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_straight_arm_pulldown_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS)
            ),

            // Tricep Kickbacks
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_tricep_kickback),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_tricep_kickback_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            ),

            // Cable Attachment Variations - Triceps
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_rope_tricep_extension),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_rope_tricep_extension_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_reverse_grip_tricep_pushdown),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_reverse_grip_tricep_pushdown_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            ),

            // Grip Variations - Rows
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_underhand_row),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_underhand_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),

            // Grip Variations - Pull-ups
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_neutral_grip_pullup),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_neutral_grip_pullup_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS)
            ),

            // Cable Attachment Variations - Rear Delts
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_rear_delt_fly),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_rear_delt_fly_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.REAR_DELTS, BodyPart.TRAPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_rope_face_pull),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_rope_face_pull_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.REAR_DELTS, BodyPart.TRAPS)
            ),

            // Deadlift Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_sumo_deadlift),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_sumo_deadlift_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.GLUTES, BodyPart.ADDUCTORS, BodyPart.HAMSTRINGS, BodyPart.LOWER_BACK)
            ),

            // Specialty Press Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_landmine_press),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_landmine_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FRONT_DELTS, BodyPart.TRICEPS, BodyPart.CORE)
            ),

            // Bicep Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_incline_curl),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_incline_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.BICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_zottman_curl),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_zottman_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.BICEPS, BodyPart.FOREARMS)
            ),

            // Chest Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_decline_dumbbell_press),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_decline_dumbbell_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),

            // Core Rotational
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_woodchopper),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_woodchopper_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),

            // Advanced Leg Movements
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_sissy_squat),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_sissy_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_paused_squat),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_paused_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS, BodyPart.CORE)
            ),

            // Back Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_seal_row),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_seal_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),

            // Glute Isolation
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_glute_bridge),
                category = ExerciseCategory.WEIGHTED_BODYWEIGHT,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_glute_bridge_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),

            // Glute Kickbacks (THE ACTUAL GLUTE KICKBACK!)
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_glute_kickback),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_glute_kickback_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.GLUTES)
            ),

            // Cable Fly Angle Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_low_cable_fly),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_low_cable_fly_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_high_cable_fly),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_high_cable_fly_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),

            // Rear Delt Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_reverse_fly),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_reverse_fly_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.REAR_DELTS, BodyPart.TRAPS)
            ),

            // Grip Variations - Bench Press
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_close_grip_bench),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_close_grip_bench_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS, BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),

            // Grip Variations - Rows
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_wide_grip_row),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_wide_grip_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRAPS, BodyPart.REAR_DELTS, BodyPart.LATS)
            ),

            // Calf Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_standing_calf_raise),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_standing_calf_raise_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CALVES)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_seated_calf_raise),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_seated_calf_raise_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CALVES)
            ),

            // Unilateral Shoulder Work
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_single_arm_overhead_press),
                category = ExerciseCategory.DUMBBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_single_arm_overhead_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FRONT_DELTS, BodyPart.SIDE_DELTS, BodyPart.TRICEPS, BodyPart.CORE)
            ),

            // Forearm Isolation
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_wrist_curl),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_wrist_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FOREARMS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_reverse_wrist_curl),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_reverse_wrist_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FOREARMS)
            ),

            // Machine Chest Press
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_chest_press_machine),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_chest_press_machine_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),

            // Trap Focused
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_upright_row),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_upright_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRAPS, BodyPart.SIDE_DELTS)
            ),

            // Oblique Work
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_oblique_twist),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_oblique_twist_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_bicycle_crunch),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_bicycle_crunch_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),

            // Unilateral Leg
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_single_leg_curl),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_single_leg_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.HAMSTRINGS)
            ),

            // Plyometric
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_burpee),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_burpee_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FULL_BODY, BodyPart.CARDIO)
            ),

            // Olympic Lift
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_power_clean),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_power_clean_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FULL_BODY, BodyPart.TRAPS, BodyPart.HAMSTRINGS, BodyPart.GLUTES, BodyPart.QUADRICEPS)
            ),

            // Cardio Machines
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_treadmill),
                category = ExerciseCategory.CARDIO,
                measureType = MeasureType.DISTANCE_TIME,
                instructions = context.getString(R.string.seed_treadmill_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CARDIO, BodyPart.LEGS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_stationary_bike),
                category = ExerciseCategory.CARDIO,
                measureType = MeasureType.DISTANCE_TIME,
                instructions = context.getString(R.string.seed_stationary_bike_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CARDIO, BodyPart.LEGS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_elliptical),
                category = ExerciseCategory.CARDIO,
                measureType = MeasureType.DISTANCE_TIME,
                instructions = context.getString(R.string.seed_elliptical_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CARDIO, BodyPart.FULL_BODY)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_stair_climber),
                category = ExerciseCategory.CARDIO,
                measureType = MeasureType.DISTANCE_TIME,
                instructions = context.getString(R.string.seed_stair_climber_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CARDIO, BodyPart.GLUTES, BodyPart.LEGS)
            ),

            // Pulldown Angle Variation
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_front_lat_pulldown),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_front_lat_pulldown_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),

            // Machine Leg Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_hack_squat),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_hack_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_smith_machine_squat),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_smith_machine_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_seated_leg_curl),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_seated_leg_curl_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.HAMSTRINGS)
            ),

            // Functional/Plyometric
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_mountain_climber),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_mountain_climber_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE, BodyPart.CARDIO, BodyPart.FULL_BODY)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_jump_squat),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_jump_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.CALVES, BodyPart.FULL_BODY)
            ),

            // Smith Machine Exercises
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_smith_machine_bench),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_smith_machine_bench_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_smith_machine_shoulder_press),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_smith_machine_shoulder_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.FRONT_DELTS, BodyPart.SIDE_DELTS, BodyPart.TRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_smith_machine_row),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_smith_machine_row_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.TRAPS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            ),

            // Leg Press Angle Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_narrow_leg_press),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_narrow_leg_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_wide_leg_press),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_wide_leg_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.ADDUCTORS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_high_foot_leg_press),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_high_foot_leg_press_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),

            // Advanced Calisthenics
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_dragon_flag),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_dragon_flag_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_muscle_up),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_muscle_up_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_front_lever),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.TIME,
                instructions = context.getString(R.string.seed_front_lever_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.CORE)
            ),

            // Smith Machine Leg Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_smith_machine_bulgarian_split_squat),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_smith_machine_bulgarian_split_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS)
            ),

            // Advanced Bodyweight Leg
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_pistol_squat),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_pistol_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES)
            ),

            // Smith Machine Chest Angles
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_smith_machine_incline_bench),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_smith_machine_incline_bench_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS, BodyPart.TRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_smith_machine_decline_bench),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_smith_machine_decline_bench_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),

            // Machine Chest Press Angles
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_incline_chest_press_machine),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_incline_chest_press_machine_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS, BodyPart.TRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_decline_chest_press_machine),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_decline_chest_press_machine_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),

            // Cable Fly Angles (Incline/Decline on bench)
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_incline_cable_fly),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_incline_cable_fly_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_decline_cable_fly),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_decline_cable_fly_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),

            // Machine Fly (was Pec Deck, adding specific machine fly)
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_machine_fly),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_machine_fly_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.FRONT_DELTS)
            ),

            // Pullover Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_machine_pullover),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_machine_pullover_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.CHEST)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_pullover_rope),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_pullover_rope_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.CHEST)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_pullover_bar),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_pullover_bar_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.CHEST)
            ),

            // Overhead Triceps Extensions - Cable/Pulley Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_overhead_triceps_extension),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_overhead_triceps_extension_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_overhead_triceps_extension_unilateral),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_overhead_triceps_extension_unilateral_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_cable_overhead_triceps_extension_cuff),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_cable_overhead_triceps_extension_cuff_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            ),

            // Overhead Triceps Extensions - Machine Variations
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_machine_overhead_triceps_extension),
                category = ExerciseCategory.MACHINE,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_machine_overhead_triceps_extension_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.TRICEPS)
            )
        )

        exercises.forEach { repository.saveExercise(it) }
    }
}
