package com.eugene.lift.domain.usecase.profile

import android.net.Uri
import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.ImageRepository
import com.eugene.lift.domain.repository.UserProfileRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val userProfileRepository: UserProfileRepository,
    private val safeExecutor: SafeExecutor
) {
    /**
     * @param profileId  ID of the profile to update.
     * @param sourceUri  URI selected by the user via camera or gallery.
     * @param oldAvatarPath  Current avatar path to be deleted after successful upload, or null.
     * @return The local file path of the saved avatar on success, or an [AppError] on failure.
     */
    suspend operator fun invoke(
        profileId: String,
        sourceUri: Uri,
        oldAvatarPath: String? = null
    ): AppResult<String> {
        return safeExecutor.execute {
            val fileName = "avatar_${profileId}_${System.currentTimeMillis()}"
            val savedPath = imageRepository.saveImageLocally(sourceUri, fileName)
                ?: throw IllegalStateException("Failed to save image locally")

            userProfileRepository.updateAvatarUrl(profileId, savedPath)

            if (oldAvatarPath != null && oldAvatarPath != savedPath) {
                imageRepository.deleteImage(oldAvatarPath)
            }

            savedPath
        }
    }
}
