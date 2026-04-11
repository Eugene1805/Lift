package com.eugene.lift.domain.usecase.profile

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case for changing a user's username.
 *
 * Validation rules (local-only, no availability check as the app is offline-first):
 * - Must be 3–30 characters.
 * - Only lowercase letters, digits and underscores (_) allowed.
 * - Cannot start or end with an underscore.
 */
class UpdateUsernameUseCase @Inject constructor(
    private val repository: UserProfileRepository,
    private val safeExecutor: SafeExecutor
) {

    suspend operator fun invoke(id: String, newUsername: String): AppResult<Unit> {
        val validationError = validate(newUsername)
        if (validationError != null) return AppResult.Error(validationError)

        return safeExecutor.execute {
            repository.updateUsername(id, newUsername)
        }
    }

    /** Returns an [AppError] if invalid, null otherwise. */
    fun validate(username: String): AppError? {
        if (username.length !in MIN_LENGTH..MAX_LENGTH) {
            return AppError.Validation
        }
        if (!USERNAME_REGEX.matches(username)) {
            return AppError.Validation
        }
        if (username.startsWith('_') || username.endsWith('_')) {
            return AppError.Validation
        }
        return null
    }

    companion object {
        const val MIN_LENGTH = 3
        const val MAX_LENGTH = 30
        private val USERNAME_REGEX = Regex("^[a-z0-9_]+$")
    }
}
