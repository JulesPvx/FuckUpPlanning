package fr.uptrash.fuckupplanning.data.network

import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for UPLanning application
 * Base URL: https://upplanning.appli.univ-poitiers.fr/
 * 
 * This interface defines the contract for API communication.
 * Actual endpoints will be implemented based on specific requirements.
 */
interface ApiService {
    
    /**
     * Placeholder for login endpoint
     * This will be implemented based on actual API documentation
     */
    @POST("login")
    suspend fun login(@Body credentials: Map<String, String>): Response<Unit>
    
    /**
     * Placeholder for session validation
     * This will be implemented based on actual API documentation
     */
    @GET("session/validate")
    suspend fun validateSession(): Response<Unit>
    
    /**
     * Placeholder for logout endpoint
     * This will be implemented based on actual API documentation
     */
    @POST("logout")
    suspend fun logout(): Response<Unit>
}