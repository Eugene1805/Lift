package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class DuplicateTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(templateId: String) {
        val original = repository.getTemplate(templateId).first() ?: return

        val newTemplateId = UUID.randomUUID().toString()

        val newExercises = original.exercises.map { templateExercise ->
            templateExercise.copy(
                id = UUID.randomUUID().toString(),

                // IMPORTANTE: Si tu modelo de dominio 'TemplateExercise' tiene el campo 'templateId',
                // debes actualizarlo aquí. Si no lo tiene (porque es implícito), con cambiar el 'id' basta.
                // Asumiendo que es una clase embebida o relacionada, al menos el ID debe ser nuevo.
            )
        }

        val copy = original.copy(
            id = newTemplateId,
            name = "${original.name} ${UUID.randomUUID()}",
            exercises = newExercises,
            isArchived = false,
            lastPerformedAt = null
        )

        repository.saveTemplate(copy)
    }
}