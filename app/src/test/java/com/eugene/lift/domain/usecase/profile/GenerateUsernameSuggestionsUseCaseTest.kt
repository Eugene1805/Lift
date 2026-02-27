package com.eugene.lift.domain.usecase.profile

import com.eugene.lift.domain.repository.UserProfileRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateUsernameSuggestionsUseCaseTest {

    private lateinit var repository: UserProfileRepository
    private lateinit var useCase: GenerateUsernameSuggestionsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GenerateUsernameSuggestionsUseCase(repository)
    }

    @Test
    fun `invoke calls repository with default count of 5`() {
        val expected = listOf("mighty_lion_42", "swift_bear_17", "bold_eagle_88", "epic_wolf_33", "iron_hawk_55")
        every { repository.generateUsernameSuggestions(5) } returns expected

        val result = useCase()

        assertEquals(expected, result)
        verify(exactly = 1) { repository.generateUsernameSuggestions(5) }
    }

    @Test
    fun `invoke calls repository with custom count`() {
        val expected = listOf("titan_bull_12", "apex_shark_99")
        every { repository.generateUsernameSuggestions(2) } returns expected

        val result = useCase(2)

        assertEquals(expected, result)
        verify(exactly = 1) { repository.generateUsernameSuggestions(2) }
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() {
        every { repository.generateUsernameSuggestions(any()) } returns emptyList()

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke returns exactly the count requested`() {
        val suggestions = List(10) { "user_$it" }
        every { repository.generateUsernameSuggestions(10) } returns suggestions

        val result = useCase(10)

        assertEquals(10, result.size)
    }
}
