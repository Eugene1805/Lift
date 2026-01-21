package com.eugene.lift.data.local

import android.content.Context
import com.eugene.lift.R
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.repository.ExerciseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class ExerciseSeeder @Inject constructor(
    private val repository: ExerciseRepository,
    @get:ApplicationContext private val context: Context
) {
    suspend fun populate() {
        if (repository.getCount() > 0) return

        val exercises = listOf(
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_bench_press),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_bench_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_squat),
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = context.getString(R.string.seed_squat_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS, BodyPart.CORE)
            ),
            Exercise(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.seed_pullup),
                category = ExerciseCategory.BODYWEIGHT,
                measureType = MeasureType.REPS_ONLY,
                instructions = context.getString(R.string.seed_pullup_desc),
                imagePath = null,
                bodyParts = listOf(BodyPart.LATS, BodyPart.BICEPS, BodyPart.REAR_DELTS)
            )
        )

        exercises.forEach { repository.saveExercise(it) }
    }
}
