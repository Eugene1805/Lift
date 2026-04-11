package com.eugene.lift.data.local

import com.eugene.lift.domain.usecase.exercise.ExerciseImageResolver
import javax.inject.Inject

class ExerciseImageResolverImpl @Inject constructor() : ExerciseImageResolver {
    override fun resolveDrawable(exerciseName: String): String? {
        return ExerciseImageMapper.getDrawable(exerciseName)
    }
}

