package com.eugene.lift.domain.usecase.profile

import com.eugene.lift.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case that wraps the repository's username-generation logic so it can be
 * injected and unit-tested independently from the repository implementation.
 */
class GenerateUsernameSuggestionsUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    /**
     * @param count How many suggestions to produce. Defaults to 5.
     * @return A list of fun, unique-looking username suggestions.
     */
    operator fun invoke(count: Int = 5): List<String> {
        return repository.generateUsernameSuggestions(count)
    }
}
