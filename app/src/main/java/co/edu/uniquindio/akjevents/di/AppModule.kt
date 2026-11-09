package co.edu.uniquindio.akjevents.di

import co.edu.uniquindio.akjevents.core.util.ResourceProvider
import co.edu.uniquindio.akjevents.core.util.ResourceProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Dependencias generales de la app que no son repositorios. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindResourceProvider(resourceProviderImpl: ResourceProviderImpl): ResourceProvider
}
