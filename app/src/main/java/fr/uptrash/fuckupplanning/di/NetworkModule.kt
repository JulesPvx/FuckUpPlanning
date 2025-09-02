package fr.uptrash.fuckupplanning.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.uptrash.fuckupplanning.data.network.ApiService
import fr.uptrash.fuckupplanning.data.network.ErrorHandlingInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
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
        return HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Provides error handling interceptor
     */
    @Provides
    @Singleton
    fun provideErrorHandlingInterceptor(): ErrorHandlingInterceptor {
        return ErrorHandlingInterceptor()
    }

    /**
     * Provides configured OkHttpClient with cookie management, logging, and error handling
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        errorHandlingInterceptor: ErrorHandlingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
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
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    /**
     * Provides API service implementation
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}