package fr.uptrash.fuckupplanning.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Error handling interceptor for API requests.
 * Provides centralized error handling and logging for all network requests.
 */
@Singleton
class ErrorHandlingInterceptor @Inject constructor() : Interceptor {
    
    companion object {
        private const val TAG = "ErrorHandlingInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.d(TAG, "Making request to: ${request.url}")
        
        return try {
            val response = chain.proceed(request)
            
            // Log response details
            Log.d(TAG, "Response for ${request.url}: ${response.code} ${response.message}")
            
            when (response.code) {
                in 200..299 -> {
                    Log.d(TAG, "Request successful: ${request.url}")
                    response
                }
                400 -> {
                    Log.w(TAG, "Bad Request (400) for ${request.url}")
                    response
                }
                401 -> {
                    Log.w(TAG, "Unauthorized (401) for ${request.url} - Session may have expired")
                    response
                }
                403 -> {
                    Log.w(TAG, "Forbidden (403) for ${request.url}")
                    response
                }
                404 -> {
                    Log.w(TAG, "Not Found (404) for ${request.url}")
                    response
                }
                in 500..599 -> {
                    Log.e(TAG, "Server Error (${response.code}) for ${request.url}")
                    response
                }
                else -> {
                    Log.w(TAG, "Unexpected response code (${response.code}) for ${request.url}")
                    response
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error for ${request.url}", e)
            throw NetworkException("Network error: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error for ${request.url}", e)
            throw ApiException("Unexpected error: ${e.message}", e)
        }
    }
}

/**
 * Custom exception for network-related errors
 */
class NetworkException(message: String, cause: Throwable? = null) : IOException(message, cause)

/**
 * Custom exception for API-related errors
 */
class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Sealed class representing different types of API errors for UI feedback
 */
sealed class ApiError {
    object NetworkError : ApiError()
    object SessionExpired : ApiError()
    object ServerError : ApiError()
    object BadRequest : ApiError()
    object NotFound : ApiError()
    object Forbidden : ApiError()
    data class UnknownError(val code: Int, val message: String) : ApiError()
    
    companion object {
        fun fromHttpCode(code: Int): ApiError {
            return when (code) {
                400 -> BadRequest
                401 -> SessionExpired
                403 -> Forbidden
                404 -> NotFound
                in 500..599 -> ServerError
                else -> UnknownError(code, "Unknown error occurred")
            }
        }
    }
}

/**
 * Result wrapper for API responses
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val error: ApiError) : ApiResult<Nothing>()
    
    inline fun <R> map(transform: (T) -> R): ApiResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }
    
    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (ApiError) -> Unit): ApiResult<T> {
        if (this is Error) action(error)
        return this
    }
}