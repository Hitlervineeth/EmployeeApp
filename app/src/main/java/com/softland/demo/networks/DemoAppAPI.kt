package com.softland.demo.networks

import com.google.gson.GsonBuilder
import com.softland.demo.responses.GetAllEmployeeDetails
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface DemoAppAPI {

    companion object{
        private val retrofit: Retrofit? = null

        fun getRetrofit():Retrofit{
            if(retrofit ==null) {
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
                    .baseUrl("http://www.mocky.io/v2/")
                    .client(okHttpClient)
                    .build()
            }
            return retrofit;
        }

        operator fun invoke(): DemoAppAPI {
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
                .baseUrl("http://www.mocky.io/v2/")
                .client(okHttpClient)
                .build()
                .create(DemoAppAPI::class.java)
        }

    }


    @GET("5d565297300000680030a986")
    fun getAllEmployees(): Call<GetAllEmployeeDetails>


}