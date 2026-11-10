package co.edu.uniquindio.akjevents.di

import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRanker
import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRepository
import co.edu.uniquindio.akjevents.data.repository.local.DataStoreSessionPreferences
import co.edu.uniquindio.akjevents.data.repository.remote.CloudinaryImageRepository
import co.edu.uniquindio.akjevents.data.repository.remote.AndroidGeocodingRepository
import co.edu.uniquindio.akjevents.domain.repository.GeocodingRepository
import co.edu.uniquindio.akjevents.data.repository.remote.FirebaseUserRepository
import co.edu.uniquindio.akjevents.data.repository.remote.FirestoreEventRepository
import co.edu.uniquindio.akjevents.data.repository.remote.FirestoreNotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.ImageRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRanker
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRepository
import co.edu.uniquindio.akjevents.domain.repository.SessionPreferencesRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Le indica a Hilt qué implementación usar para cada interfaz del dominio: Firebase para las cuentas,
 * los eventos y las notificaciones, Cloudinary para las fotos y DataStore para la sesión. Las
 * implementaciones en memoria quedan para las pruebas unitarias.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(impl: AndroidGeocodingRepository): GeocodingRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: FirestoreEventRepository): EventRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FirebaseUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindSessionPreferences(impl: DataStoreSessionPreferences): SessionPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: FirestoreNotificationRepository): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: CloudinaryImageRepository): ImageRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(impl: LocalRecommendationRepository): RecommendationRepository

    @Binds
    abstract fun bindRecommendationRanker(impl: LocalRecommendationRanker): RecommendationRanker
}
