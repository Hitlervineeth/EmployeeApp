package com.softland.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.softland.demo.database.DemoAppDB
import com.softland.demo.networks.DemoAppAPI
import com.softland.demo.responses.GetAllEmployeeDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private val db by lazy { DemoAppDB(this@MainActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        getAllEmployees()
    }


    private fun getAllEmployees( ) {


        CoroutineScope(Dispatchers.Main).launch {
            DemoAppAPI.getRetrofit().create(DemoAppAPI::class.java)
                .getAllEmployees()
                .enqueue(object : Callback<GetAllEmployeeDetails> {
                    override fun onResponse(
                        call: Call<GetAllEmployeeDetails>,
                        response: Response<GetAllEmployeeDetails>
                    ) {
                        if (response.isSuccessful) {
                            db.employeeDetailsDao().clear()
                            Log.d("demo", response.body().toString())
                            response.body()?.let {
                                db.employeeDetailsDao().saveAll(response.body())

                                val intent = Intent(this@MainActivity, EmployeeListingUI::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent)
                                finish()

                            }

                        }
                    }

                    override fun onFailure(call: Call<GetAllEmployeeDetails>, t: Throwable) {

                        val intent = Intent(this@MainActivity, EmployeeListingUI::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent)
                        finish()
                        Log.d("demo","override fun onFailure(call: Call<GetAllEmployeeDetails>, t: Throwable) {")
                    }
                })
        }
    }

}