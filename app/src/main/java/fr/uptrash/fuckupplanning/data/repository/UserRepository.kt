package fr.uptrash.fuckupplanning.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.uptrash.fuckupplanning.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    suspend fun createOrUpdateUser(user: User): Result<Unit> {
        return try {
            usersRef.child(user.id).setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            val user = snapshot.getValue(User::class.java)?.copy(id = userId)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserKarma(userId: String, karmaChange: Int): Result<Any?> {
        return try {
            val userResult = getUserById(userId)
            if (userResult.isSuccess) {
                val currentUser = userResult.getOrNull()
                if (currentUser != null) {
                    val updatedUser =
                        currentUser.copy(totalKarma = currentUser.totalKarma + karmaChange)
                    usersRef.child(userId).setValue(updatedUser).await()
                } else {
                    // Create new user if doesn't exist
                    val newUser = User(id = userId, totalKarma = karmaChange)
                    usersRef.child(userId).setValue(newUser).await()
                }
                Result.success(Unit)
            } else {
                userResult
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)?.copy(id = userId)
                trySend(user)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        usersRef.child(userId).addValueEventListener(listener)
        awaitClose { usersRef.child(userId).removeEventListener(listener) }
    }
}
