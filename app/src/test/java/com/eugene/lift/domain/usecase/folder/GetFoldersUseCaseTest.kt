package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for GetFoldersUseCase
 * Tests retrieval of all folders
 */
class GetFoldersUseCaseTest {

    private lateinit var repository: FolderRepository
    private lateinit var useCase: GetFoldersUseCase

    private val sampleFolders = listOf(
        Folder(
            id = "folder-1",
            name = "Leg Day",
            color = "#FF5722",
            createdAt = System.currentTimeMillis()
        ),
        Folder(
            id = "folder-2",
            name = "Upper Body",
            color = "#4CAF50",
            createdAt = System.currentTimeMillis()
        ),
        Folder(
            id = "folder-3",
            name = "Full Body",
            color = "#2196F3",
            createdAt = System.currentTimeMillis()
        )
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetFoldersUseCase(repository)
    }

    @Test
    fun `invoke returns all folders from repository`() = runTest {
        // GIVEN
        coEvery { repository.getAllFolders() } returns flowOf(sampleFolders)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(3, result.size)
        assertTrue(result.any { it.name == "Leg Day" })
        assertTrue(result.any { it.name == "Upper Body" })
        assertTrue(result.any { it.name == "Full Body" })
    }

    @Test
    fun `invoke returns empty list when no folders exist`() = runTest {
        // GIVEN
        coEvery { repository.getAllFolders() } returns flowOf(emptyList())

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `invoke returns folders with all properties`() = runTest {
        // GIVEN
        coEvery { repository.getAllFolders() } returns flowOf(sampleFolders)

        // WHEN
        val result = useCase().first()

        // THEN
        val folder = result.first()
        assertEquals("folder-1", folder.id)
        assertEquals("Leg Day", folder.name)
        assertEquals("#FF5722", folder.color)
        assertTrue(folder.createdAt > 0)
    }

    @Test
    fun `invoke returns folders in order from repository`() = runTest {
        // GIVEN
        coEvery { repository.getAllFolders() } returns flowOf(sampleFolders)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals("Leg Day", result[0].name)
        assertEquals("Upper Body", result[1].name)
        assertEquals("Full Body", result[2].name)
    }

    @Test
    fun `invoke handles single folder`() = runTest {
        // GIVEN
        val singleFolder = listOf(sampleFolders.first())
        coEvery { repository.getAllFolders() } returns flowOf(singleFolder)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals("Leg Day", result.first().name)
    }

    @Test
    fun `invoke handles large number of folders`() = runTest {
        // GIVEN
        val manyFolders = (1..100).map { index ->
            Folder(
                id = "folder-$index",
                name = "Folder $index",
                color = "#FF5722",
                createdAt = System.currentTimeMillis()
            )
        }
        coEvery { repository.getAllFolders() } returns flowOf(manyFolders)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(100, result.size)
    }

    @Test
    fun `invoke returns flow that emits multiple times`() = runTest {
        // GIVEN
        val flow = kotlinx.coroutines.flow.flow {
            emit(listOf(sampleFolders[0]))
            kotlinx.coroutines.delay(100)
            emit(sampleFolders)
        }
        coEvery { repository.getAllFolders() } returns flow

        // WHEN
        val results = mutableListOf<List<Folder>>()
        useCase().collect { results.add(it) }

        // THEN
        assertEquals(2, results.size)
        assertEquals(1, results[0].size)
        assertEquals(3, results[1].size)
    }

    @Test
    fun `invoke handles folders with different colors`() = runTest {
        // GIVEN
        val coloredFolders = listOf(
            Folder(id = "1", name = "Red", color = "#FF0000", createdAt = 0),
            Folder(id = "2", name = "Green", color = "#00FF00", createdAt = 0),
            Folder(id = "3", name = "Blue", color = "#0000FF", createdAt = 0)
        )
        coEvery { repository.getAllFolders() } returns flowOf(coloredFolders)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals("#FF0000", result[0].color)
        assertEquals("#00FF00", result[1].color)
        assertEquals("#0000FF", result[2].color)
    }
}
