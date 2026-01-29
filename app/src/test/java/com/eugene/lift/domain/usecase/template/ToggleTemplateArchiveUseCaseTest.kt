package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.repository.TemplateRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit test for ToggleTemplateArchiveUseCase
 * Tests archiving and unarchiving of workout templates
 */
class ToggleTemplateArchiveUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var useCase: ToggleTemplateArchiveUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = ToggleTemplateArchiveUseCase(repository)
    }

    @Test
    fun `invoke archives template when isArchived is true`() = runTest {
        // GIVEN
        val templateId = "template-1"

        // WHEN
        useCase(templateId, isArchived = true)

        // THEN
        coVerify(exactly = 1) { repository.archiveTemplate(templateId, true) }
    }

    @Test
    fun `invoke unarchives template when isArchived is false`() = runTest {
        // GIVEN
        val templateId = "template-1"

        // WHEN
        useCase(templateId, isArchived = false)

        // THEN
        coVerify(exactly = 1) { repository.archiveTemplate(templateId, false) }
    }

    @Test
    fun `invoke handles multiple archive operations`() = runTest {
        // GIVEN
        val templateId = "template-1"

        // WHEN
        useCase(templateId, isArchived = true)
        useCase(templateId, isArchived = false)
        useCase(templateId, isArchived = true)

        // THEN
        coVerify(exactly = 2) { repository.archiveTemplate(templateId, true) }
        coVerify(exactly = 1) { repository.archiveTemplate(templateId, false) }
    }

    @Test
    fun `invoke handles archiving multiple templates`() = runTest {
        // GIVEN
        val templateIds = listOf("template-1", "template-2", "template-3")

        // WHEN
        templateIds.forEach { useCase(it, isArchived = true) }

        // THEN
        templateIds.forEach { id ->
            coVerify(exactly = 1) { repository.archiveTemplate(id, true) }
        }
    }

    @Test
    fun `invoke passes parameters unchanged to repository`() = runTest {
        // GIVEN
        val templateId = "special-template-123"
        val isArchived = true

        // WHEN
        useCase(templateId, isArchived)

        // THEN
        coVerify(exactly = 1) { repository.archiveTemplate("special-template-123", true) }
    }

    @Test
    fun `invoke handles UUID format template IDs`() = runTest {
        // GIVEN
        val templateId = "550e8400-e29b-41d4-a716-446655440000"

        // WHEN
        useCase(templateId, isArchived = true)

        // THEN
        coVerify(exactly = 1) { repository.archiveTemplate(templateId, true) }
    }
}
