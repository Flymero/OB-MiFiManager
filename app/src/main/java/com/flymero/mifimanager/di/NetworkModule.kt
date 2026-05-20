package com.flymero.mifimanager.di

import android.content.Context
import com.flymero.mifimanager.BuildConfig
import com.flymero.mifimanager.data.api.DigestAuthInterceptor
import com.flymero.mifimanager.data.api.MiFiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://192.168.1.1/"

    @Provides
    @Singleton
    fun provideDigestAuthInterceptor(@ApplicationContext context: Context): DigestAuthInterceptor {
        val interceptor = DigestAuthInterceptor()
        val prefs = context.getSharedPreferences("mifi_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("remember", false)) {
            val username = prefs.getString("username", "") ?: ""
            val password = prefs.getString("password", "") ?: ""
            if (username.isNotEmpty() && password.isNotEmpty()) {
                interceptor.updateCredentials(username, password)
            }
        }
        return interceptor
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(digestAuthInterceptor: DigestAuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(digestAuthInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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
    fun provideMiFiApi(retrofit: Retrofit): MiFiApi {
        return retrofit.create(MiFiApi::class.java)
    }
}
