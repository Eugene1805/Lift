package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.repository.TemplateRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit test for DeleteTemplateUseCase
 * Tests deletion of workout templates
 */
class DeleteTemplateUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var useCase: DeleteTemplateUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = DeleteTemplateUseCase(repository)
    }

    @Test
    fun `invoke deletes template by ID`() = runTest {
        // GIVEN
        val templateId = "template-1"

        // WHEN
        useCase(templateId)

        // THEN
        coVerify(exactly = 1) { repository.deleteTemplate(templateId) }
    }

    @Test
    fun `invoke handles deletion of multiple templates sequentially`() = runTest {
        // GIVEN
        val templateIds = listOf("template-1", "template-2", "template-3")

        // WHEN
        templateIds.forEach { useCase(it) }

        // THEN
        templateIds.forEach { id ->
            coVerify(exactly = 1) { repository.deleteTemplate(id) }
        }
    }

    @Test
    fun `invoke handles deletion with UUID format`() = runTest {
        // GIVEN
        val templateId = "550e8400-e29b-41d4-a716-446655440000"

        // WHEN
        useCase(templateId)

        // THEN
        coVerify(exactly = 1) { repository.deleteTemplate(templateId) }
    }

    @Test
    fun `invoke handles deletion with empty string ID`() = runTest {
        // GIVEN - Repository should handle validation
        val templateId = ""

        // WHEN
        useCase(templateId)

        // THEN
        coVerify(exactly = 1) { repository.deleteTemplate(templateId) }
    }

    @Test
    fun `invoke delegates to repository without modification`() = runTest {
        // GIVEN
        val templateId = "special-template-123"

        // WHEN
        useCase(templateId)

        // THEN
        coVerify(exactly = 1) { repository.deleteTemplate("special-template-123") }
    }
}
