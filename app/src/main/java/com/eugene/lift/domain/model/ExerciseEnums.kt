package com.eugene.lift.domain.model

import androidx.annotation.StringRes
import com.eugene.lift.R

enum class BodyPart(@get:StringRes val labelRes: Int) {
    CHEST(R.string.part_chest),
    BACK(R.string.part_back),
    LEGS(R.string.part_legs),
    SHOULDERS(R.string.part_shoulders),
    ARMS(R.string.part_arms),
    CORE(R.string.part_core),
    CARDIO(R.string.part_cardio),
    OTHER(R.string.part_other)
}

enum class ExerciseCategory(@get:StringRes val labelRes: Int) {
    BARBELL(R.string.cat_barbell),
    DUMBBELL(R.string.cat_dumbbell),
    MACHINE(R.string.cat_machine),
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