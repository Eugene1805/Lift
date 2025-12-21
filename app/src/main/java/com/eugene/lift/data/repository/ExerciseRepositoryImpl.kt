package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao
) : ExerciseRepository {

    override fun getExercises(): Flow<List<ExerciseEntity>> {
        return dao.getAllExercisesRaw().map { results ->
            results.map { row ->
                // Reconstruimos la lista desde el string que nos dio GROUP_CONCAT
                val parts = if (row.bodyParts.isBlank()) emptyList()
                else row.bodyParts.split(",").map { BodyPart.valueOf(it) }

                row.exercise.copy(bodyParts = parts) // Asumiendo que el data class tiene el campo
            }
        }
    }
    override suspend fun getExercise(id: String): ExerciseEntity? = dao.getExerciseById(id)

    override suspend fun saveExercise(exercise: ExerciseEntity) {
        // Separamos la entidad de sus partes para guardarlas
        val entityClean = exercise.copy(bodyParts = emptyList()) // OJO: La Entity DB no debe tener lista
        dao.saveExerciseWithParts(entityClean, exercise.bodyParts)
    }

    override suspend fun deleteExercise(exercise: ExerciseEntity) = dao.deleteExercise(exercise)
}