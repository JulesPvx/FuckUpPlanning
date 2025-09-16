package fr.uptrash.fuckupplanning.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.uptrash.fuckupplanning.data.repository.HomeworkRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHomeworkRepository(): HomeworkRepository {
        return HomeworkRepository()
    }
}
