package com.eugene.lift.domain.repository

import android.net.Uri

/**
 * Repository interface for local image operations.
 * Following clean architecture — lives in the domain layer with no Android-framework leaks beyond Uri.
 */
interface ImageRepository {

    /**
     * Compresses and saves an image from the given [sourceUri] to local app-private storage.
     *
     * @param sourceUri  URI returned by the photo-picker / camera contract.
     * @param fileName   Target file name (without extension – .jpg will be appended).
     * @param quality    JPEG compression quality 1-100. Defaults to 80.
     * @return Absolute path of the saved file, or null on failure.
     */
    suspend fun saveImageLocally(
        sourceUri: Uri,
        fileName: String,
        quality: Int = 80
    ): String?

    /**
     * Deletes a locally stored image file.
     *
     * @param filePath Absolute path previously returned by [saveImageLocally].
     */
    suspend fun deleteImage(filePath: String)
}
