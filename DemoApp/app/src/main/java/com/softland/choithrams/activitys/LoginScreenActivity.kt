package com.softland.choithrams.activitys

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.softland.choithrams.BuildConfig
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.entitys.Login_details
import com.softland.choithrams.networks.ChoithramsAPI
import com.softland.choithrams.requestobjects.Credentials
import com.softland.choithrams.requestobjects.LoginActivityRequest
import com.softland.choithrams.requestobjects.RequestDataForLoginActivity
import com.softland.choithrams.retrofitresponses.LoginActivity
import com.softland.choithrams.utils.Constants
import com.softland.choithrams.views.CustomProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginScreenActivity : AppCompatActivity() {
    private val db by lazy { ChoithramDB(this@LoginScreenActivity) }
    private val progressDialog = CustomProgressDialog()

    //Declaring the needed Variables
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var txt_device_id: TextView
    lateinit var LoginButton: MaterialButton

    val PERMISSION_ID = 1010
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var altitude: Double = 0.0

    lateinit var username : TextInputEditText
    lateinit var password : TextInputEditText




    val TAG = "LoginScreenActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
         username = findViewById<TextInputEditText>(R.id.text_filed_username)
         password = findViewById<TextInputEditText>(R.id.text_filed_password)
         txt_device_id = findViewById<TextView>(R.id.txt_device_id)
         LoginButton = findViewById<MaterialButton>(R.id.btn_login)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@LoginScreenActivity)
        RequestPermission()
        val sharedPrefProjectDetails = getSharedPreferences("ProjectDetails", Context.MODE_PRIVATE)
        val serviceURL = sharedPrefProjectDetails.getString("ServiceURL", "");
        Constants.choithrams_base_url = serviceURL.toString()
        txt_device_id.visibility= GONE
        getLastLocation()


        LoginButton.setOnClickListener(View.OnClickListener {

            synchronized(this@LoginScreenActivity) {
                getLastLocation()
            }

            if (username.text.toString().trim().isBlank() || password.text.toString().trim() .isBlank()) {
                showToast(this@LoginScreenActivity, "Invalid input ! ", 2)
            } else {
                progressDialog.show(this@LoginScreenActivity, "Please Wait...")
                loginuser(username.text.toString().trim(), password.text.toString().trim())
            }

        })

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

    override fun onStart() {
        super.onStart()

    }

    override fun onBackPressed() {
        finish()
    }

    private fun loginuser(username: String, password: String) {

        Log.d(TAG, "Your  longitude :$longitude")
        Log.d(TAG, "Your  latitude:$latitude")
        Log.d(TAG, "Your  altitude:$altitude")
        if (longitude == 0.0 || latitude == 0.0) {
            showToast(this@LoginScreenActivity, "Invalid Location ! ", 2)
            progressDialog.dialog.dismiss()
            return;
        }


        var DeviceID = Settings.Secure.getString(
            getApplicationContext().getApplicationContext().getContentResolver(),
            Settings.Secure.ANDROID_ID
        );

        val credentialsForLoginActivity = Credentials(
            altitude.toString(),
            "PICKER",
            "",
            DeviceID,
            "",
            DeviceID.toUpperCase(),
            0,
            latitude.toString(),
            longitude.toString(),
            0,
            "LoginActivity",
            "",
            0,
            1,
            0
        )

        val requestDataForLoginActivity= RequestDataForLoginActivity(
            BuildConfig.VERSION_NAME,
            "" + android.os.Build.VERSION.SDK_INT,
            "",
            getPhoneName(),
            DeviceID.toUpperCase(),
            DeviceID,
            password,
            username
        )
        val loginActivityRequest = LoginActivityRequest(credentialsForLoginActivity, requestDataForLoginActivity)




        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .getLogin(loginActivityRequest)
                .enqueue(object : Callback<LoginActivity> {
                    override fun onResponse(
                        call: Call<LoginActivity>,
                        response: Response<LoginActivity>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                if (url == 1) {
                                    var userid=response.body()?.ResponseData!!.UserID
                                    if(userid==0){
                                        txt_device_id.setText("DEVICE ID : "+DeviceID.toUpperCase())
                                        txt_device_id.visibility=VISIBLE
                                        progressDialog.dialog.dismiss()
                                        showToast(applicationContext, response.body()?.ResponseData!!.Message, 2)
                                    }else {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            db.loginDetailsDao().clear()
                                            db.loginDetailsDao().save(
                                                Login_details(
                                                    1, response.body()?.ResponseData!!.UserID,
                                                    response.body()?.ResponseData!!.TokenID,
                                                    response.body()?.ResponseData!!.DisplayName,
                                                    response.body()?.ResponseData!!.JobNumber,
                                                    response.body()?.ResponseData!!.StoreID,
                                                    response.body()?.ResponseData!!.StoreCode,
                                                    response.body()?.ResponseData!!.StoreName,
                                                    response.body()?.ResponseData!!.PickerCode,
                                                    response.body()?.ResponseData!!.FinCode,
                                                    response.body()?.ResponseData!!.EmployeeType,
                                                    response.body()?.ResponseData!!.ServerTime,
                                                    response.body()?.ResponseData!!.RoundOff,
                                                    response.body()?.ResponseData!!.RoundOffLimit,
                                                    response.body()?.ResponseData!!.CurrencyID,
                                                    response.body()?.ResponseData!!.Currency,
                                                    response.body()?.ResponseData!!.TimeZone
                                                )
                                            )
                                            Log.e(
                                                TAG,
                                                "saved data " + db.loginDetailsDao().getAll().size
                                            )

                                            val sharedPref = getSharedPreferences("JobDetails", Context.MODE_PRIVATE)
                                            with(sharedPref.edit()) {
                                                putInt("JobNumber", response.body()?.ResponseData!!.JobNumber)
                                                putString("JobNumberPrefix", response.body()?.ResponseData!!.FinCode.toUpperCase()+ response.body()?.ResponseData!!.StoreCode.toUpperCase()+response.body()?.ResponseData!!.PickerCode.toUpperCase())
                                                apply()
                                            }
                                            progressDialog.dialog.dismiss()
                                            loginSuccess(password)
                                        }
                                    }
                                } else {
                                    showToast(
                                        applicationContext,
                                        "" + response.body()?.StatusReturn!!.Message.toString(),
                                        2
                                    )
                                    progressDialog.dialog.dismiss()
                                }
                            } else {
                                showToast(applicationContext, "Operation failed ! ", 2)
                                progressDialog.dialog.dismiss()
                            }
                        } else {
                            showToast(applicationContext, "Operation failed ! ", 2)
                            progressDialog.dialog.dismiss()
                        }
                    }

                    override fun onFailure(call: Call<LoginActivity>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(applicationContext, "Operation failed ! ", 2)
                    }
                })
        }
    }

    fun RequestPermission() {
        ActivityCompat.requestPermissions(
            this@LoginScreenActivity,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,

            ),
            PERMISSION_ID
        )
    }

    fun getLastLocation() {
        if (CheckPermission()) {
            if (isLocationEnabled()) {

                if (ActivityCompat.checkSelfPermission(
                        this@LoginScreenActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@LoginScreenActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        NewLocationData()
                    } else {
                        longitude = location.longitude
                        latitude = location.latitude
                        altitude = location.altitude

                        Log.d(TAG, "Your Location longitude :" + location.longitude)
                        Log.d(TAG, "Your Location latitude:" + location.latitude)
                        Log.d(TAG, "Your Location altitude:" + location.altitude)
                    }
                }

            } else {
                Toast.makeText(this@LoginScreenActivity, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            RequestPermission()
        }
    }

    private fun CheckPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@LoginScreenActivity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this@LoginScreenActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    fun isLocationEnabled(): Boolean {
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun NewLocationData() {
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@LoginScreenActivity)
        if (ActivityCompat.checkSelfPermission(
                this@LoginScreenActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@LoginScreenActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            Log.d(TAG, "your last last location: " + lastLocation.longitude.toString())
            longitude = lastLocation.longitude
            latitude = lastLocation.latitude
            altitude = lastLocation.altitude
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    fun getPhoneName(): String? {
        val myDevice = BluetoothAdapter.getDefaultAdapter()
        return myDevice.name
    }

    fun getIMEINumber(): String? {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.deviceId
    }

    fun loginSuccess(password: String) {
        var usertype: String? = ""
        CoroutineScope(Dispatchers.IO).launch {
            usertype = db.loginDetailsDao().getAll().get(0).EmployeeType
            if (usertype.toString().toUpperCase().trim().equals("STORE MANAGER") ) {
                Handler(Looper.getMainLooper()).post {
                    showToast(applicationContext, "Logged in successfully", 1)
                }

                val sharedPref = getSharedPreferences("LoginDetails", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putBoolean("IsLogin", true)
                    putString("UserType", "manager")
                    putString("Password",password )
                    apply()
                }
                val intent = Intent(applicationContext, HomeScreenManagerActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else if (usertype.toString().toUpperCase().trim().equals("PICKER")) {
                Handler(Looper.getMainLooper()).post {
                    showToast(applicationContext, "Logged in successfully", 1)
                }
                val sharedPref = getSharedPreferences("LoginDetails", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putBoolean("IsLogin", true)
                    putString("UserType", "picker")
                    putString("Password",password )
                    apply()
                }
                val intent = Intent(applicationContext, HomeScreenPickerActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                Handler(Looper.getMainLooper()).post {
                    showToast(applicationContext, "Invalid input ! ", 2)
                }

            }
        }
    }


}