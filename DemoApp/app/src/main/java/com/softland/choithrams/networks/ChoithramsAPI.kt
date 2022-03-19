package com.softland.choithrams.networks

import com.google.gson.GsonBuilder
import com.softland.choithrams.requestobjects.*
import com.softland.choithrams.retrofitresponses.*
import com.softland.choithrams.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import retrofit2.http.Body
import retrofit2.http.POST


interface ChoithramsAPI {

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
                 //   .baseUrl("http://202.88.237.210/QASFACHOITHRAM_BUSINESS/api/StorePickerController/")
                    .baseUrl(Constants.choithrams_base_url)
                    .client(okHttpClient)
                    .build()
            }
            return retrofit;
        }

        operator fun invoke(): ChoithramsAPI{
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
                .baseUrl(Constants.choithrams_base_url)
                .client(okHttpClient)
                .build()
                .create(ChoithramsAPI::class.java)
        }
    }

    @POST("LoginActivity")
    fun getLogin(@Body  loginActivityRequest: LoginActivityRequest): Call<LoginActivity>

    @POST("GetAllSections")
    fun getAllSections(@Body getAllSectionsRequest: GetAllSectionsRequest): Call<GetAllSections>

    @POST("GetAllRules")
    fun getAllRules(@Body getAllRulesRequest: GetAllRulesRequest): Call<GetAllRules>

    @POST("GetAllStockItems")
    fun getAllStockItems(@Body getAllStockItemsRequest: GetAllStockItemsRequest): Call<GetAllStockItems>

    @POST("SaveDayOpen")
    fun saveDayOpen(@Body saveDayOpenRequest: SaveDayOpenRequest): Call<SaveDayOpen>

    @POST("SaveDayClose")
    fun saveDayClose(@Body saveDayCloseRequest: SaveDayCloseRequest): Call<SaveDayOpen>

    @POST("LogOut")
    fun logOut(@Body logOutRequest: LogOutRequest): Call<LogOut>

    @POST("GetAllSalesRates")
    fun getAllSalesRates(@Body getAllSalesRatesRequest: GetAllSalesRatesRequest): Call<GetAllSalesRates>

    @POST("InsertJobDetails")
    fun insertJobDetails(@Body insertJobRequest: InsertJobRequest): Call<InsertJobDetails>

    @POST("ApproveJobDetails")
    fun approveJobDetails(@Body approveJobDetailsRequest: ApproveJobDetailsRequest): Call<InsertJobDetails>


    @POST("GetAllSavedJobs")
    fun getAllSavedJobs(@Body getAllSectionsRequest: GetAllSectionsRequest): Call<GetAllSavedJobsResponse>

    @POST("GetSavedJobDetails")
    fun getSavedJobDetails(@Body getSavedJobDetailsRequest: GetSavedJobDetailsRequest): Call<GetSavedJobDetails>

    @POST("GetJobDetailsByStoreManager")
    fun getJobDetailsByStoreManager(@Body getSavedJobDetailsRequest: GetSavedJobDetailsRequest): Call<GetJobDetailsByStoreManager>


    @POST("GetAllJobsByStoreManager")
    fun getAllJobsByStoreManager(@Body getAllJobsByStoreManagerRequest: GetAllJobsByStoreManagerRequest): Call<GetAllJobsByStoreManager>




}