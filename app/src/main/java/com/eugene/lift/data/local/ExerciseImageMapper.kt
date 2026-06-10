package com.eugene.lift.data.local

import java.text.Normalizer

/**
 * Centralizes the association between exercise naming conventions and visual assets.
 *
 * This exists as a decoupling layer so that the database and localized strings can remain
 * agnostic of the specific drawable resource names. By using English names as keys, we
 * ensure a stable identifier that persists even when the user changes the app's display
 * language.
 *
 * Case-insensitivity and trimming are enforced to handle potential discrepancies in
 * manual data entry or slight name variations in future seed data.
 */
object ExerciseImageMapper {

    private val seedKeyToDrawable: Map<String, String> = mapOf(
        "seed_bench_press" to "bench_press",
        "seed_squat" to "back_squat",
        "seed_deadlift" to "deadlift",
        "seed_overhead_press" to "overhead_shoulder_press",
        "seed_barbell_row" to "barbell_row",
        "seed_dumbbell_shoulder_press" to "dumbell_shoulder_press",
        "seed_pullup" to "pull_up",
        "seed_bicep_curl" to "dumbell_biceps_curl",
        "seed_leg_extension" to "leg_extension",
        "seed_hip_thrust" to "hip_thrust",
        "seed_calf_raise" to "machine_standing_calf_raises",
        "seed_bulgarian_split_squat" to "dumbell_bulgarian_split_squat",
        "seed_cable_lateral_raise" to "cable_lateral_raise",
        "seed_pec_deck" to "chest_peck_fly",
        "seed_preacher_curl" to "machine_preacher_curl",
        "seed_weighted_dip" to "weigthed_dips",
        "seed_hip_abduction" to "abductors",
        "seed_incline_dumbbell_press" to "dumbell_incline_chest_press",
        "seed_wrist_curl" to "wrist_curl",
        "seed_smith_machine_bulgarian_split_squat" to "smith_machine_bulgarian_split_squat"
    )

    private val nameToDrawable: Map<String, String> = mapOf(
        // Grouped by equipment to facilitate bulk asset updates or style consistency checks
        "bench press (barbell)"                      to "bench_press",
        "press de banca (barra)"                     to "bench_press",
        "back squat"                                 to "back_squat",
        "sentadilla trasera"                         to "back_squat",
        "deadlift (barbell)"                         to "deadlift",
        "peso muerto (barra)"                        to "deadlift",
        "overhead press (barbell)"                   to "overhead_shoulder_press",
        "press militar (barra)"                      to "overhead_shoulder_press",
        "barbell row"                                to "barbell_row",
        "remo con barra"                             to "barbell_row",
        "hip thrust (barbell)"                       to "hip_thrust",
        "empuje de cadera (barra)"                   to "hip_thrust",

        "dumbbell shoulder press"                    to "dumbell_shoulder_press",
        "press de hombros con mancuernas"            to "dumbell_shoulder_press",
        "bulgarian split squat (dumbbell)"           to "dumbell_bulgarian_split_squat",
        "sentadilla bulgara (mancuernas)"            to "dumbell_bulgarian_split_squat",
        "incline dumbbell press"                     to "dumbell_incline_chest_press",
        "press inclinado con mancuernas"             to "dumbell_incline_chest_press",
        "bicep curl (dumbbell)"                      to "dumbell_biceps_curl",
        "curl de biceps (mancuernas)"                to "dumbell_biceps_curl",
        "wrist curl (barbell)"                       to "wrist_curl",
        "curl de muneca (barra)"                     to "wrist_curl",

        "pull-ups"                                   to "pull_up",
        "dominadas"                                  to "pull_up",
        "weighted dips"                              to "weigthed_dips",
        "fondos con peso"                            to "weigthed_dips",

        "leg extension (machine)"                    to "leg_extension",
        "extension de piernas (maquina)"             to "leg_extension",
        "preacher curl (machine)"                    to "machine_preacher_curl",
        "curl en banco scott (maquina)"              to "machine_preacher_curl",
        "cable lateral raise"                        to "cable_lateral_raise",
        "elevacion lateral en polea"                 to "cable_lateral_raise",
        "pec deck (machine)"                         to "chest_peck_fly",
        "pec deck (maquina)"                         to "chest_peck_fly",
        "standing calf raise (machine)"              to "machine_standing_calf_raises",
        "elevacion de gemelos (maquina)"             to "machine_standing_calf_raises",
        "hip abduction (machine)"                    to "abductors",
        "abduccion de cadera (maquina)"              to "abductors",

        "bulgarian split squat (smith machine)"      to "smith_machine_bulgarian_split_squat",
        "sentadilla bulgara en multipower"           to "smith_machine_bulgarian_split_squat",

        // Pre-emptive mapping for exercises not yet in the default seed data but for
        // which assets already exist in the drawable folder.
        "single arm triceps extension"               to "single_arm_triceps_extension",
    )

    /**
     * Resolves a human-readable exercise name to its corresponding technical asset identifier.
     *
     * @param exerciseName The canonical English name used as a key.
     * @return The drawable resource name (WebP) or null if no visual is available for this entry.
     */
    fun getDrawable(exerciseName: String): String? {
        return nameToDrawable[normalizeKey(exerciseName)]
    }

    fun getDrawableForSeedKey(seedResourceEntryName: String): String? {
        return seedKeyToDrawable[seedResourceEntryName]
    }

    private fun normalizeKey(value: String): String {
        return Normalizer.normalize(value.trim().lowercase(), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
    }
}
