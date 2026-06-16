package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.ActiveWorkoutDraft
import com.eugene.lift.domain.model.ActiveWorkoutDraftSummary
import kotlinx.coroutines.flow.Flow

interface ActiveWorkoutDraftRepository {
    fun observeSummary(): Flow<ActiveWorkoutDraftSummary?>
    suspend fun getDraft(): ActiveWorkoutDraft?
    suspend fun saveDraft(draft: ActiveWorkoutDraft)
    suspend fun clearDraft()
}
