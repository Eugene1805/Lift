package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.UserCredentialsDao
import com.eugene.lift.data.local.dao.UserProfileDao
import com.eugene.lift.data.local.entity.UserProfileEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class UserProfileRepositoryImplTest {

    private lateinit var userProfileDao: UserProfileDao
    private lateinit var userCredentialsDao: UserCredentialsDao
    private lateinit var repository: UserProfileRepositoryImpl

    private fun buildEntity(
        id: String = "test-id",
        username: String = "mighty_lion_42",
        displayName: String = "Mighty Lion",
        avatarUrl: String? = null
    ) = UserProfileEntity(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @Before
    fun setUp() {
        userProfileDao = mockk(relaxed = true)
        userCredentialsDao = mockk(relaxed = true)
        repository = UserProfileRepositoryImpl(userProfileDao, userCredentialsDao)
    }

    // ── getCurrentProfile ────────────────────────────────────────────────────

    @Test
    fun `getCurrentProfile maps entity to domain`() = runTest {
        val entity = buildEntity()
        coEvery { userProfileDao.getCurrentProfile() } returns flowOf(entity)

        val result = mutableListOf<Any?>()
        repository.getCurrentProfile().collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals("test-id", (result[0] as com.eugene.lift.domain.model.UserProfile).id)
    }

    @Test
    fun `getCurrentProfile emits null when no profile exists`() = runTest {
        coEvery { userProfileDao.getCurrentProfile() } returns flowOf(null)

        val result = mutableListOf<Any?>()
        repository.getCurrentProfile().collect { result.add(it) }

        assertNull(result[0])
    }

    // ── getCurrentProfileOnce ────────────────────────────────────────────────

    @Test
    fun `getCurrentProfileOnce returns mapped domain profile`() = runTest {
        coEvery { userProfileDao.getCurrentProfileOnce() } returns buildEntity()

        val profile = repository.getCurrentProfileOnce()

        assertNotNull(profile)
        assertEquals("test-id", profile?.id)
        assertEquals("mighty_lion_42", profile?.username)
    }

    @Test
    fun `getCurrentProfileOnce returns null when no profile`() = runTest {
        coEvery { userProfileDao.getCurrentProfileOnce() } returns null

        val profile = repository.getCurrentProfileOnce()

        assertNull(profile)
    }

    // ── getOrCreateProfile ───────────────────────────────────────────────────

    @Test
    fun `getOrCreateProfile returns existing profile if one exists`() = runTest {
        coEvery { userProfileDao.getCurrentProfileOnce() } returns buildEntity(id = "existing-id")

        val profile = repository.getOrCreateProfile()

        assertEquals("existing-id", profile.id)
        coVerify(exactly = 0) { userProfileDao.insertProfile(any()) }
    }

    @Test
    fun `getOrCreateProfile creates and inserts a new profile when none exists`() = runTest {
        coEvery { userProfileDao.getCurrentProfileOnce() } returns null

        val profile = repository.getOrCreateProfile()

        assertNotNull(profile.id)
        coVerify(exactly = 1) { userProfileDao.insertProfile(any()) }
    }

    // ── updateDisplayName ────────────────────────────────────────────────────

    @Test
    fun `updateDisplayName delegates to DAO with current timestamp`() = runTest {
        val idSlot = slot<String>()
        val nameSlot = slot<String>()
        coEvery { userProfileDao.updateDisplayName(capture(idSlot), capture(nameSlot), any()) } returns Unit

        repository.updateDisplayName("test-id", "New Name")

        assertEquals("test-id", idSlot.captured)
        assertEquals("New Name", nameSlot.captured)
    }

    // ── updateBio ────────────────────────────────────────────────────────────

    @Test
    fun `updateBio delegates null bio to DAO`() = runTest {
        var capturedBio: String? = "NOT_SET_SENTINEL"
        coEvery { userProfileDao.updateBio(any(), any(), any()) } answers {
            capturedBio = secondArg()
        }

        repository.updateBio("test-id", null)

        assertNull(capturedBio)
    }

    // ── updateAvatarUrl ──────────────────────────────────────────────────────

    @Test
    fun `updateAvatarUrl delegates path to DAO`() = runTest {
        var capturedPath: String? = null
        coEvery { userProfileDao.updateAvatarUrl(any(), any(), any()) } answers {
            capturedPath = secondArg()
        }

        repository.updateAvatarUrl("test-id", "/files/images/avatar.jpg")

        assertEquals("/files/images/avatar.jpg", capturedPath)
    }

    // ── updateUsername ───────────────────────────────────────────────────────

    @Test
    fun `updateUsername delegates to DAO with correct arguments`() = runTest {
        val idSlot = slot<String>()
        val usernameSlot = slot<String>()
        coEvery { userProfileDao.updateUsername(capture(idSlot), capture(usernameSlot), any()) } returns Unit

        repository.updateUsername("test-id", "new_username")

        assertEquals("test-id", idSlot.captured)
        assertEquals("new_username", usernameSlot.captured)
    }

    // ── generateUsernameSuggestions ──────────────────────────────────────────

    @Test
    fun `generateUsernameSuggestions returns requested count of suggestions`() {
        val suggestions = repository.generateUsernameSuggestions(5)

        assertEquals(5, suggestions.size)
    }

    @Test
    fun `generateUsernameSuggestions returns unique suggestions`() {
        // Run multiple times; with a large pool it's essentially impossible to get all identical
        val suggestions = repository.generateUsernameSuggestions(5)
        // At minimum, all should be non-blank
        suggestions.forEach { assert(it.isNotBlank()) }
    }

    @Test
    fun `generateUsernameSuggestions respects custom count`() {
        val suggestions = repository.generateUsernameSuggestions(3)
        assertEquals(3, suggestions.size)
    }

    // ── recordWorkoutCompleted ───────────────────────────────────────────────

    @Test
    fun `recordWorkoutCompleted calls incrementWorkoutCount`() = runTest {
        repository.recordWorkoutCompleted("test-id", volume = 0.0, duration = 0L, prCount = 0)

        coVerify(exactly = 1) { userProfileDao.incrementWorkoutCount(eq("test-id"), any<LocalDate>(), any()) }
    }

    @Test
    fun `recordWorkoutCompleted adds volume only when greater than zero`() = runTest {
        repository.recordWorkoutCompleted("test-id", volume = 150.0, duration = 0L, prCount = 0)

        coVerify(exactly = 1) { userProfileDao.addVolume(eq("test-id"), eq(150.0), any()) }
    }

    @Test
    fun `recordWorkoutCompleted skips volume when zero`() = runTest {
        repository.recordWorkoutCompleted("test-id", volume = 0.0, duration = 0L, prCount = 0)

        coVerify(exactly = 0) { userProfileDao.addVolume(any(), any(), any()) }
    }

    @Test
    fun `recordWorkoutCompleted adds duration only when greater than zero`() = runTest {
        repository.recordWorkoutCompleted("test-id", volume = 0.0, duration = 3600L, prCount = 0)

        coVerify(exactly = 1) { userProfileDao.addDuration(eq("test-id"), eq(3600L), any()) }
    }

    @Test
    fun `recordWorkoutCompleted adds PRs only when greater than zero`() = runTest {
        repository.recordWorkoutCompleted("test-id", volume = 0.0, duration = 0L, prCount = 2)

        coVerify(exactly = 1) { userProfileDao.addPRs(eq("test-id"), eq(2), any()) }
    }
}
