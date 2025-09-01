package fr.uptrash.fuckupplanning.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.uptrash.fuckupplanning.data.network.ApiService
import fr.uptrash.fuckupplanning.data.network.CookieManager
import fr.uptrash.fuckupplanning.data.network.ErrorHandlingInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Network module for dependency injection using Hilt.
 * Provides network-related dependencies including Retrofit, OkHttp, and API services.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val TAG = "NetworkModule"
    private const val BASE_URL = "https://upplanning.appli.univ-poitiers.fr/"
    
    /**
     * Provides HTTP logging interceptor for debugging network requests
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        Log.d(TAG, "Creating HTTP logging interceptor")
        return HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    /**
     * Provides cookie manager for session handling
     */
    @Provides
    @Singleton
    fun provideCookieManager(@ApplicationContext context: Context): CookieManager {
        Log.d(TAG, "Creating cookie manager")
        return CookieManager(context)
    }
    
    /**
     * Provides error handling interceptor
     */
    @Provides
    @Singleton
    fun provideErrorHandlingInterceptor(): ErrorHandlingInterceptor {
        Log.d(TAG, "Creating error handling interceptor")
        return ErrorHandlingInterceptor()
    }
    
    /**
     * Provides configured OkHttpClient with cookie management, logging, and error handling
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        cookieManager: CookieManager,
        loggingInterceptor: HttpLoggingInterceptor,
        errorHandlingInterceptor: ErrorHandlingInterceptor
    ): OkHttpClient {
        Log.d(TAG, "Creating OkHttpClient with base URL: $BASE_URL")
        
        return OkHttpClient.Builder()
            .cookieJar(cookieManager)
            .addInterceptor(errorHandlingInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    /**
     * Provides Retrofit instance configured for UPLanning API
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        Log.d(TAG, "Creating Retrofit instance for base URL: $BASE_URL")
        
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Provides API service implementation
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        Log.d(TAG, "Creating API service")
        return retrofit.create(ApiService::class.java)
    }
}