package com.eugene.lift.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.entity.ExerciseBodyPartCrossRef
import com.eugene.lift.data.local.entity.ExerciseEntity

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseBodyPartCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}