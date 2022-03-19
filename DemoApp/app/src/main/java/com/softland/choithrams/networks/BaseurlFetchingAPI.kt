package com.softland.choithrams.networks

import com.google.gson.GsonBuilder
import com.softland.choithrams.retrofitresponses.ServerUrl
import com.softland.choithrams.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface BaseurlFetchingAPI {

    companion object{
        private val retrofit: Retrofit? = null

        fun getRetrofit():Retrofit{
            if(retrofit==null) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .build()
                val gson = GsonBuilder().setLenient().create()
                return Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl(Constants.base_url)
                    .client(okHttpClient)
                    .build()
            }
            return retrofit;
        }

        operator fun invoke(): BaseurlFetchingAPI{
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build()
            val gson = GsonBuilder().setLenient().create()
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(Constants.base_url)
                .client(okHttpClient)
                .build()
                .create(BaseurlFetchingAPI::class.java)
        }

    }


    @GET("GetServiceUrlForDevice")
    fun getUrlapi(@Query("composite") data:String): Call<ServerUrl>


}