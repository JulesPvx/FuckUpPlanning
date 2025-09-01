# UPLanning API Implementation

This implementation provides a complete API infrastructure for the UPLanning application using modern Android development best practices.

## Architecture Overview

The implementation follows the MVVM (Model-View-ViewModel) pattern with the following components:

### Network Layer
- **ApiService**: Retrofit interface defining the API endpoints for UPLanning
- **CookieManager**: Persistent cookie management using SharedPreferences for GWT session handling
- **ErrorHandlingInterceptor**: Centralized error handling with comprehensive logging
- **NetworkModule**: Hilt dependency injection configuration for network components

### Repository Pattern
- **AuthRepository**: Interface and implementation for authentication operations
- **ApiResult**: Sealed class for handling API responses with success/error states
- **ApiError**: Enumeration of different error types for UI feedback

### MVVM Implementation
- **LoginViewModel**: Complete state management for login functionality
- **LoginScreen**: Material Design 3 UI with proper form validation
- **LoginUiState**: Data class representing the UI state

## Key Features

### 1. Cookie Management
The `CookieManager` class provides persistent cookie storage essential for GWT-based applications:
- Automatic cookie saving and loading
- Expiration handling
- Host-based cookie storage
- SharedPreferences persistence

### 2. Error Handling
Comprehensive error handling throughout the application:
- Network error detection
- HTTP status code mapping
- User-friendly error messages
- Centralized logging

### 3. Dependency Injection
Complete Hilt setup for dependency injection:
- Network module for API configuration
- Repository module for business logic
- Proper scoping and lifecycle management

### 4. Coroutines Integration
Full coroutine support for asynchronous operations:
- Structured concurrency
- Proper error propagation
- UI thread safety

### 5. Logging
Comprehensive logging throughout all components:
- Network request/response logging
- Error logging with stack traces
- Debug information for development

## API Configuration

Base URL: `https://upplanning.appli.univ-poitiers.fr/`

The API service is configured with:
- 30-second timeouts
- Retry on connection failure
- Cookie-based session management
- GSON serialization
- Comprehensive logging

## Testing

Unit tests are included for:
- Error handling logic
- API result mapping
- HTTP status code conversion

## Usage

The implementation is ready for integration with actual API endpoints. The current placeholder endpoints in `ApiService` should be replaced with the actual UPLanning API specification.

## Security Considerations

- Cookies are stored securely using SharedPreferences
- Passwords are cleared from memory after use
- Network traffic logging can be disabled in production
- Proper SSL/TLS configuration with OkHttp