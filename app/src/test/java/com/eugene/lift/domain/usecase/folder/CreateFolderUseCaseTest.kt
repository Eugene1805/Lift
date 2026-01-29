package com.eugene.lift.domain.usecase.folder

import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.repository.FolderRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for CreateFolderUseCase
 * Tests folder creation with validation
 */
class CreateFolderUseCaseTest {

    private lateinit var repository: FolderRepository
    private lateinit var useCase: CreateFolderUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = CreateFolderUseCase(repository)
    }

    @Test
    fun `invoke creates folder with valid name and color`() = runTest {
        // GIVEN
        val name = "Leg Day Workouts"
        val color = "#FF5722"

        // WHEN
        useCase(name, color)

        // THEN
        val slot = slot<Folder>()
        coVerify(exactly = 1) { repository.createFolder(capture(slot)) }

        val folder = slot.captured
        assertEquals("Leg Day Workouts", folder.name)
        assertEquals("#FF5722", folder.color)
    }

    @Test
    fun `invoke trims whitespace from name`() = runTest {
        // GIVEN
        val name = "  Leg Day  "
        val color = "#FF5722"

        // WHEN
        useCase(name, color)

        // THEN
        val slot = slot<Folder>()
        coVerify(exactly = 1) { repository.createFolder(capture(slot)) }

        val folder = slot.captured
        assertEquals("Leg Day", folder.name)
    }

    @Test
    fun `invoke does not create folder when name is blank`() = runTest {
        // GIVEN
        val name = ""
        val color = "#FF5722"

        // WHEN
        useCase(name, color)

        // THEN
        coVerify(exactly = 0) { repository.createFolder(any()) }
    }

    @Test
    fun `invoke does not create folder when name is only whitespace`() = runTest {
        // GIVEN
        val name = "   "
        val color = "#FF5722"

        // WHEN
        useCase(name, color)

        // THEN
        coVerify(exactly = 0) { repository.createFolder(any()) }
    }

    @Test
    fun `invoke generates unique ID for folder`() = runTest {
        // GIVEN
        val name = "Test Folder"
        val color = "#FF5722"

        // WHEN
        useCase(name, color)

        // THEN
        val slot = slot<Folder>()
        coVerify(exactly = 1) { repository.createFolder(capture(slot)) }

        val folder = slot.captured
        assertTrue(folder.id.isNotEmpty())
    }

    @Test
    fun `invoke sets createdAt timestamp`() = runTest {
        // GIVEN
        val name = "Test Folder"
        val color = "#FF5722"
        val beforeTime = System.currentTimeMillis()

        // WHEN
        useCase(name, color)

        // THEN
        val afterTime = System.currentTimeMillis()
        val slot = slot<Folder>()
        coVerify(exactly = 1) { repository.createFolder(capture(slot)) }

        val folder = slot.captured
        assertTrue(folder.createdAt >= beforeTime)
        assertTrue(folder.createdAt <= afterTime)
    }

    @Test
    fun `invoke handles different color formats`() = runTest {
        // GIVEN
        val colors = listOf("#FF5722", "#00FF00", "#123456", "#ABCDEF")

        // WHEN
        colors.forEach { color ->
            useCase("Folder", color)
        }

        // THEN
        coVerify(exactly = colors.size) { repository.createFolder(any()) }
    }

    @Test
    fun `invoke handles special characters in name`() = runTest {
        // GIVEN
        val name = "My Folder 💪 (2024) #1"
        val color = "#FF5722"

        // WHEN
        useCase(name, color)

        // THEN
        val slot = slot<Folder>()
        coVerify(exactly = 1) { repository.createFolder(capture(slot)) }

        val folder = slot.captured
        assertEquals("My Folder 💪 (2024) #1", folder.name)
    }

    @Test
    fun `invoke handles long folder names`() = runTest {
        // GIVEN
        val longName = "A".repeat(100)
        val color = "#FF5722"

        // WHEN
        useCase(longName, color)

        // THEN
        val slot = slot<Folder>()
        coVerify(exactly = 1) { repository.createFolder(capture(slot)) }

        val folder = slot.captured
        assertEquals(longName, folder.name)
    }

    @Test
    fun `invoke creates multiple folders with unique IDs`() = runTest {
        // GIVEN
        val names = listOf("Folder 1", "Folder 2", "Folder 3")
        val capturedFolders = mutableListOf<Folder>()

        // WHEN
        names.forEach { name ->
            useCase(name, "#FF5722")
        }

        // THEN
        coVerify(exactly = 3) {
            repository.createFolder(capture(capturedFolders))
        }

        val ids = capturedFolders.map { it.id }
        assertEquals(3, ids.distinct().size) // All IDs should be unique
    }
}
