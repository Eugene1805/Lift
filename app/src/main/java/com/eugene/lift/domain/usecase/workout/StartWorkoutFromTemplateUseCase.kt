package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class StartWorkoutFromTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    /**
     * Crea una nueva sesión activa basada en una plantilla.
     * Pre-crea los sets vacíos según el 'targetSets' de la plantilla.
     */
    suspend operator fun invoke(templateId: String): WorkoutSession? {
        // 1. Obtenemos la plantilla (usamos .first() para obtener el valor actual del Flow)
        val template = templateRepository.getTemplate(templateId).first() ?: return null

        // 2. Mapeamos Ejercicios de Plantilla -> Ejercicios de Sesión
        val sessionExercises = template.exercises.map { templateExercise ->

            // Generamos N sets vacíos según lo que pedía la plantilla
            val initialSets = (1..templateExercise.targetSets).map { index ->
                WorkoutSet(
                    id = UUID.randomUUID().toString(),
                    weight = 0.0, // Opcional: Podrías buscar el peso de la última sesión aquí (Mejora futura)
                    reps = 0,
                    completed = false,
                    rpe = null,
                    rir = null,
                    isPr = false
                )
            }

            SessionExercise(
                id = UUID.randomUUID().toString(),
                exercise = templateExercise.exercise,
                sets = initialSets
            )
        }

        // 3. Retornamos la sesión lista para empezar
        return WorkoutSession(
            id = UUID.randomUUID().toString(),
            templateId = template.id,
            name = template.name, // El nombre puede ser editado luego
            date = LocalDateTime.now(), // Hora de inicio
            durationSeconds = 0,
            exercises = sessionExercises
        )
    }
}