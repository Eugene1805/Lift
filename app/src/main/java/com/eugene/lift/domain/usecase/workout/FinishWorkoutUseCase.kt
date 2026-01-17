package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class FinishWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
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
                // Usamos .first() porque el repositorio devuelve un Flow
                val previousRecord = repository.getPersonalRecord(sessionExercise.exercise.id).first()
                val previousRecordWeight = previousRecord?.weight ?: 0.0

                // D. Comparamos
                if (sessionBestWeight > previousRecordWeight) {
                    // ¡NUEVO RÉCORD! Marcamos los sets que lograron ese peso
                    val updatedSets = sessionExercise.sets.map { set ->
                        // Marcamos como PR si está completado y tiene el peso récord
                        if (set.completed && set.weight == sessionBestWeight) {
                            set.copy(isPr = true)
                        } else {
                            set
                        }
                    }
                    sessionExercise.copy(sets = updatedSets)
                } else {
                    // No hubo récord, devolvemos el ejercicio tal cual
                    sessionExercise
                }
            } else {
                // Si no hay sets completados, no hay PRs posibles
                sessionExercise
            }
        }.filter {
            // Regla de validación: No guardar ejercicios vacíos
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
    }
}