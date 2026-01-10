package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import javax.inject.Inject

class SaveTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(template: WorkoutTemplate) {
        if (template.name.isBlank()) {
            throw IllegalArgumentException("El nombre de la rutina no puede estar vacío")
        }

        // Validación opcional: ¿Permitimos rutinas sin ejercicios?
        // Generalmente sí (drafts), pero si quisieras evitarlo, descomenta esto:
        // if (template.exercises.isEmpty()) {
        //     throw IllegalArgumentException("La rutina debe tener al menos un ejercicio")
        // }

        repository.saveTemplate(template)
    }
}