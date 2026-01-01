package com.eugene.lift.domain.usecase

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.isNotEmpty

enum class SortOrder { NAME_ASC, NAME_DESC }

data class ExerciseFilter(
    val query: String = "",
    val bodyParts: Set<BodyPart> = emptySet(),
    val categories: Set<ExerciseCategory> = emptySet(),
    val sortOrder: SortOrder = SortOrder.NAME_ASC
)

class GetExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke(filter: ExerciseFilter): Flow<List<Exercise>> {
        return repository.getExercises().map { list ->
            var result = list

            // 1. Text Search
            if (filter.query.isNotBlank()) {
                result = result.filter {
                    it.name.contains(filter.query, ignoreCase = true)
                }
            }

            // 2. Body Part Filter
            if (filter.bodyParts.isNotEmpty()) {
                result = result.filter { exercise ->
                    exercise.bodyParts.intersect((filter.bodyParts as Iterable<BodyPart>).toSet()).isNotEmpty()
                }
            }

            // 3. Category Filter
            if (filter.categories.isNotEmpty()) {
                result = result.filter { it.category in filter.categories }
            }

            // 4. Sorting
            result = when (filter.sortOrder) {
                SortOrder.NAME_ASC -> result.sortedBy { it.name }
                SortOrder.NAME_DESC -> result.sortedByDescending { it.name }
            }

            result
        }
    }
}