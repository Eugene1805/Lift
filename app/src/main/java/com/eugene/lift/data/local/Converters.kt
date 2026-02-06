package com.eugene.lift.data.local

import androidx.room.TypeConverter
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromBodyPart(value: BodyPart): String = value.name

    @TypeConverter
    fun toBodyPart(value: String): BodyPart = try {
        BodyPart.valueOf(value)
    } catch (_: IllegalArgumentException) {
        BodyPart.OTHER
    }

    @TypeConverter
    fun fromCategory(value: ExerciseCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): ExerciseCategory = try {
        ExerciseCategory.valueOf(value)
    } catch (_: IllegalArgumentException) {
        ExerciseCategory.REPS_ONLY
    }

    @TypeConverter
    fun fromMeasureType(value: MeasureType): String = value.name

    @TypeConverter
    fun toMeasureType(value: String): MeasureType = try {
        MeasureType.valueOf(value)
    } catch (_: IllegalArgumentException) {
        MeasureType.REPS_AND_WEIGHT
    }
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
}