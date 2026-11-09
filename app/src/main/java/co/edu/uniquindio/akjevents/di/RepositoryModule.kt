package co.edu.uniquindio.akjevents.di

import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryNotificationRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRanker
import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRepository
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRanker
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Le indica a Hilt qué implementación usar para cada interfaz del dominio.
 * En la fase 3 basta con cambiar estas implementaciones en memoria por las remotas.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: InMemoryEventRepository): EventRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: InMemoryUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: InMemoryNotificationRepository): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(impl: LocalRecommendationRepository): RecommendationRepository

    @Binds
    abstract fun bindRecommendationRanker(impl: LocalRecommendationRanker): RecommendationRanker
}
