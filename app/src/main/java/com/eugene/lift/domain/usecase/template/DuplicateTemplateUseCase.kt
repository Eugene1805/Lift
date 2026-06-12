package com.eugene.lift.domain.usecase.template

import android.content.Context
import com.eugene.lift.R
import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.TemplateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class DuplicateTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository,
    @ApplicationContext private val context: Context,
    private val safeExecutor: SafeExecutor
) {
    suspend operator fun invoke(templateId: String): AppResult<Unit> {
        return safeExecutor.execute {
            val original = repository.getTemplate(templateId).first() ?: return@execute

            val newTemplateId = UUID.randomUUID().toString()

            val newExercises = original.exercises.map { templateExercise ->
                templateExercise.copy(
                    id = UUID.randomUUID().toString()
                )
            }

            val copy = original.copy(
                id = newTemplateId,
                name = context.getString(R.string.template_duplicate_name, original.name),
                exercises = newExercises,
                isArchived = false,
                lastPerformedAt = null
            )

            repository.saveTemplate(copy)
        }
    }
}
