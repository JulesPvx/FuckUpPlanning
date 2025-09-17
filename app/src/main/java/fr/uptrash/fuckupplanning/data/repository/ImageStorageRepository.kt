package fr.uptrash.fuckupplanning.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val storage = FirebaseStorage.getInstance()
    private val homeworkImagesRef = storage.reference.child("homework_images")

    private suspend fun optimizeImage(context: Context, imageUri: Uri): ByteArray? {
        // Load image using Coil
        val request = ImageRequest.Builder(context)
            .data(imageUri)
            .allowHardware(false)
            .build()
        val result =
            (context.imageLoader.execute(request) as? SuccessResult)?.drawable ?: return null

        // Convert Drawable to Bitmap
        val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap ?: return null

        // Handle orientation with ExifInterface
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
        val exif = ExifInterface(inputStream)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        val orientedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Compress to WebP with max 100 KiB
        var quality = 80
        var output: ByteArray
        do {
            val stream = ByteArrayOutputStream()
            orientedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, stream)
            output = stream.toByteArray()
            quality -= 5
        } while (output.size > 51200 && quality > 10)

        return output
    }

    suspend fun uploadImage(imageUri: Uri, homeworkId: String): Result<String> {
        return try {
            val optimizedBytes = optimizeImage(context, imageUri)
                ?: return Result.failure(Exception("Failed to optimize image"))

            val fileName = "${homeworkId}_${UUID.randomUUID()}.webp"
            val imageRef = homeworkImagesRef.child(fileName)

            imageRef.putBytes(optimizedBytes).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("ImageUpload", "Error uploading image", e)
            Result.failure(e)
        }
    }

    suspend fun uploadImages(imageUris: List<Uri>, homeworkId: String): Result<List<String>> =
        kotlinx.coroutines.coroutineScope {
            try {
                val deferredResults = imageUris.map { imageUri ->
                    async {
                        uploadImage(imageUri, homeworkId)
                    }
                }
                val results = deferredResults.awaitAll()
                val uploadedUrls = results.map {
                    it.getOrElse { e -> throw e }
                }
                Result.success(uploadedUrls)
            } catch (e: Exception) {
                Log.e("ImageUpload", "Error uploading images concurrently", e)
                Result.failure(e)
            }
        }


    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ImageDelete", "Error deleting image", e)
            Result.failure(e)
        }
    }

    suspend fun deleteImages(imageUrls: List<String>): Result<Unit> {
        return try {
            for (imageUrl in imageUrls) {
                deleteImage(imageUrl)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ImageDelete", "Error deleting images", e)
            Result.failure(e)
        }
    }
}

