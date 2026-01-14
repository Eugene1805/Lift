package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.WorkoutRepository
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class FinishWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(activeSession: WorkoutSession) {
        // 1. Calcular duración real
        val endTime = LocalDateTime.now()
        // Asumiendo que activeSession.date es la fecha de inicio
        val duration = Duration.between(activeSession.date, endTime).seconds

        // 2. Limpieza de datos
        // Filtramos ejercicios que no tengan sets completados, o sets vacíos
        val validExercises = activeSession.exercises.map { exercise ->
            // Opcional: Filtrar sets no completados si esa es tu regla de negocio
            // exercise.copy(sets = exercise.sets.filter { it.completed })
            exercise
        }.filter {
            // Eliminar ejercicios sin sets si se desea
            it.sets.isNotEmpty()
        }

        if (validExercises.isEmpty()) {
            throw IllegalStateException("No se puede guardar un entrenamiento vacío")
        }

        val finalSession = activeSession.copy(
            durationSeconds = duration,
            exercises = validExercises
        )

        // 3. Guardar
        repository.saveSession(finalSession)
    }
}