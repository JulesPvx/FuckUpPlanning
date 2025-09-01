package fr.uptrash.fuckupplanning.di

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.uptrash.fuckupplanning.data.repository.AuthRepository
import fr.uptrash.fuckupplanning.data.repository.AuthRepositoryImpl
import javax.inject.Singleton

/**
 * Repository module for dependency injection.
 * Binds repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    companion object {
        private const val TAG = "RepositoryModule"
    }
    
    /**
     * Binds AuthRepository interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}