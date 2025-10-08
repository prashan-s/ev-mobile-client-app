package lk.chargehere.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lk.chargehere.app.auth.TokenManager
import lk.chargehere.app.data.remote.api.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // HTTP URL for testing purposes - clear text traffic enabled
    // TODO: Change to HTTPS for production
    private const val BASE_URL = "https://ev.dulanga.com/backend/"
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getAccessToken()

            // Debug logging
            android.util.Log.d("AuthInterceptor", "Request URL: ${originalRequest.url}")
            android.util.Log.d("AuthInterceptor", "Token exists: ${token != null}")
            if (token != null) {
                android.util.Log.d("AuthInterceptor", "Token preview: ${token.take(50)}...")
            }

            val newRequest = if (token != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                android.util.Log.w("AuthInterceptor", "No token found - request will be sent without Authorization header")
                originalRequest
            }

            val response = chain.proceed(newRequest)

            // Handle 401 Unauthorized - token refresh logic would go here
            if (response.code == 401) {
                android.util.Log.e("AuthInterceptor", "Received 401 Unauthorized for ${originalRequest.url}")
                // In a real implementation, you would refresh the token here
                // and retry the request
            }

            response
        }
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideStationApiService(retrofit: Retrofit): StationApiService {
        return retrofit.create(StationApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideReservationApiService(retrofit: Retrofit): ReservationApiService {
        return retrofit.create(ReservationApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideOperatorApiService(retrofit: Retrofit): OperatorApiService {
        return retrofit.create(OperatorApiService::class.java)
    }
}