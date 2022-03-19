package com.softland.choithrams.activitys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.softland.choithrams.BuildConfig
import com.softland.choithrams.R
import com.softland.choithrams.networks.BaseurlFetchingAPI
import com.softland.choithrams.retrofitresponses.ServerUrl
import com.softland.choithrams.utils.ChoithramUtils
import com.softland.choithrams.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class SplashScreenActivity : AppCompatActivity() {
    private var ServiceCounter: Int = 0
    val TAG = "SplashScreenActivity"
    lateinit var text_fetching_url: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        text_fetching_url=findViewById<TextView>(R.id.text_fetching_url);
        text_fetching_url.visibility=View.GONE


        Handler().postDelayed({
            val sharedPref = getSharedPreferences("LoginDetails", Context.MODE_PRIVATE)
            val isLogin = sharedPref.getBoolean("IsLogin", false);
            val userType = sharedPref.getString("UserType", "nil");

            val sharedPrefProjectDetails =getSharedPreferences("ProjectDetails", Context.MODE_PRIVATE)
            val serviceURL = sharedPrefProjectDetails.getString("ServiceURL", "");

            if (serviceURL.equals("")) {
                if (ChoithramUtils.isOnline(this@SplashScreenActivity)) {
                    text_fetching_url.visibility=View.VISIBLE
                    fetchURL()
                }else {
                    showToast(this@SplashScreenActivity, "Check internet connection and try again", 2)
                    startActivity(Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS))
                    finish()
                }
            } else {

                if (serviceURL != null) {
                    Constants.choithrams_base_url=serviceURL
                }

                if (isLogin) {
                    if (userType?.trim().equals("picker")) {
                        val intent = Intent(this@SplashScreenActivity, HomeScreenPickerActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent)
                        finish()
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    } else {
                        val intent = Intent(this@SplashScreenActivity, HomeScreenManagerActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent)
                        finish()
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                } else {
                    val intent = Intent(this@SplashScreenActivity, LoginScreenActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent)
                    finish()
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

        }, 1500)
    }

    fun goToLoginPage() {
        val intent = Intent(this@SplashScreenActivity, LoginScreenActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun fetchURL() {
        if (ServiceCounter > 4) {
            showToast(this@SplashScreenActivity, "Operation failed !", 2)
            finish()
            return
        }

        when (ServiceCounter) {
            0 -> Constants.base_url = "https://androservice.softlandindia.co.in/SILService.svc/"
            1 -> Constants.base_url = "http://androservice.palmtecandro.com/SILService.svc/"
            2 -> Constants.base_url ="http://androservice.softlandindiasalesforce.com/SILService.svc/"
            3 -> Constants.base_url = "http://androservice.salesforceautomation.in/SILService.svc/"
            4 -> Constants.base_url = "http://androservice.softlandsalesforce.com/SILService.svc/"
            else -> {
                ServiceCounter = 0
                Constants.base_url = "https://androservice.softlandindia.co.in/SILService.svc/"
            }
        }

        val jsonobj = JSONObject()
        jsonobj.put("ProjectCode", BuildConfig.FLAVOR)
        CoroutineScope(Dispatchers.Main).launch {
            BaseurlFetchingAPI.getRetrofit().create(BaseurlFetchingAPI::class.java).getUrlapi(jsonobj.toString())
                .enqueue(object : Callback<ServerUrl> {
                    override fun onResponse(call: Call<ServerUrl>, response: Response<ServerUrl>) =
                        if (response.isSuccessful) {
                            var url = response.body()?.serviceUrl
                            if (url.toString().isNullOrBlank()) {
                                ServiceCounter++
                                fetchURL()
                            } else {
                                try {
                                    url = url.toString().split('~')[1]
                                    Log.i( TAG,"fetchURL => " + Constants.base_url + "GetServiceUrlForDevice?composite=" + jsonobj.toString())
                                    Log.i(TAG, "URL =>$url")
                                    val sharedPref = getSharedPreferences("ProjectDetails", Context.MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putString("ServiceURL",url)
                                        putString("ProjectCode", BuildConfig.FLAVOR)
                                        apply()
                                    }
                                    Constants.choithrams_base_url=url
                                    goToLoginPage()
                                }catch (e:Exception){
                                    ServiceCounter++
                                    fetchURL()
                                }
                            }
                        } else {
                            Log.i(TAG, "response.error " + response.errorBody()!!.string()!!)
                            ServiceCounter++
                            fetchURL()
                        }

                    override fun onFailure(call: Call<ServerUrl>, t: Throwable) {
                        t.printStackTrace()
                        ServiceCounter++
                        fetchURL()
                    }
                })
        }
    }


    private fun showToast(context: Context, msg: String, status: Int) {
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT

        if (status == 1) {
            val customView: View =
                layoutInflater.inflate(R.layout.layout_custom_toast_success, null)
            var msg_txt = customView.findViewById<TextView>(R.id.message)
            msg_txt.text = msg
            toast.view = customView
        } else {
            val customView: View = layoutInflater.inflate(R.layout.layout_custom_toast_error, null)
            var msg_txt = customView.findViewById<TextView>(R.id.message)
            msg_txt.text = msg
            toast.view = customView
        }

        toast.show()
    }

}
