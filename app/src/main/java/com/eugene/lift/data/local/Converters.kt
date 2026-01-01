package com.eugene.lift.data.local

import androidx.room.TypeConverter
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType

class Converters {

    @TypeConverter
    fun fromBodyPart(value: BodyPart): String = value.name

    @TypeConverter
    fun toBodyPart(value: String): BodyPart = try {
        BodyPart.valueOf(value)
    } catch (e: IllegalArgumentException) {
        BodyPart.OTHER
    }

    @TypeConverter
    fun fromCategory(value: ExerciseCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): ExerciseCategory = try {
        ExerciseCategory.valueOf(value)
    } catch (e: IllegalArgumentException) {
        ExerciseCategory.REPS_ONLY
    }

    @TypeConverter
    fun fromMeasureType(value: MeasureType): String = value.name

    @TypeConverter
    fun toMeasureType(value: String): MeasureType = try {
        MeasureType.valueOf(value)
    } catch (e: IllegalArgumentException) {
        MeasureType.REPS_AND_WEIGHT
    }
}