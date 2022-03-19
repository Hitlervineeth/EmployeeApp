package com.softland.choithrams.activitys

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.networks.ChoithramsAPI
import com.softland.choithrams.requestobjects.*
import com.softland.choithrams.retrofitresponses.LogOut
import com.softland.choithrams.retrofitresponses.SaveDayOpen
import com.softland.choithrams.views.CustomProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class HomeScreenPickerActivity : AppCompatActivity() {

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var userid = 0;

    var storeid = 0;
    lateinit var txt_picker_name: TextView
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var btnpicking: CardView
    lateinit var btn_endjob: CardView
    lateinit var btn_logout: CardView
    lateinit var dialog : BottomSheetDialog


    val TAG = "HomeScreenPickerActivity"
    private val progressDialog = CustomProgressDialog()
    val PERMISSION_ID = 1010


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        val db by lazy { ChoithramDB(this@HomeScreenPickerActivity) }

         btnpicking = findViewById<CardView>(R.id.btnpicking)
         btn_endjob = findViewById<CardView>(R.id.btn_endjob)
         btn_logout = findViewById<CardView>(R.id.btn_logout)
         txt_picker_name = findViewById<TextView>(R.id.txt_picker_name)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@HomeScreenPickerActivity)
        RequestPermission()
        getLastLocation()


        CoroutineScope(Dispatchers.IO).launch {
            txt_picker_name.text =db.loginDetailsDao().getAll()[0].DisplayName
            userid = db.loginDetailsDao().getAll()[0].UserID
            storeid = db.loginDetailsDao().getAll()[0].StoreID
        }

        btn_endjob.setOnClickListener(View.OnClickListener {
            getLastLocation()
            val builder = AlertDialog.Builder(this@HomeScreenPickerActivity)
            builder.setTitle("Day Close ")
            builder.setMessage("Do you want to end today work now ?")
                .setCancelable(true)
                .setPositiveButton("Yes") { dialog, id ->
                    dialog.dismiss()
                   showPasswordEntryDialog()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()


        })

        btnpicking.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@HomeScreenPickerActivity, PickingScreenActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        })

        btn_logout.setOnClickListener(View.OnClickListener {
            getLastLocation()
            val builder = AlertDialog.Builder(this@HomeScreenPickerActivity)
            builder.setTitle("Logout")
            builder.setMessage("Do you want to logout now ?")
                .setCancelable(true)
                .setPositiveButton("Yes") { dialog, id ->
                    dialog.dismiss()
                    logOut()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        })

    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this@HomeScreenPickerActivity)
        builder.setTitle("Exit")
        builder.setMessage("Do you want to exit now ?")
            .setCancelable(true)
            .setPositiveButton("Yes") { dialog, id ->
                finish()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    override fun onStart() {
        super.onStart()

        try{
            dialog.dismiss()
            dialog.dismiss()
            dialog.dismiss()
            dialog.dismiss()
            dialog.dismiss()
            dialog.dismiss()
        }catch (e:Exception){

        }

        val sharedPref = getSharedPreferences("choithrams", Context.MODE_PRIVATE)
        val dayopenDate = sharedPref.getString("StartDayDate", getCurrentDate()).toString().split(" ")[0].trim();
        val dayopen = sharedPref.getBoolean("StartDayActivityFlag", false);

        if (dayopen) {
            if (getdatedifference(getCurrentDate(), "$dayopenDate 00:00:00") > 0) {

                val builder = AlertDialog.Builder(this@HomeScreenPickerActivity)
                builder.setTitle("Day Open")
                builder.setMessage("Welcome to new day")
                    .setCancelable(true)
                    .setPositiveButton("OK") { dialog, id ->
                        SaveDayClose(true)
                    }

                val alert = builder.create()
                alert.show()

            }
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

    private fun SaveDayClose(sts:Boolean) {
        progressDialog.show(this@HomeScreenPickerActivity, "Please Wait...")
        if (longitude == 0.0 || latitude == 0.0) {
            showToast(this, "Invalid Location ! ", 2)
            progressDialog.dialog.dismiss()
            return;
        }


        var DeviceID = Settings.Secure.getString(
            getApplicationContext().getApplicationContext().getContentResolver(),
            Settings.Secure.ANDROID_ID
        );

        val credentials = Credentials(
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
            "SaveDayClose",
            "",
            userid,
            1,
            storeid
        )

        val sharedPref = getSharedPreferences("choithrams", Context.MODE_PRIVATE)
        val dayopenDate = sharedPref.getString("StartDayDate", getCurrentDate()).toString().split(" ")[0].trim();
        val dayopen = sharedPref.getBoolean("StartDayActivityFlag", false);


        var requestDataForSaveDayClose = RequestDataForSaveDayClose( dayopenDate , userid , storeid )

        var saveDayClose = SaveDayCloseRequest ( credentials , requestDataForSaveDayClose )

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .saveDayClose(saveDayClose)
                .enqueue(object : Callback<SaveDayOpen> {
                    override fun onResponse(
                        call: Call<SaveDayOpen>,
                        response: Response<SaveDayOpen>
                    ) {
                        progressDialog.dialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                if (url == 1) {
                                    if (response.body()?.ResponseData!!.ReturnStatus == 1) {
                                        progressDialog.dialog.dismiss()
                                        val sharedPref = getSharedPreferences("choithrams", Context.MODE_PRIVATE)
                                        with(sharedPref.edit()) {
                                            putBoolean("StartDayActivityFlag", false)
                                            putString("StartDayDate", getCurrentDate())
                                            apply()
                                        }
                                        if(sts) {
                                            showToast(
                                                applicationContext,
                                                "Successfully  day opened",
                                                1
                                            )
                                        }else{
                                            showToast(
                                                applicationContext,
                                                "Successfully  day closed",
                                                1
                                            )
                                        }

                                    } else {
                                        showToast(applicationContext, "Operation failed ! ", 2)
                                        progressDialog.dialog.dismiss()
                                        showDatcloseDialog()
                                    }
                                } else {
                                    showToast(
                                        applicationContext,
                                        "" + response.body()?.StatusReturn!!.Message.toString(),
                                        2
                                    )
                                    progressDialog.dialog.dismiss()
                                    showDatcloseDialog()
                                }
                            } else {
                                showToast(applicationContext, "Operation failed ! ", 2)
                                progressDialog.dialog.dismiss()
                                showDatcloseDialog()
                            }
                        } else {
                            showToast(applicationContext, "Operation failed ! ", 2)
                            progressDialog.dialog.dismiss()
                            showDatcloseDialog()
                        }
                    }

                    override fun onFailure(call: Call<SaveDayOpen>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(applicationContext, "Operation failed ! ", 2)
                        showDatcloseDialog()
                    }
                })
        }
    }

    private fun logOut() {
        progressDialog.show(this@HomeScreenPickerActivity, "Please Wait...")
        if (longitude == 0.0 || latitude == 0.0) {
            showToast(this@HomeScreenPickerActivity, "Invalid Location ! ", 2)
            progressDialog.dialog.dismiss()
            return;
        }


        var DeviceID = Settings.Secure.getString(
            getApplicationContext().getApplicationContext().getContentResolver(),
            Settings.Secure.ANDROID_ID
        );

        val credentials = Credentials(
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
            "LogOut",
            "",
            userid,
            1,
            storeid
        )


        val sharedPref = getSharedPreferences("JobDetails", Context.MODE_PRIVATE)
        var JobNumber = sharedPref.getInt("JobNumber", 1)

        var jobDetails_list = ArrayList<LastBillNumber>()
        jobDetails_list.add(LastBillNumber(JobNumber, 1))

        var requestDataForLogOut = RequestDataForLogOut(jobDetails_list, userid)

        var logOutRequest = LogOutRequest(credentials, requestDataForLogOut)

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .logOut(logOutRequest)
                .enqueue(object : Callback<LogOut> {
                    override fun onResponse(
                        call: Call<LogOut>,
                        response: Response<LogOut>
                    ) {
                        progressDialog.dialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                if (url == 1) {
                                    if (response.body()?.ResponseData!!.ReturnStatus == 1) {
                                        progressDialog.dialog.dismiss()
                                        showToast(
                                            this@HomeScreenPickerActivity,
                                            "Logged out successfully",
                                            1
                                        )
                                        val sharedPref = getSharedPreferences(
                                            "LoginDetails",
                                            Context.MODE_PRIVATE
                                        )
                                        with(sharedPref.edit()) {
                                            putBoolean("IsLogin", false)
                                            putString("UserType", "nil")
                                            apply()
                                        }
                                        val intent = Intent(
                                            this@HomeScreenPickerActivity,
                                            LoginScreenActivity::class.java
                                        )
                                        startActivity(intent)
                                        finish()
                                        overridePendingTransition(
                                            R.anim.slide_in_left,
                                            R.anim.slide_out_right
                                        )

                                    } else if (response.body()?.ResponseData!!.ReturnStatus == 2) {
                                        showToast(applicationContext, "Data syncing in process", 2)
                                        progressDialog.dialog.dismiss()
                                    } else {
                                        showToast(applicationContext, "Operation failed ! ", 2)
                                        progressDialog.dialog.dismiss()
                                    }
                                } else {
                                    showToast(
                                        applicationContext,
                                        "" + response.body()?.StatusReturn!!.Message,
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

                    override fun onFailure(call: Call<LogOut>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(applicationContext, "Operation failed ! ", 2)
                    }
                })
        }
    }

    fun RequestPermission() {
        ActivityCompat.requestPermissions(
            this@HomeScreenPickerActivity,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ),
            PERMISSION_ID
        )
    }

    fun getLastLocation() {
        if (CheckPermission()) {
            if (isLocationEnabled()) {

                if (ActivityCompat.checkSelfPermission(
                        this@HomeScreenPickerActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@HomeScreenPickerActivity,
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
                Toast.makeText(this@HomeScreenPickerActivity, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            RequestPermission()
        }
    }

    private fun CheckPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@HomeScreenPickerActivity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this@HomeScreenPickerActivity,
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@HomeScreenPickerActivity)
        if (ActivityCompat.checkSelfPermission(
                this@HomeScreenPickerActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@HomeScreenPickerActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
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

    private fun getdatedifference(currentDate: String, selectedDate: String): Long {
        val date1: Date
        val date2: Date
        val dates = SimpleDateFormat("yyyy-MM-dd")
        date1 = dates.parse(currentDate)
        date2 = dates.parse(selectedDate)
        val difference: Long = abs(date1.time - date2.time)
        val seconds = difference / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        Log.e(TAG, "getdatedifference $currentDate to $selectedDate  => $days")
        return days
    }

    fun showDatcloseDialog(){
        val builder = AlertDialog.Builder(this@HomeScreenPickerActivity)
        builder.setTitle("Day Open")
        builder.setMessage("Welcome to new day")
            .setCancelable(true)
            .setPositiveButton("Retry") { dialog, id ->
                SaveDayClose(true)
            }

        val alert = builder.create()
        alert.show()
    }

    fun showPasswordEntryDialog() {
        val view1: View = layoutInflater.inflate(R.layout.layout_bottom_dilalog_for_password_entry, null)
        dialog = BottomSheetDialog(this@HomeScreenPickerActivity)
        dialog.setContentView(view1)
        val  password =view1.findViewById<TextInputEditText>(R.id.text_filed_password)
        val submit =view1.findViewById<MaterialButton>(R.id.btn_login)

        val sharedPref = this.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE)
        val loginPassword = sharedPref.getString("Password", "");

        submit.setOnClickListener(View.OnClickListener {

            if(loginPassword.equals(password.text.toString().trim())){
                dialog.cancel()
                SaveDayClose(false)
            }else{
                showToast(applicationContext, "Invalid Password ", 2)
            }
        })

        if(!dialog.isShowing) {
            dialog.show()
        }
    }


}