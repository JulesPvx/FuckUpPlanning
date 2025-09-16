package fr.uptrash.fuckupplanning.data.repository

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
    private val karmaRepository: KarmaRepository
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

    suspend fun deleteHomework(homeworkId: String): Result<Unit> {
        return try {
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
            var ownerKarmaChange = 0

            if (isUpvote) {
                if (hasUpvoted) {
                    // Remove upvote
                    currentUpvotes.remove(userId)
                    karmaChange = -1
                    ownerKarmaChange = -1
                } else {
                    // Add upvote (remove downvote if exists)
                    if (hasDownvoted) {
                        currentDownvotes.remove(userId)
                        karmaChange = 2 // Remove -1 and add +1
                        ownerKarmaChange = 2
                    } else {
                        karmaChange = 1
                        ownerKarmaChange = 1
                    }
                    currentUpvotes[userId] = true
                }
            } else {
                if (hasDownvoted) {
                    // Remove downvote
                    currentDownvotes.remove(userId)
                    karmaChange = 1
                    ownerKarmaChange = 1
                } else {
                    // Add downvote (remove upvote if exists)
                    if (hasUpvoted) {
                        currentUpvotes.remove(userId)
                        karmaChange = -2 // Remove +1 and add -1
                        ownerKarmaChange = -2
                    } else {
                        karmaChange = -1
                        ownerKarmaChange = -1
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
            if (ownerKarmaChange != 0 && homework.ownerId != "Unknown") {
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

    fun getUserVoteStatus(homeworkId: String, userId: String): Flow<Pair<Boolean, Boolean>> =
        callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val homework = snapshot.getValue(Homework::class.java)
                    val hasUpvoted = homework?.upvotes?.get(userId) == true
                    val hasDownvoted = homework?.downvotes?.get(userId) == true
                    trySend(Pair(hasUpvoted, hasDownvoted))
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            homeworkRef.child(homeworkId).addValueEventListener(listener)
            awaitClose { homeworkRef.child(homeworkId).removeEventListener(listener) }
        }
}
