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
        "seed_lat_pulldown" to "lat_pulldown",
        "seed_leg_press" to "leg_press",
        "seed_dumbbell_row" to "dumbbell_row",
        "seed_lateral_raise" to "dumbbell_lateral_raise",
        "seed_romanian_deadlift" to "romanian_deadlift",
        "seed_front_squat" to "front_squat",
        "seed_dips" to "dips",
        "seed_face_pull" to "face_pull",
        "seed_leg_extension" to "leg_extension",
        "seed_hip_thrust" to "hip_thrust",
        "seed_incline_bench" to "incline_bench_press",
        "seed_calf_raise" to "machine_standing_calf_raises",
        "seed_hammer_curl" to "hammer_curl",
        "seed_bulgarian_split_squat" to "dumbell_bulgarian_split_squat",
        "seed_chest_supported_row" to "chest_supported_row",
        "seed_cable_lateral_raise" to "cable_lateral_raise",
        "seed_pec_deck" to "chest_peck_fly",
        "seed_seated_cable_row" to "cable_row",
        "seed_machine_shoulder_press" to "machine_shoulder_press",
        "seed_preacher_curl" to "machine_preacher_curl",
        "seed_weighted_dip" to "weigthed_dips",
        "seed_goblet_squat" to "goblet_squat",
        "seed_cable_row" to "cable_row",
        "seed_t_bar_row" to "t_bar_row",
        "seed_hip_abduction" to "abductors",
        "seed_incline_dumbbell_press" to "dumbell_incline_chest_press",
        "seed_glute_kickback" to "glute_kickback",
        "seed_wrist_curl" to "wrist_curl",
        "seed_chest_press_machine" to "chest_press_machine",
        "seed_hack_squat" to "hack_squat",
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
        "lat pulldown (cable)"                       to "lat_pulldown",
        "jalon al pecho (polea)"                     to "lat_pulldown",
        "leg press (machine)"                        to "leg_press",
        "prensa de piernas (maquina)"                to "leg_press",
        "dumbbell row"                               to "dumbbell_row",
        "remo con mancuerna"                         to "dumbbell_row",
        "lateral raise (dumbbell)"                   to "dumbbell_lateral_raise",
        "elevacion lateral (mancuernas)"             to "dumbbell_lateral_raise",
        "bulgarian split squat (dumbbell)"           to "dumbell_bulgarian_split_squat",
        "sentadilla bulgara (mancuernas)"            to "dumbell_bulgarian_split_squat",
        "incline dumbbell press"                     to "dumbell_incline_chest_press",
        "press inclinado con mancuernas"             to "dumbell_incline_chest_press",
        "bicep curl (dumbbell)"                      to "dumbell_biceps_curl",
        "curl de biceps (mancuernas)"                to "dumbell_biceps_curl",
        "romanian deadlift (barbell)"                to "romanian_deadlift",
        "peso muerto rumano (barra)"                 to "romanian_deadlift",
        "front squat (barbell)"                      to "front_squat",
        "sentadilla frontal (barra)"                 to "front_squat",
        "dips"                                       to "dips",
        "fondos en paralelas"                        to "dips",
        "face pulls (cable)"                         to "face_pull",
        "jalones a la cara (polea)"                  to "face_pull",
        "wrist curl (barbell)"                       to "wrist_curl",
        "curl de muneca (barra)"                     to "wrist_curl",

        "pull-ups"                                   to "pull_up",
        "dominadas"                                  to "pull_up",
        "weighted dips"                              to "weigthed_dips",
        "fondos con peso"                            to "weigthed_dips",
        "incline bench press (barbell)"              to "incline_bench_press",
        "press de banca inclinado (barra)"           to "incline_bench_press",
        "hammer curl (dumbbell)"                     to "hammer_curl",
        "curl martillo (mancuernas)"                 to "hammer_curl",

        "leg extension (machine)"                    to "leg_extension",
        "extension de piernas (maquina)"             to "leg_extension",
        "chest supported row (machine)"              to "chest_supported_row",
        "remo con apoyo de pecho (maquina)"          to "chest_supported_row",
        "seated cable row (machine)"                 to "cable_row",
        "remo sentado en polea (maquina)"            to "cable_row",
        "machine shoulder press"                     to "machine_shoulder_press",
        "press de hombros en maquina"                to "machine_shoulder_press",
        "preacher curl (machine)"                    to "machine_preacher_curl",
        "curl en banco scott (maquina)"              to "machine_preacher_curl",
        "cable lateral raise"                        to "cable_lateral_raise",
        "elevacion lateral en polea"                 to "cable_lateral_raise",
        "pec deck (machine)"                         to "chest_peck_fly",
        "pec deck (maquina)"                         to "chest_peck_fly",
        "goblet squat (dumbbell)"                    to "goblet_squat",
        "sentadilla caliz (mancuerna)"               to "goblet_squat",
        "cable row (machine)"                        to "cable_row",
        "remo en polea (maquina)"                    to "cable_row",
        "t-bar row"                                  to "t_bar_row",
        "remo en t"                                  to "t_bar_row",
        "standing calf raise (machine)"              to "machine_standing_calf_raises",
        "elevacion de gemelos (maquina)"             to "machine_standing_calf_raises",
        "hip abduction (machine)"                    to "abductors",
        "abduccion de cadera (maquina)"              to "abductors",
        "glute kickback (cable)"                     to "glute_kickback",
        "patada de gluteo (polea)"                   to "glute_kickback",
        "chest press (machine)"                      to "chest_press_machine",
        "press de pecho (maquina)"                   to "chest_press_machine",
        "hack squat (machine)"                       to "hack_squat",
        "sentadilla hack (maquina)"                  to "hack_squat",

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
