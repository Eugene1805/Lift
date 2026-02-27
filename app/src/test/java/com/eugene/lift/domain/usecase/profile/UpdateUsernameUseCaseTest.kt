package com.eugene.lift.domain.usecase.profile

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateUsernameUseCaseTest {

    private lateinit var repository: UserProfileRepository
    private lateinit var safeExecutor: SafeExecutor
    private lateinit var useCase: UpdateUsernameUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        safeExecutor = SafeExecutor()
        useCase = UpdateUsernameUseCase(repository, safeExecutor)
    }

    // ── Validation tests ────────────────────────────────────────────────────

    @Test
    fun `validate returns null for valid lowercase username`() {
        assertNull(useCase.validate("mighty_lion_42"))
    }

    @Test
    fun `validate returns null for minimum length username`() {
        assertNull(useCase.validate("abc"))
    }

    @Test
    fun `validate returns null for 30-char username`() {
        assertNull(useCase.validate("a".repeat(30)))
    }

    @Test
    fun `validate returns error for username shorter than 3 chars`() {
        assertEquals(AppError.Validation, useCase.validate("ab"))
    }

    @Test
    fun `validate returns error for empty username`() {
        assertEquals(AppError.Validation, useCase.validate(""))
    }

    @Test
    fun `validate returns error for username longer than 30 chars`() {
        assertEquals(AppError.Validation, useCase.validate("a".repeat(31)))
    }

    @Test
    fun `validate returns error for uppercase characters`() {
        assertEquals(AppError.Validation, useCase.validate("MightyLion"))
    }

    @Test
    fun `validate returns error for special characters`() {
        assertEquals(AppError.Validation, useCase.validate("mighty-lion"))
    }

    @Test
    fun `validate returns error for spaces`() {
        assertEquals(AppError.Validation, useCase.validate("mighty lion"))
    }

    @Test
    fun `validate returns error for username starting with underscore`() {
        assertEquals(AppError.Validation, useCase.validate("_mighty_lion"))
    }

    @Test
    fun `validate returns error for username ending with underscore`() {
        assertEquals(AppError.Validation, useCase.validate("mighty_lion_"))
    }

    @Test
    fun `validate allows digits in username`() {
        assertNull(useCase.validate("user123"))
    }

    @Test
    fun `validate allows underscore in middle`() {
        assertNull(useCase.validate("user_name_42"))
    }

    // ── Invoke tests ────────────────────────────────────────────────────────

    @Test
    fun `invoke returns Success when validation passes and repo call succeeds`() = runTest {
        coEvery { repository.updateUsername(any(), any()) } returns Unit

        val result = useCase("id-123", "valid_username")

        assertTrue(result is AppResult.Success)
        coVerify(exactly = 1) { repository.updateUsername("id-123", "valid_username") }
    }

    @Test
    fun `invoke returns Error without calling repository when validation fails`() = runTest {
        val result = useCase("id-123", "INVALID!")

        assertTrue(result is AppResult.Error)
        assertEquals(AppError.Validation, (result as AppResult.Error).error)
        coVerify(exactly = 0) { repository.updateUsername(any(), any()) }
    }

    @Test
    fun `invoke returns Error when repository throws exception`() = runTest {
        coEvery { repository.updateUsername(any(), any()) } throws RuntimeException("DB error")

        val result = useCase("id-123", "valid_user")

        assertTrue(result is AppResult.Error)
    }
}
