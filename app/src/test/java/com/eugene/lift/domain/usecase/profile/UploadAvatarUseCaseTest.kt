package com.eugene.lift.domain.usecase.profile

import android.net.Uri
import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.ImageRepository
import com.eugene.lift.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UploadAvatarUseCaseTest {

    private lateinit var imageRepository: ImageRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var safeExecutor: SafeExecutor
    private lateinit var useCase: UploadAvatarUseCase

    private val mockUri = mockk<Uri>(relaxed = true)
    private val profileId = "profile-001"
    private val savedPath = "/data/data/com.eugene.lift/files/images/avatar_profile-001.jpg"

    @Before
    fun setUp() {
        imageRepository = mockk(relaxed = true)
        userProfileRepository = mockk(relaxed = true)
        safeExecutor = SafeExecutor()
        useCase = UploadAvatarUseCase(imageRepository, userProfileRepository, safeExecutor)
    }

    @Test
    fun `invoke returns success with saved path when image saves and profile updates`() = runTest {
        coEvery { imageRepository.saveImageLocally(mockUri, match { it.startsWith("avatar_$profileId") }, any()) } returns savedPath
        coEvery { userProfileRepository.updateAvatarUrl(profileId, savedPath) } returns Unit

        val result = useCase(profileId, mockUri)

        assertTrue(result is AppResult.Success)
        assertEquals(savedPath, (result as AppResult.Success).data)
        coVerify(exactly = 1) { imageRepository.saveImageLocally(mockUri, match { it.startsWith("avatar_$profileId") }, any()) }
        coVerify(exactly = 1) { userProfileRepository.updateAvatarUrl(profileId, savedPath) }
    }

    @Test
    fun `invoke returns error when saveImageLocally returns null`() = runTest {
        coEvery { imageRepository.saveImageLocally(any(), any(), any()) } returns null

        val result = useCase(profileId, mockUri)

        assertTrue(result is AppResult.Error)
        coVerify(exactly = 0) { userProfileRepository.updateAvatarUrl(any(), any()) }
    }

    @Test
    fun `invoke deletes old avatar when old path differs from new path`() = runTest {
        val oldPath = "/old/path/avatar.jpg"
        coEvery { imageRepository.saveImageLocally(any(), any(), any()) } returns savedPath
        coEvery { userProfileRepository.updateAvatarUrl(any(), any()) } returns Unit

        useCase(profileId, mockUri, oldAvatarPath = oldPath)

        coVerify(exactly = 1) { imageRepository.deleteImage(oldPath) }
    }

    @Test
    fun `invoke does not delete old avatar when old path equals new path`() = runTest {
        coEvery { imageRepository.saveImageLocally(any(), any(), any()) } returns savedPath
        coEvery { userProfileRepository.updateAvatarUrl(any(), any()) } returns Unit

        useCase(profileId, mockUri, oldAvatarPath = savedPath)

        coVerify(exactly = 0) { imageRepository.deleteImage(any()) }
    }

    @Test
    fun `invoke does not delete when no old avatar exists`() = runTest {
        coEvery { imageRepository.saveImageLocally(any(), any(), any()) } returns savedPath
        coEvery { userProfileRepository.updateAvatarUrl(any(), any()) } returns Unit

        useCase(profileId, mockUri, oldAvatarPath = null)

        coVerify(exactly = 0) { imageRepository.deleteImage(any()) }
    }

    @Test
    fun `invoke returns error when userProfileRepository throws`() = runTest {
        coEvery { imageRepository.saveImageLocally(any(), any(), any()) } returns savedPath
        coEvery { userProfileRepository.updateAvatarUrl(any(), any()) } throws RuntimeException("DB error")

        val result = useCase(profileId, mockUri)

        assertTrue(result is AppResult.Error)
    }
}
