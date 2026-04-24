package com.kntransport.app.network

import android.content.Context
import com.kntransport.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {

    private var retrofit: Retrofit? = null
    private var tokenManager: TokenManager? = null

    fun init(context: Context) {
        tokenManager = TokenManager(context)
        retrofit     = buildRetrofit(tokenManager!!)
    }

    val service: ApiService
        get() = retrofit?.create(ApiService::class.java)
            ?: error("ApiClient.init(context) must be called before using ApiClient.service")

    fun getTokenManager(): TokenManager =
        tokenManager ?: error("ApiClient.init(context) must be called first")

    private fun buildRetrofit(tm: TokenManager): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tm))
            .addInterceptor(logging)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)

        // Trust all certificates in debug builds to work around Railway's
        // certificate chain not being fully trusted by Android's CA store.
        // Production builds use the default strict SSL validation.
        if (BuildConfig.DEBUG) {
            val trustAll = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
            }
            clientBuilder
                .sslSocketFactory(sslContext.socketFactory, trustAll)
                .hostnameVerifier { _, _ -> true }
        }

        return Retrofit.Builder()
            .baseUrl(BuildConfig.ACTIVE_API_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
