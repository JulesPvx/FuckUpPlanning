package fr.uptrash.fuckupplanning.data.repository

import android.util.Log
import fr.uptrash.fuckupplanning.data.network.ApiError
import fr.uptrash.fuckupplanning.data.network.ApiResult
import fr.uptrash.fuckupplanning.data.network.ApiService
import fr.uptrash.fuckupplanning.data.network.NetworkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    suspend fun login(username: String, password: String): ApiResult<Unit>
    suspend fun validateSession(): ApiResult<Unit>
    suspend fun logout(): ApiResult<Unit>
}

/**
 * Implementation of AuthRepository using the API service.
 * Handles authentication operations with proper error handling and logging.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    override suspend fun login(username: String, password: String): ApiResult<Unit> {
        Log.d(TAG, "Attempting login for user: $username")
        
        return safeApiCall {
            val credentials = mapOf(
                "username" to username,
                "password" to password
            )
            apiService.login(credentials)
        }
    }
    
    override suspend fun validateSession(): ApiResult<Unit> {
        Log.d(TAG, "Validating current session")
        
        return safeApiCall {
            apiService.validateSession()
        }
    }
    
    override suspend fun logout(): ApiResult<Unit> {
        Log.d(TAG, "Logging out user")
        
        return safeApiCall {
            apiService.logout()
        }
    }
    
    /**
     * Safe API call wrapper that handles exceptions and converts them to ApiResult
     */
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): ApiResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Making API call")
                val response = apiCall()
                
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "API call successful")
                    ApiResult.Success(body!!)
                } else {
                    val errorCode = response.code()
                    val errorMessage = response.message()
                    Log.w(TAG, "API call failed with code: $errorCode, message: $errorMessage")
                    
                    val apiError = ApiError.fromHttpCode(errorCode)
                    ApiResult.Error(apiError)
                }
            } catch (e: NetworkException) {
                Log.e(TAG, "Network error during API call", e)
                ApiResult.Error(ApiError.NetworkError)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during API call", e)
                ApiResult.Error(ApiError.UnknownError(0, e.message ?: "Unknown error"))
            }
        }
    }
}