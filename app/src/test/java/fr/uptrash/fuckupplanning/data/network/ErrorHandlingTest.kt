package fr.uptrash.fuckupplanning.data.network

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for error handling and API result types
 */
class ErrorHandlingTest {
    
    @Test
    fun testApiErrorFromHttpCode() {
        // Test various HTTP error codes
        assertEquals(ApiError.BadRequest, ApiError.fromHttpCode(400))
        assertEquals(ApiError.SessionExpired, ApiError.fromHttpCode(401))
        assertEquals(ApiError.Forbidden, ApiError.fromHttpCode(403))
        assertEquals(ApiError.NotFound, ApiError.fromHttpCode(404))
        assertEquals(ApiError.ServerError, ApiError.fromHttpCode(500))
        assertEquals(ApiError.ServerError, ApiError.fromHttpCode(503))
        
        // Test unknown error codes
        val unknownError = ApiError.fromHttpCode(418) as ApiError.UnknownError
        assertEquals(418, unknownError.code)
    }
    
    @Test
    fun testApiResultSuccess() {
        val result = ApiResult.Success("test data")
        
        var successCalled = false
        var errorCalled = false
        
        result.onSuccess { data ->
            successCalled = true
            assertEquals("test data", data)
        }.onError {
            errorCalled = true
        }
        
        assertTrue(successCalled)
        assertFalse(errorCalled)
    }
    
    @Test
    fun testApiResultError() {
        val result = ApiResult.Error(ApiError.NetworkError)
        
        var successCalled = false
        var errorCalled = false
        
        result.onSuccess {
            successCalled = true
        }.onError { error ->
            errorCalled = true
            assertEquals(ApiError.NetworkError, error)
        }
        
        assertFalse(successCalled)
        assertTrue(errorCalled)
    }
    
    @Test
    fun testApiResultMap() {
        val successResult = ApiResult.Success(5)
        val mappedResult = successResult.map { it * 2 }
        
        assertTrue(mappedResult is ApiResult.Success)
        assertEquals(10, (mappedResult as ApiResult.Success).data)
        
        val errorResult = ApiResult.Error(ApiError.NetworkError)
        val mappedErrorResult = errorResult.map { "should not be called" }
        
        assertTrue(mappedErrorResult is ApiResult.Error)
        assertEquals(ApiError.NetworkError, (mappedErrorResult as ApiResult.Error).error)
    }
}