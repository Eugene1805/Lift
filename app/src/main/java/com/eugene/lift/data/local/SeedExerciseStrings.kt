package com.eugene.lift.data.local

import android.content.Context
import com.eugene.lift.R
import com.eugene.lift.domain.model.Exercise
import java.text.Normalizer

object SeedExerciseStrings {

    @Volatile
    private var cachedLocaleTag: String? = null

    @Volatile
    private var cachedNameToSeedKey: Map<String, String> = emptyMap()

    fun attachSeedKeys(context: Context, exercises: List<Exercise>): List<Exercise> {
        return exercises.map { exercise ->
            exercise.copy(seedKey = findSeedKeyByName(context, exercise.name))
        }
    }

    fun findSeedKeyByName(context: Context, exerciseName: String): String? {
        ensureNameCache(context)
        return cachedNameToSeedKey[normalizeForLookup(exerciseName)]
    }

    fun normalizeForLookup(value: String): String = normalize(value)

    fun localize(context: Context, exercise: Exercise): Exercise {
        val seedKey = exercise.seedKey ?: return exercise
        return exercise.copy(
            name = resolveString(context, seedKey) ?: exercise.name,
            instructions = resolveString(context, "${seedKey}_desc") ?: exercise.instructions
        )
    }

    private fun ensureNameCache(context: Context) {
        val localeTag = context.resources.configuration.locales[0]?.toLanguageTag().orEmpty()
        if (cachedLocaleTag == localeTag) return

        synchronized(this) {
            if (cachedLocaleTag == localeTag) return
            cachedNameToSeedKey = buildNameToSeedKeyMap(context)
            cachedLocaleTag = localeTag
        }
    }

    private fun buildNameToSeedKeyMap(context: Context): Map<String, String> {
        val nameToSeedKey = linkedMapOf<String, String>()
        for (resId in SEED_NAME_RESOURCE_IDS) {
            val seedKey = context.resources.getResourceEntryName(resId)
            val resolvedName = context.getString(resId)
            val previousSeedKey = nameToSeedKey.put(
                normalizeForLookup(resolvedName),
                seedKey
            )
            check(previousSeedKey == null || previousSeedKey == seedKey) {
                "Duplicate localized seed exercise name: $resolvedName"
            }
        }
        return nameToSeedKey
    }

    private fun resolveString(context: Context, entryName: String): String? {
        val resId = context.resources.getIdentifier(entryName, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else null
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value.trim().lowercase(), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
    }

    // Direct references make seed-key discovery independent from R8 field reflection.
    private val SEED_NAME_RESOURCE_IDS = intArrayOf(
        R.string.seed_ab_wheel,
        R.string.seed_arnold_press,
        R.string.seed_back_extension,
        R.string.seed_barbell_row,
        R.string.seed_barbell_shrug,
        R.string.seed_bench_press,
        R.string.seed_bicep_curl,
        R.string.seed_bicycle_crunch,
        R.string.seed_box_jump,
        R.string.seed_bulgarian_split_squat,
        R.string.seed_burpee,
        R.string.seed_cable_crossover,
        R.string.seed_cable_crunch,
        R.string.seed_cable_curl,
        R.string.seed_cable_fly,
        R.string.seed_cable_lateral_raise,
        R.string.seed_cable_oblique_twist,
        R.string.seed_cable_overhead_triceps_extension,
        R.string.seed_cable_overhead_triceps_extension_cuff,
        R.string.seed_cable_overhead_triceps_extension_unilateral,
        R.string.seed_cable_pullover_bar,
        R.string.seed_cable_pullover_rope,
        R.string.seed_cable_rear_delt_fly,
        R.string.seed_cable_row,
        R.string.seed_cable_tricep_pushdown,
        R.string.seed_cable_woodchopper,
        R.string.seed_calf_raise,
        R.string.seed_chest_press_machine,
        R.string.seed_chest_supported_row,
        R.string.seed_chin_up,
        R.string.seed_close_grip_bench,
        R.string.seed_close_grip_lat_pulldown,
        R.string.seed_concentration_curl,
        R.string.seed_deadlift,
        R.string.seed_decline_bench,
        R.string.seed_decline_cable_fly,
        R.string.seed_decline_chest_press_machine,
        R.string.seed_decline_dumbbell_press,
        R.string.seed_decline_sit_up,
        R.string.seed_dips,
        R.string.seed_dragon_flag,
        R.string.seed_dumbbell_fly,
        R.string.seed_dumbbell_overhead_press,
        R.string.seed_dumbbell_press,
        R.string.seed_dumbbell_pullover,
        R.string.seed_dumbbell_row,
        R.string.seed_dumbbell_shoulder_press,
        R.string.seed_elliptical,
        R.string.seed_ez_bar_curl,
        R.string.seed_face_pull,
        R.string.seed_farmers_walk,
        R.string.seed_front_lat_pulldown,
        R.string.seed_front_lever,
        R.string.seed_front_raise,
        R.string.seed_front_squat,
        R.string.seed_glute_bridge,
        R.string.seed_glute_kickback,
        R.string.seed_goblet_squat,
        R.string.seed_good_morning,
        R.string.seed_hack_squat,
        R.string.seed_hammer_curl,
        R.string.seed_hanging_leg_raise,
        R.string.seed_high_cable_fly,
        R.string.seed_high_foot_leg_press,
        R.string.seed_hip_abduction,
        R.string.seed_hip_adduction,
        R.string.seed_hip_thrust,
        R.string.seed_incline_bench,
        R.string.seed_incline_cable_fly,
        R.string.seed_incline_chest_press_machine,
        R.string.seed_incline_curl,
        R.string.seed_incline_dumbbell_press,
        R.string.seed_jump_squat,
        R.string.seed_landmine_press,
        R.string.seed_lat_pulldown,
        R.string.seed_lateral_raise,
        R.string.seed_leg_curl,
        R.string.seed_leg_extension,
        R.string.seed_leg_press,
        R.string.seed_low_cable_fly,
        R.string.seed_lunges,
        R.string.seed_machine_fly,
        R.string.seed_machine_lateral_raise,
        R.string.seed_machine_overhead_triceps_extension,
        R.string.seed_machine_pullover,
        R.string.seed_machine_row,
        R.string.seed_machine_shoulder_press,
        R.string.seed_mountain_climber,
        R.string.seed_muscle_up,
        R.string.seed_narrow_leg_press,
        R.string.seed_neck_curl,
        R.string.seed_neck_extension,
        R.string.seed_neutral_grip_pullup,
        R.string.seed_overhead_press,
        R.string.seed_paused_squat,
        R.string.seed_pec_deck,
        R.string.seed_pendulum_squat,
        R.string.seed_pistol_squat,
        R.string.seed_plank,
        R.string.seed_power_clean,
        R.string.seed_preacher_curl,
        R.string.seed_pullup,
        R.string.seed_pushup,
        R.string.seed_rack_pull,
        R.string.seed_reverse_curl,
        R.string.seed_reverse_fly,
        R.string.seed_reverse_grip_tricep_pushdown,
        R.string.seed_reverse_pec_deck,
        R.string.seed_reverse_wrist_curl,
        R.string.seed_romanian_deadlift,
        R.string.seed_rope_face_pull,
        R.string.seed_rope_tricep_extension,
        R.string.seed_rowing_machine,
        R.string.seed_russian_twist,
        R.string.seed_seal_row,
        R.string.seed_seated_cable_row,
        R.string.seed_seated_calf_raise,
        R.string.seed_seated_leg_curl,
        R.string.seed_shrugs,
        R.string.seed_side_plank,
        R.string.seed_single_arm_overhead_press,
        R.string.seed_single_arm_row,
        R.string.seed_single_arm_triceps_extension,
        R.string.seed_single_leg_curl,
        R.string.seed_single_leg_deadlift,
        R.string.seed_single_leg_press,
        R.string.seed_sissy_squat,
        R.string.seed_sit_up,
        R.string.seed_skull_crusher,
        R.string.seed_smith_machine_bench,
        R.string.seed_smith_machine_bulgarian_split_squat,
        R.string.seed_smith_machine_decline_bench,
        R.string.seed_smith_machine_hip_thrust,
        R.string.seed_smith_machine_incline_bench,
        R.string.seed_smith_machine_romanian_deadlift,
        R.string.seed_smith_machine_row,
        R.string.seed_smith_machine_shoulder_press,
        R.string.seed_smith_machine_squat,
        R.string.seed_squat,
        R.string.seed_stair_climber,
        R.string.seed_standing_calf_raise,
        R.string.seed_stationary_bike,
        R.string.seed_step_up,
        R.string.seed_straight_arm_pulldown,
        R.string.seed_sumo_deadlift,
        R.string.seed_t_bar_row,
        R.string.seed_treadmill,
        R.string.seed_tricep_extension,
        R.string.seed_tricep_kickback,
        R.string.seed_underhand_row,
        R.string.seed_upright_row,
        R.string.seed_weighted_crunch,
        R.string.seed_weighted_dip,
        R.string.seed_weighted_pullup,
        R.string.seed_wide_grip_lat_pulldown,
        R.string.seed_wide_grip_row,
        R.string.seed_wide_leg_press,
        R.string.seed_wrist_curl,
        R.string.seed_zottman_curl,
    )
}
