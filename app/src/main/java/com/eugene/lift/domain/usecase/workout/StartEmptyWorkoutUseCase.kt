package com.eugene.lift.domain.usecase.workout

import android.content.Context
import com.eugene.lift.R
import com.eugene.lift.domain.model.WorkoutSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class StartEmptyWorkoutUseCase @Inject constructor(
    @get:ApplicationContext private val context: Context
) {
    operator fun invoke(): WorkoutSession {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val dateTimeString = now.format(formatter)
        val baseName = context.getString(R.string.quick_start_title)

        return WorkoutSession(
            id = UUID.randomUUID().toString(),
            templateId = null,
            name = "$baseName - $dateTimeString",
            date = now,
            durationSeconds = 0,
            exercises = emptyList()
        )
    }
}