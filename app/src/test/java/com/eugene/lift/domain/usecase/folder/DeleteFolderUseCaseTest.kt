package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.domain.repository.FolderRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit test for DeleteFolderUseCase
 * Tests folder deletion
 */
class DeleteFolderUseCaseTest {

    private lateinit var repository: FolderRepository
    private lateinit var useCase: DeleteFolderUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = DeleteFolderUseCase(repository)
    }

    @Test
    fun `invoke deletes folder by ID`() = runTest {
        // GIVEN
        val folderId = "folder-1"

        // WHEN
        useCase(folderId)

        // THEN
        coVerify(exactly = 1) { repository.deleteFolder(folderId) }
    }

    @Test
    fun `invoke handles deletion of multiple folders sequentially`() = runTest {
        // GIVEN
        val folderIds = listOf("folder-1", "folder-2", "folder-3")

        // WHEN
        folderIds.forEach { useCase(it) }

        // THEN
        folderIds.forEach { id ->
            coVerify(exactly = 1) { repository.deleteFolder(id) }
        }
    }

    @Test
    fun `invoke handles UUID format folder IDs`() = runTest {
        // GIVEN
        val folderId = "550e8400-e29b-41d4-a716-446655440000"

        // WHEN
        useCase(folderId)

        // THEN
        coVerify(exactly = 1) { repository.deleteFolder(folderId) }
    }

    @Test
    fun `invoke passes folder ID unchanged to repository`() = runTest {
        // GIVEN
        val folderId = "special-folder-123"

        // WHEN
        useCase(folderId)

        // THEN
        coVerify(exactly = 1) { repository.deleteFolder("special-folder-123") }
    }

    @Test
    fun `invoke handles empty string folder ID`() = runTest {
        // GIVEN - Repository should handle validation
        val folderId = ""

        // WHEN
        useCase(folderId)

        // THEN
        coVerify(exactly = 1) { repository.deleteFolder(folderId) }
    }
}
