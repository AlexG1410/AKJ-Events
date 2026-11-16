package co.edu.uniquindio.akjevents.di

import co.edu.uniquindio.akjevents.BuildConfig
import co.edu.uniquindio.akjevents.data.remote.ai.AiServiceConfig
import co.edu.uniquindio.akjevents.data.remote.ai.AiSessionTokenProvider
import co.edu.uniquindio.akjevents.data.remote.ai.CategorySuggestionApi
import co.edu.uniquindio.akjevents.data.remote.ai.FirebaseAiSessionTokenProvider
import co.edu.uniquindio.akjevents.data.remote.ai.RecommendationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.inject.Named
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Cliente separado de Cloudinary: nunca envía el token de Firebase a ese proveedor. */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Provides
    @Singleton
    fun provideAiConfig() = AiServiceConfig(BuildConfig.AI_BASE_URL)

    @Provides
    fun provideAiTokens(provider: FirebaseAiSessionTokenProvider): AiSessionTokenProvider = provider

    @Provides
    @Singleton
    @Named("ai")
    fun provideAiRetrofit(config: AiServiceConfig): Retrofit = Retrofit.Builder()
        // Sin configuración el repositorio falla antes de hacer red. Debug sigue compilando.
        .baseUrl(config.baseUrl?.toString() ?: "https://ai-not-configured.invalid/")
        .client(OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(18, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .retryOnConnectionFailure(false)
            .build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    fun provideCategorySuggestionApi(@Named("ai") retrofit: Retrofit): CategorySuggestionApi = retrofit.create(CategorySuggestionApi::class.java)

    @Provides
    fun provideRecommendationApi(@Named("ai") retrofit: Retrofit): RecommendationApi = retrofit.create(RecommendationApi::class.java)
}
