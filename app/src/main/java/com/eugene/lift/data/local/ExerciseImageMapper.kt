package com.eugene.lift.data.local

/**
 * Maps seeded exercise names (English, as stored in the DB) to their
 * corresponding drawable resource names (without extension).
 *
 * To add support for a new drawable in the future:
 *  1. Add the WebP file to res/drawable/
 *  2. Add a new entry here: "Exact Exercise Name" to "drawable_file_name"
 *
 * Matching is case-insensitive (trimmed) to be resilient against minor
 * whitespace differences.
 */
object ExerciseImageMapper {

    private val nameToDrawable: Map<String, String> = mapOf(
        // Barbell exercises
        "bench press (barbell)"                      to "bench_press",
        "back squat"                                 to "back_squat",
        "deadlift (barbell)"                         to "deadlift",
        "overhead press (barbell)"                   to "overhead_shoulder_press",
        "barbell row"                                to "barbell_row",
        "hip thrust (barbell)"                       to "hip_thrust",

        // Dumbbell exercises
        "dumbbell shoulder press"                    to "dumbell_shoulder_press",
        "bulgarian split squat (dumbbell)"           to "dumbell_bulgarian_split_squat",
        "incline dumbbell press"                     to "dumbell_incline_chest_press",
        "bicep curl (dumbbell)"                      to "dumbell_biceps_curl",

        // Bodyweight / Weighted-bodyweight
        "pull-ups"                                   to "pull_up",
        "weighted dips"                              to "weigthed_dips",

        // Machine exercises
        "leg extension (machine)"                    to "leg_extension",
        "preacher curl (machine)"                    to "machine_preacher_curl",
        "cable lateral raise"                        to "cable_lateral_raise",
        "pec deck (machine)"                         to "chest_peck_fly",
        "standing calf raise (machine)"              to "machine_standing_calf_raises",
        "hip abduction (machine)"                    to "abductors",

        // Smith machine
        "bulgarian split squat (smith machine)"      to "smith_machine_bulgarian_split_squat",

        // Forearms
        "wrist curl (barbell)"                       to "wrist_curl",

        // Tricep single-arm (no seed exercise with this exact name currently,
        // adding for future use when the exercise is seeded)
        "single arm triceps extension"               to "single_arm_triceps_extension",
    )

    /**
     * Returns the drawable resource name for the given exercise name, or null
     * if no mapping exists.
     *
     * @param exerciseName The name of the exercise as stored in the database.
     */
    fun getDrawable(exerciseName: String): String? {
        return nameToDrawable[exerciseName.trim().lowercase()]
    }
}
