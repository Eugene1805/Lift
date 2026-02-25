package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPersonalRecordUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(exerciseId: String): Flow<WorkoutSet?> {
        return repository.getPersonalRecord(exerciseId)
    }
}
