package com.example.anipet_capstone.network

import com.example.anipet_capstone.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Read base URL from BuildConfig so different machines can override it in Gradle
    private val BASE_URL: String = BuildConfig.API_BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Default OkHttp timeouts are 10s each, which is too short for endpoints like
    // apply_adoption.php that upload an ID document plus house photos — on real
    // mobile connections the request was timing out client-side before the server
    // could finish saving the files and responding.
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            // Scalars must be registered before Gson: without it, plain String
            // @Part fields on @Multipart calls (e.g. apply_adoption.php's pet_id,
            // user_id, terms_accepted) get serialized by Gson as JSON — wrapping
            // them in quotes ("3" instead of 3) — which then breaks server-side
            // integer casts (bind_param('i', ...) on a quoted string casts to 0).
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}