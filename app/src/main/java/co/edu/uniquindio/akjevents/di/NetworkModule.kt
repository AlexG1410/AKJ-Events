package co.edu.uniquindio.akjevents.di

import co.edu.uniquindio.akjevents.data.remote.image.CloudinaryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/** Retrofit para la API de Cloudinary (guía 18). Se usa @Provides porque Retrofit se arma con un builder. */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val CLOUDINARY_BASE_URL = "https://api.cloudinary.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        // Subir una foto con mala señal puede tardar más que una consulta normal
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(CLOUDINARY_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideCloudinaryApi(retrofit: Retrofit): CloudinaryApi = retrofit.create(CloudinaryApi::class.java)
}
