package fr.uptrash.fuckupplanning.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val isUserLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            result.user?.let { user ->
                Result.success(user)
            } ?: Result.failure(Exception("Authentication failed: User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAuthState(): Flow<FirebaseUser?> = flow {
        firebaseAuth.addAuthStateListener { auth ->
            // This will be called whenever auth state changes
        }
        emit(firebaseAuth.currentUser)
    }

    fun getUserId(): String? = currentUser?.uid

    fun isAnonymousUser(): Boolean = currentUser?.isAnonymous == true
}
