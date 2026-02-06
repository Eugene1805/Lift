package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.repository.ExerciseRepository
import javax.inject.Inject

class SaveExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(exercise: Exercise) {
        require(!(exercise.name.isBlank())){
            throw IllegalArgumentException("Exercise name cannot be empty")
        }
        // Add more business rules here if needed (e.g. max body parts?)

        repository.saveExercise(exercise)
    }
}