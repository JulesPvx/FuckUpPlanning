package fr.uptrash.fuckupplanning.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uptrash.fuckupplanning.data.network.ApiError
import fr.uptrash.fuckupplanning.data.network.ApiResult
import fr.uptrash.fuckupplanning.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for login screen
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val username: String = "",
    val password: String = ""
)

/**
 * ViewModel for login functionality using MVVM pattern.
 * Handles authentication operations and UI state management.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "LoginViewModel"
    }
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        Log.d(TAG, "LoginViewModel initialized")
        validateExistingSession()
    }
    
    /**
     * Validates existing session on app start
     */
    private fun validateExistingSession() {
        Log.d(TAG, "Validating existing session")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            authRepository.validateSession()
                .onSuccess {
                    Log.d(TAG, "Session validation successful")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null
                    )
                }
                .onError { error ->
                    Log.d(TAG, "Session validation failed: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = null // Don't show error for session validation failure
                    )
                }
        }
    }
    
    /**
     * Updates username in the UI state
     */
    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }
    
    /**
     * Updates password in the UI state
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    /**
     * Attempts to log in with provided credentials
     */
    fun login() {
        val currentState = _uiState.value
        
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter both username and password"
            )
            return
        }
        
        Log.d(TAG, "Attempting login for user: ${currentState.username}")
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )
            
            authRepository.login(currentState.username, currentState.password)
                .onSuccess {
                    Log.d(TAG, "Login successful")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null,
                        password = "" // Clear password for security
                    )
                }
                .onError { error ->
                    Log.w(TAG, "Login failed: $error")
                    val errorMessage = getErrorMessage(error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = errorMessage,
                        password = "" // Clear password on error
                    )
                }
        }
    }
    
    /**
     * Logs out the current user
     */
    fun logout() {
        Log.d(TAG, "Logging out user")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            authRepository.logout()
                .onSuccess {
                    Log.d(TAG, "Logout successful")
                    _uiState.value = LoginUiState() // Reset to initial state
                }
                .onError { error ->
                    Log.w(TAG, "Logout failed: $error")
                    // Even if logout fails, consider user logged out locally
                    _uiState.value = LoginUiState(
                        errorMessage = "Logout completed locally"
                    )
                }
        }
    }
    
    /**
     * Clears the current error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Converts ApiError to user-friendly message
     */
    private fun getErrorMessage(error: ApiError): String {
        return when (error) {
            is ApiError.NetworkError -> "Network error. Please check your connection."
            is ApiError.SessionExpired -> "Session expired. Please log in again."
            is ApiError.ServerError -> "Server error. Please try again later."
            is ApiError.BadRequest -> "Invalid credentials. Please check your username and password."
            is ApiError.NotFound -> "Service not found. Please try again later."
            is ApiError.Forbidden -> "Access denied. Please check your credentials."
            is ApiError.UnknownError -> "An unexpected error occurred: ${error.message}"
        }
    }
}