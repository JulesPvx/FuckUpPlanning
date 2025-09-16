package fr.uptrash.fuckupplanning.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.uptrash.fuckupplanning.data.model.Homework
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeworkRepository @Inject constructor() {
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

    suspend fun toggleHomeworkCompletion(homeworkId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            homeworkRef.child(homeworkId).child("completed").setValue(isCompleted).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
