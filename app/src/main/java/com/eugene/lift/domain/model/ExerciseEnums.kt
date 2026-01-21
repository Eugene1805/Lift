package com.eugene.lift.domain.model

import androidx.annotation.StringRes
import com.eugene.lift.R

enum class BodyPart(@get:StringRes val labelRes: Int) {
    // Upper Body - Chest & Back
    CHEST(R.string.part_chest),
    BACK(R.string.part_back),
    LATS(R.string.part_lats),
    TRAPS(R.string.part_traps),
    LOWER_BACK(R.string.part_lower_back),

    // Upper Body - Shoulders
    SHOULDERS(R.string.part_shoulders),
    FRONT_DELTS(R.string.part_front_delts),
    SIDE_DELTS(R.string.part_side_delts),
    REAR_DELTS(R.string.part_rear_delts),

    // Upper Body - Arms
    ARMS(R.string.part_arms),
    BICEPS(R.string.part_biceps),
    TRICEPS(R.string.part_triceps),
    FOREARMS(R.string.part_forearms),

    // Lower Body
    LEGS(R.string.part_legs),
    QUADRICEPS(R.string.part_quadriceps),
    HAMSTRINGS(R.string.part_hamstrings),
    GLUTES(R.string.part_glutes),
    CALVES(R.string.part_calves),
    ADDUCTORS(R.string.part_adductors),
    ABDUCTORS(R.string.part_abductors),

    // Core & Other
    CORE(R.string.part_core),
    NECK(R.string.part_neck),
    FULL_BODY(R.string.part_full_body),
    CARDIO(R.string.part_cardio),
    OTHER(R.string.part_other)
}

enum class ExerciseCategory(@get:StringRes val labelRes: Int) {
    BARBELL(R.string.cat_barbell),
    DUMBBELL(R.string.cat_dumbbell),
    MACHINE(R.string.cat_machine),
    BODYWEIGHT(R.string.cat_bodyweight),
    WEIGHTED_BODYWEIGHT(R.string.cat_weighted_bw),
    ASSISTED_BODYWEIGHT(R.string.cat_assisted_bw),
    REPS_ONLY(R.string.cat_reps_only),
    CARDIO(R.string.cat_cardio),
    DURATION(R.string.cat_duration)
}

enum class MeasureType(@get:StringRes val labelRes: Int) {
    REPS_AND_WEIGHT(R.string.type_reps_weight),
    REPS_ONLY(R.string.type_reps_only),
    TIME(R.string.type_time),
    DISTANCE_TIME(R.string.type_distance)
}