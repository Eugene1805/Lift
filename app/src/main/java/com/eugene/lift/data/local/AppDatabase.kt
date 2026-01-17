package com.eugene.lift.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.dao.FolderDao
import com.eugene.lift.data.local.dao.TemplateDao
import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.data.local.entity.ExerciseBodyPartCrossRef
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.data.local.entity.WorkoutSetEntity
import com.eugene.lift.data.local.entity.SessionExerciseEntity
import com.eugene.lift.data.local.entity.WorkoutSessionEntity
import com.eugene.lift.data.local.entity.TemplateExerciseEntity
import com.eugene.lift.data.local.entity.WorkoutTemplateEntity
import com.eugene.lift.data.local.entity.FolderEntity



@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseBodyPartCrossRef::class,
        WorkoutTemplateEntity::class,
        TemplateExerciseEntity::class,
        WorkoutSessionEntity::class,
        SessionExerciseEntity::class,
        WorkoutSetEntity::class,
        FolderEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun templateDao(): TemplateDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun folderDao(): FolderDao

}