package com.eugene.lift.domain.usecase

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.repository.ExerciseRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.collections.isNotEmpty

enum class SortOrder { NAME_ASC, NAME_DESC, RECENT, FREQUENCY }

data class ExerciseFilter(
    val query: String = "",
    val bodyParts: Set<BodyPart> = emptySet(),
    val categories: Set<ExerciseCategory> = emptySet(),
    val sortOrder: SortOrder = SortOrder.NAME_ASC
)

data class ExerciseUsageStats(
    val usageCount: Map<String, Int> = emptyMap(),
    val lastUsedDates: Map<String, LocalDateTime> = emptyMap()
)

class GetExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository
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
                SortOrder.RECENT, SortOrder.FREQUENCY -> result // Will be sorted externally with stats
            }

            result
        }
    }

    suspend fun getUsageStats(): ExerciseUsageStats {
        return ExerciseUsageStats(
            usageCount = workoutRepository.getExerciseUsageCount(),
            lastUsedDates = workoutRepository.getExerciseLastUsedDates()
        )
    }

    fun sortByStats(
        exercises: List<Exercise>,
        sortOrder: SortOrder,
        stats: ExerciseUsageStats
    ): List<Exercise> {
        return when (sortOrder) {
            SortOrder.RECENT -> {
                val exercisesWithDates = exercises.map { exercise ->
                    exercise to stats.lastUsedDates[exercise.id]
                }
                // Sort: exercises with dates first (most recent first), then exercises without dates alphabetically
                exercisesWithDates.sortedWith(
                    compareBy<Pair<Exercise, LocalDateTime?>>(
                        { it.second == null }, // null dates go last
                    ).thenByDescending { it.second }
                        .thenBy { it.first.name }
                ).map { it.first }
            }
            SortOrder.FREQUENCY -> {
                val exercisesWithCount = exercises.map { exercise ->
                    exercise to (stats.usageCount[exercise.id] ?: 0)
                }
                // Sort by count descending, then alphabetically
                exercisesWithCount.sortedWith(
                    compareByDescending<Pair<Exercise, Int>> { it.second }
                        .thenBy { it.first.name }
                ).map { it.first }
            }
            else -> exercises
        }
    }
}