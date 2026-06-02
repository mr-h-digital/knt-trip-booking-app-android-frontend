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

    /** Shared OkHttpClient with trust-all SSL in debug — also used by Coil for image loading. */
    var httpClient: OkHttpClient = OkHttpClient()
        private set

    fun init(context: Context) {
        tokenManager = TokenManager(context)
        val baseClient = buildBaseClient()
        httpClient = baseClient
        retrofit   = buildRetrofit(tokenManager!!, baseClient)
    }

    val service: ApiService
        get() = retrofit?.create(ApiService::class.java)
            ?: error("ApiClient.init(context) must be called before using ApiClient.service")

    fun getTokenManager(): TokenManager =
        tokenManager ?: error("ApiClient.init(context) must be called first")

    /** Builds a base OkHttpClient with trust-all SSL in debug (no auth interceptor — used by Coil). */
    fun buildBaseClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val trustAll = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
            }
            builder.sslSocketFactory(sslContext.socketFactory, trustAll)
                   .hostnameVerifier { _, _ -> true }
        }
        return builder.build()
    }

    /**
     * Builds an OkHttpClient suitable for Coil image loading: trust-all SSL in debug
     * plus the auth interceptor so protected avatar URLs load correctly.
     * Called lazily after ApiClient.init() has been called.
     */
    fun buildCoilClient(): OkHttpClient =
        buildBaseClient().newBuilder()
            .addInterceptor(AuthInterceptor(
                tokenManager ?: error("ApiClient.init(context) must be called before image loading")
            ))
            .build()

    private fun buildRetrofit(tm: TokenManager, baseClient: OkHttpClient): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

        val client = baseClient.newBuilder()
            .addInterceptor(AuthInterceptor(tm))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.ACTIVE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
