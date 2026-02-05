package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.UserProfileRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class FinishWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(activeSession: WorkoutSession) {
        // 1. Calcular duración real
        val endTime = LocalDateTime.now()
        val duration = Duration.between(activeSession.date, endTime).seconds

        // 2. Procesar ejercicios: Limpieza y CÁLCULO DE PRs
        val processedExercises = activeSession.exercises.map { sessionExercise ->

            // A. Filtramos solo los sets completados para el cálculo
            val completedSets = sessionExercise.sets.filter { it.completed }

            if (completedSets.isNotEmpty()) {
                // B. Buscamos el peso máximo levantado HOY
                val sessionBestWeight = completedSets.maxOf { it.weight }

                // C. Buscamos el récord ANTERIOR en la base de datos
                val previousRecord = repository.getPersonalRecord(sessionExercise.exercise.id).first()
                val previousRecordWeight = previousRecord?.weight ?: 0.0

                // D. Comparamos
                if (sessionBestWeight > previousRecordWeight) {
                    // ¡NUEVO RÉCORD! Marcamos los sets que lograron ese peso
                    val updatedSets = sessionExercise.sets.map { set ->
                        if (set.completed && set.weight == sessionBestWeight) {
                            set.copy(isPr = true)
                        } else {
                            set
                        }
                    }
                    sessionExercise.copy(sets = updatedSets)
                } else {
                    sessionExercise
                }
            } else {
                sessionExercise
            }
        }.filter {
            it.sets.isNotEmpty()
        }

        // 3. Validación final
        if (processedExercises.isEmpty()) {
            throw IllegalStateException("No se puede guardar un entrenamiento vacío")
        }

        // 4. Crear la sesión final con los datos procesados (PRs marcados)
        val finalSession = activeSession.copy(
            durationSeconds = duration,
            exercises = processedExercises
        )

        // 5. Guardar en DB
        repository.saveSession(finalSession)

        // 6. Record stats to user profile
        val totalVolume = finalSession.exercises
            .flatMap { it.sets }
            .filter { it.completed }
            .sumOf { it.weight * it.reps }

        val totalPRs = finalSession.exercises
            .flatMap { it.sets }
            .count { it.isPr }

        val profile = userProfileRepository.getCurrentProfileOnce()
        if (profile != null) {
            userProfileRepository.recordWorkoutCompleted(
                id = profile.id,
                volume = totalVolume,
                duration = duration,
                prCount = totalPRs
            )
        }
    }
}