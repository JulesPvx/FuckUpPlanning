package fr.uptrash.fuckupplanning.data.repository

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.uptrash.fuckupplanning.data.model.Homework
import fr.uptrash.fuckupplanning.data.model.KarmaTransaction
import fr.uptrash.fuckupplanning.data.model.KarmaTransactionType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeworkRepository @Inject constructor(
    private val userRepository: UserRepository,
    private val karmaRepository: KarmaRepository,
    private val imageStorageRepository: ImageStorageRepository
) {
    private val database = FirebaseDatabase.getInstance()
    private val homeworkRef = database.getReference("homework")

    fun getAllHomework(): Flow<List<Homework>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val homeworkList = mutableListOf<Homework>()
                for (child in snapshot.children) {
                    child.getValue(Homework::class.java)?.let { homework ->
                        homeworkList.add(homework.copy(id = child.key ?: ""))
                    }
                }
                trySend(homeworkList.sortedBy { it.dueDate })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        homeworkRef.addValueEventListener(listener)
        awaitClose { homeworkRef.removeEventListener(listener) }
    }

    suspend fun addHomework(homework: Homework): Result<String> {
        return try {
            val key = homeworkRef.push().key ?: throw Exception("Failed to generate key")
            homeworkRef.child(key).setValue(homework.copy(id = key)).await()
            Result.success(key)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHomework(homework: Homework): Result<Unit> {
        return try {
            homeworkRef.child(homework.id).setValue(homework).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addHomeworkWithImages(homework: Homework, imageUris: List<Uri>): Result<String> {
        return try {
            val key = homeworkRef.push().key ?: throw Exception("Failed to generate key")

            // Upload images if any
            val imageUrls = if (imageUris.isNotEmpty()) {
                val uploadResult = imageStorageRepository.uploadImages(imageUris, key)
                if (uploadResult.isFailure) {
                    return Result.failure(
                        uploadResult.exceptionOrNull() ?: Exception("Failed to upload images")
                    )
                }
                uploadResult.getOrThrow()
            } else {
                emptyList()
            }

            // Create homework with image URLs
            val homeworkWithImages = homework.copy(id = key, imageUrls = imageUrls)
            homeworkRef.child(key).setValue(homeworkWithImages).await()

            Result.success(key)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHomeworkWithImages(
        homework: Homework,
        newImageUris: List<Uri> = emptyList()
    ): Result<Unit> {
        return try {
            // Upload new images if any
            val newImageUrls = if (newImageUris.isNotEmpty()) {
                val uploadResult = imageStorageRepository.uploadImages(newImageUris, homework.id)
                if (uploadResult.isFailure) {
                    return Result.failure(
                        uploadResult.exceptionOrNull() ?: Exception("Failed to upload images")
                    )
                }
                uploadResult.getOrThrow()
            } else {
                emptyList()
            }

            // Combine existing and new image URLs
            val allImageUrls = homework.imageUrls + newImageUrls
            val updatedHomework = homework.copy(imageUrls = allImageUrls)

            homeworkRef.child(homework.id).setValue(updatedHomework).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeImageFromHomework(homeworkId: String, imageUrl: String): Result<Unit> {
        return try {
            val snapshot = homeworkRef.child(homeworkId).get().await()
            val homework = snapshot.getValue(Homework::class.java)?.copy(id = homeworkId)
                ?: return Result.failure(Exception("Homework not found"))

            // Remove image from storage
            imageStorageRepository.deleteImage(imageUrl)

            // Update homework with removed image URL
            val updatedImageUrls = homework.imageUrls.filterNot { it == imageUrl }
            val updatedHomework = homework.copy(imageUrls = updatedImageUrls)

            homeworkRef.child(homeworkId).setValue(updatedHomework).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHomework(homeworkId: String): Result<Unit> {
        return try {
            // Get homework to delete associated images
            val snapshot = homeworkRef.child(homeworkId).get().await()
            val homework = snapshot.getValue(Homework::class.java)?.copy(id = homeworkId)

            // Delete associated images from storage
            homework?.imageUrls?.let { imageUrls ->
                if (imageUrls.isNotEmpty()) {
                    imageStorageRepository.deleteImages(imageUrls)
                }
            }

            // Delete homework from database
            homeworkRef.child(homeworkId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun voteOnHomework(
        homeworkId: String,
        userId: String,
        isUpvote: Boolean
    ): Result<Unit> {
        return try {
            val snapshot = homeworkRef.child(homeworkId).get().await()
            val homework = snapshot.getValue(Homework::class.java)?.copy(id = homeworkId)
                ?: return Result.failure(Exception("Homework not found"))

            val currentUpvotes = homework.upvotes.toMutableMap()
            val currentDownvotes = homework.downvotes.toMutableMap()

            // Check current vote status
            val hasUpvoted = currentUpvotes[userId] == true
            val hasDownvoted = currentDownvotes[userId] == true

            var karmaChange = 0

            if (isUpvote) {
                if (hasUpvoted) {
                    // Remove upvote
                    currentUpvotes.remove(userId)
                    karmaChange = -1
                } else {
                    // Add upvote (remove downvote if exists)
                    if (hasDownvoted) {
                        currentDownvotes.remove(userId)
                        karmaChange = 2 // Remove -1 and add +1
                    } else {
                        karmaChange = 1
                    }
                    currentUpvotes[userId] = true
                }
            } else {
                if (hasDownvoted) {
                    // Remove downvote
                    currentDownvotes.remove(userId)
                    karmaChange = 1
                } else {
                    // Add downvote (remove upvote if exists)
                    if (hasUpvoted) {
                        currentUpvotes.remove(userId)
                        karmaChange = -2 // Remove +1 and add -1
                    } else {
                        karmaChange = -1
                    }
                    currentDownvotes[userId] = true
                }
            }

            val newKarma = homework.karma + karmaChange
            val updatedHomework = homework.copy(
                karma = newKarma,
                upvotes = currentUpvotes,
                downvotes = currentDownvotes
            )

            // Update homework
            homeworkRef.child(homeworkId).setValue(updatedHomework).await()

            // Handle karma transactions
            if (homework.ownerId != "Unknown") {
                // Remove previous vote transaction if exists
                if (hasUpvoted || hasDownvoted) {
                    karmaRepository.removeVoteTransaction(homeworkId, userId, homework.ownerId)
                }

                // Add new karma transaction if user is voting (not removing vote)
                if ((isUpvote && !hasUpvoted) || (!isUpvote && !hasDownvoted)) {
                    val transaction = KarmaTransaction(
                        userId = homework.ownerId,
                        homeworkId = homeworkId,
                        karmaChange = if (isUpvote) 1 else -1,
                        type = KarmaTransactionType.VOTE,
                        voterUserId = userId
                    )
                    karmaRepository.addKarmaTransaction(transaction)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
