package com.test.weatherlogger.net

import com.test.weatherlogger.BuildConfig
import com.test.weatherlogger.utils.Constants.APP_ID
import com.test.weatherlogger.utils.Constants.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RestApi {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val originalHttpUrl=original.url()
            val url=originalHttpUrl.newBuilder()
                .addQueryParameter("APPID",APP_ID)
                .build()
            val requestBuilder = original.newBuilder()
                .method(original.method(), original.body())
            val request = requestBuilder
                .url(url)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: IRestApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        retrofit.create(IRestApi::class.java)
    }

}