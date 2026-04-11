package com.eugene.lift.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.eugene.lift.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [ImageRepository].
 *
 * Images are stored as JPEG files inside the app's private `images/` directory.
 * The approach is:
 * 1. Open the source URI via the [android.content.ContentResolver].
 * 2. Decode it into a sized-down [Bitmap] (max 512×512) to keep file-size reasonable.
 * 3. Compress to JPEG at the requested quality.
 * 4. Write to the app-private files directory — no permissions needed.
 */
@Singleton
class ImageRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : ImageRepository {

    private val imagesDir: File
        get() = File(context.filesDir, "images").also { it.mkdirs() }

    override suspend fun saveImageLocally(
        sourceUri: Uri,
        fileName: String,
        quality: Int
    ): String? {
        return try {
            val bitmap = decodeSampledBitmap(sourceUri) ?: return null
            val outputFile = File(imagesDir, "$fileName.jpg")
            withContext(ioDispatcher) {
                FileOutputStream(outputFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), out)
                }
            }
            bitmap.recycle()
            outputFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun deleteImage(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val deleted = file.delete()
            if (!deleted) return
        }
    }

    /**
     * Decodes the bitmap from [uri] at a ~512-px target size to avoid OOM errors
     * while still producing a high-quality avatar image.
     */
    private fun decodeSampledBitmap(uri: Uri): Bitmap? {
        val resolver = context.contentResolver

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

        options.inSampleSize = calculateInSampleSize(options)
        options.inJustDecodeBounds = false

        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > MAX_DIMENSION || width > MAX_DIMENSION) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (
                (halfHeight / inSampleSize) >= MAX_DIMENSION &&
                (halfWidth / inSampleSize) >= MAX_DIMENSION
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    companion object {
        private const val MAX_DIMENSION = 512
    }
}
