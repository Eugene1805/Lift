package com.eugene.lift.data.local

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

    private val nameToDrawable: Map<String, String> = mapOf(
        // Grouped by equipment to facilitate bulk asset updates or style consistency checks
        "bench press (barbell)"                      to "bench_press",
        "back squat"                                 to "back_squat",
        "deadlift (barbell)"                         to "deadlift",
        "overhead press (barbell)"                   to "overhead_shoulder_press",
        "barbell row"                                to "barbell_row",
        "hip thrust (barbell)"                       to "hip_thrust",

        "dumbbell shoulder press"                    to "dumbell_shoulder_press",
        "bulgarian split squat (dumbbell)"           to "dumbell_bulgarian_split_squat",
        "incline dumbbell press"                     to "dumbell_incline_chest_press",
        "bicep curl (dumbbell)"                      to "dumbell_biceps_curl",

        "pull-ups"                                   to "pull_up",
        "weighted dips"                              to "weigthed_dips",

        "leg extension (machine)"                    to "leg_extension",
        "preacher curl (machine)"                    to "machine_preacher_curl",
        "cable lateral raise"                        to "cable_lateral_raise",
        "pec deck (machine)"                         to "chest_peck_fly",
        "standing calf raise (machine)"              to "machine_standing_calf_raises",
        "hip abduction (machine)"                    to "abductors",

        "bulgarian split squat (smith machine)"      to "smith_machine_bulgarian_split_squat",

        "wrist_curl (barbell)"                       to "wrist_curl",

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
        return nameToDrawable[exerciseName.trim().lowercase()]
    }
}

