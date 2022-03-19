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
import com.google.android.gms.location.*
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.networks.ChoithramsAPI
import com.softland.choithrams.requestobjects.*
import com.softland.choithrams.retrofitresponses.GetAllJobsByStoreManager
import com.softland.choithrams.retrofitresponses.LogOut
import com.softland.choithrams.views.CustomProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeScreenManagerActivity : AppCompatActivity() {

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var userid = 0;
    var storeid = 0;
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val TAG = "HomeScreenManagerActivity"
    private val progressDialog = CustomProgressDialog()
    val PERMISSION_ID = 1010
    val db by lazy { ChoithramDB(this@HomeScreenManagerActivity) }
    lateinit var txt_picker_name: TextView
    lateinit var btn_logout: CardView
    lateinit var btn_live_jobs: CardView
    lateinit var btn_pending_approvals: CardView
    lateinit var btn_approved_status: CardView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen_manager)
         btn_logout = findViewById<CardView>(R.id.btn_logout)
         btn_live_jobs = findViewById<CardView>(R.id.btn_live_jobs)
         btn_pending_approvals = findViewById<CardView>(R.id.btn_pending_approvals)
         btn_approved_status = findViewById<CardView>(R.id.btn_approved_status)
         txt_picker_name = findViewById<TextView>(R.id.txt_picker_name)



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@HomeScreenManagerActivity)
        RequestPermission()
        getLastLocation()

        CoroutineScope(Dispatchers.IO).launch {
            txt_picker_name.text =db.loginDetailsDao().getAll()[0].DisplayName
            userid = db.loginDetailsDao().getAll()[0].UserID
            storeid = db.loginDetailsDao().getAll()[0].StoreID
        }


        btn_logout.setOnClickListener(View.OnClickListener {
            val builder = AlertDialog.Builder(this@HomeScreenManagerActivity)
            builder.setTitle("Logout")
            builder.setMessage("Do you want to logout now ?")
                .setCancelable(true)
                .setPositiveButton("Yes") { dialog, id ->
                    logOut()
                }
                .setNegativeButton("No") { dialog, id ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        })

        btn_live_jobs.setOnClickListener(View.OnClickListener {
            Thread {
                db.jobDetailsDao().clear()
            }.start()

            getAllJobsByStoreManager(0)
        })

        btn_pending_approvals.setOnClickListener(View.OnClickListener {

            Thread {
                db.jobDetailsDao().clear()
            }.start()

            getAllJobsByStoreManager(1)

        })

        btn_approved_status.setOnClickListener(View.OnClickListener {

            Thread {
                db.jobDetailsDao().clear()
            }.start()

            getAllJobsByStoreManager(2)

        })

    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this@HomeScreenManagerActivity)
        builder.setTitle("Exit")
        builder.setMessage("Do you want to exit now ?")
            .setCancelable(true)
            .setPositiveButton("Yes") { dialog, id ->
                finish()
            }
            .setNegativeButton("No") { dialog, id ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
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

    fun RequestPermission() {
        ActivityCompat.requestPermissions(
            this@HomeScreenManagerActivity,
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
                        this@HomeScreenManagerActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@HomeScreenManagerActivity,
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
                Toast.makeText(this@HomeScreenManagerActivity, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            RequestPermission()
        }
    }

    private fun CheckPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@HomeScreenManagerActivity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this@HomeScreenManagerActivity,
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@HomeScreenManagerActivity)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
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


    private fun getAllJobsByStoreManager( search_type_id: Int) {
        progressDialog.show(this@HomeScreenManagerActivity, "Please Wait...")
        if (longitude == 0.0 || latitude == 0.0) {
            showToast(this@HomeScreenManagerActivity, "Invalid Location ! ", 2)
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
            "GetAllJobsByStoreManager",
            "",
            userid,
            1,
            storeid
        )

        var requestDataForGetAllJobsByStoreManager = RequestDataForGetAllJobsByStoreManager(getCurrentDate(), 0, getCurrentDate(), userid)

        var getAllJobsByStoreManagerRequest =
            GetAllJobsByStoreManagerRequest(credentials, requestDataForGetAllJobsByStoreManager)

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .getAllJobsByStoreManager(getAllJobsByStoreManagerRequest)
                .enqueue(object : Callback<GetAllJobsByStoreManager> {
                    override fun onResponse(
                        call: Call<GetAllJobsByStoreManager>,
                        response: Response<GetAllJobsByStoreManager>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                if (url == 1) {

                                    if (response.body()?.ResponseData!!.NoOfItems > 0 && !response.body()?.ResponseData!!.JobByStoreManager.isNullOrEmpty()) {
                                        CoroutineScope(Dispatchers.Main).launch{
                                            db.jobDetailsDao().saveAll(response.body()?.ResponseData!!.JobByStoreManager)
//                                            val joblist = response.body()?.ResponseData!!.JobByStoreManager
//                                            for (obj in joblist) {
//                                                db.jobDetailsDao().save(
//                                                    Job_details(
//                                                        0,
//                                                        obj.UserID,
//                                                        obj.JobNumber,
//                                                        obj.JobDate,
//                                                        obj.JobEndDate,
//                                                        obj.JobStartDate,
//                                                        obj.JobStatus,
//                                                        obj.NoOfItems,
//                                                        obj.Remarks,
//                                                        obj.StoreID,
//                                                        obj.NetAmount,
//                                                        obj.RoundOff,
//                                                        obj.GrossAmount,
//                                                        obj.TotalDiscount,
//                                                        obj.SectionID,
//                                                        obj.SectionName,
//                                                        obj.SectionCode,
//                                                        obj.Picker
//                                                    )
//                                                )
//                                            }
                                        }
                                        progressDialog.dialog.dismiss()

                                        when (search_type_id) {
                                            0 -> {
                                                val intent = Intent(this@HomeScreenManagerActivity, LiveJobsScreenActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                            }
                                            1 -> {
                                                val intent = Intent(this@HomeScreenManagerActivity, PendingApprovalActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                            }
                                            else -> {
                                                val intent = Intent(this@HomeScreenManagerActivity, ApprovedStatusScreenActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                                            }
                                        }

                                    } else {
                                        showToast(applicationContext, "Job not found ! ", 2)
                                        progressDialog.dialog.dismiss()
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

                    override fun onFailure(call: Call<GetAllJobsByStoreManager>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(applicationContext, "Operation failed ! ", 2)

                    }
                })
        }
    }

    private fun logOut() {
        progressDialog.show(this@HomeScreenManagerActivity, "Please Wait...")
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
            "LogOut",
            "",
            userid,
            1,
            storeid
        )


        val sharedPref = getSharedPreferences("JobDetails", Context.MODE_PRIVATE)
        var JobNumber =1

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
                                            this@HomeScreenManagerActivity,
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
                                            this@HomeScreenManagerActivity,
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



}