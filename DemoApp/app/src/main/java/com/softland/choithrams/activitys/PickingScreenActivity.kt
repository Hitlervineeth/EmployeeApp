package com.softland.choithrams.activitys

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.entitys.Section_details
import com.softland.choithrams.networks.ChoithramsAPI
import com.softland.choithrams.requestobjects.*
import com.softland.choithrams.retrofitresponses.GetAllSavedJobsResponse
import com.softland.choithrams.retrofitresponses.GetAllSections
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

class PickingScreenActivity : AppCompatActivity() {

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var userid = 0;
    var storeid = 0;
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val progressDialog = CustomProgressDialog()
    private val db by lazy { ChoithramDB(this) }
    val TAG = "PickingScreenActivity"
    val PERMISSION_ID = 1010

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picking_screen)
        val sharedPref = getSharedPreferences("choithrams",Context.MODE_PRIVATE)
        val StartDayActivityFlag = sharedPref.getBoolean("StartDayActivityFlag", false)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        RequestPermission()
        getLastLocation()


        CoroutineScope(Dispatchers.Main).launch {
            userid = db.loginDetailsDao().getAll().get(0).UserID
            storeid = db.loginDetailsDao().getAll().get(0).StoreID
            findViewById<TextView>(R.id.txt_picker_name).text =
                db.loginDetailsDao().getAll()[0].DisplayName
        }
        findViewById<MaterialButton>(R.id.btn_next).visibility = View.INVISIBLE
        if (!StartDayActivityFlag) {
            showStartDayActivityDialog()
        } else {
            findViewById<MaterialButton>(R.id.btn_next).visibility = View.VISIBLE
        }

        findViewById<MaterialButton>(R.id.btn_next).setOnClickListener(View.OnClickListener {
            if (findViewById<MaterialRadioButton>(R.id.radio_new_job).isChecked()) {
                getLastLocation()
                saveSections()
            } else {
                getLastLocation()
                Thread{
                    db.jobDetailsDao().clear()
                }.start()

                getAllSavedJobs()
            }
        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onBackPressed() {
        val intent = Intent(this, HomeScreenPickerActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun setacive(view: View) {
        if (view.id == R.id.radio_new_job) {
            findViewById<MaterialRadioButton>(R.id.radio_new_job).isChecked = true;
            findViewById<MaterialRadioButton>(R.id.radio_saved_job).isChecked = false;
        } else {
            findViewById<MaterialRadioButton>(R.id.radio_new_job).isChecked = false;
            findViewById<MaterialRadioButton>(R.id.radio_saved_job).isChecked = true;
        }
    }


    private fun showStartDayActivityDialog() {
        var dialog = AlertDialog.Builder(this).create()
        val factory = LayoutInflater.from(this)
        val dialogView: View = factory.inflate(R.layout.layout_confirm_dialog, null)
        dialog.setView(dialogView)
        dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
            .setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                findViewById<MaterialButton>(R.id.btn_next).visibility = View.INVISIBLE
            })
        dialogView.findViewById<MaterialButton>(R.id.btn_ok)
            .setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                saveDayopen()
            })
        dialogView.findViewById<TextView>(R.id.txt_main_tittle).text = "START DAY ACTIVITY"
        dialogView.findViewById<TextView>(R.id.txt_sub_tittle).text = ""
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 50)
        dialog.window?.setBackgroundDrawable(inset)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun saveSections() {
        progressDialog.show(this, "Please Wait...")
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
            "GetAllSections",
            "",
            userid,
            1,
            storeid
        )

        val requestDataForGetAllSections = RequestDataForGetAllSections(storeid, userid)

        val getAllSectionsRequest = GetAllSectionsRequest(credentials, requestDataForGetAllSections)


        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .getAllSections(getAllSectionsRequest)
                .enqueue(object : Callback<GetAllSections> {
                    override fun onResponse(
                        call: Call<GetAllSections>,
                        response: Response<GetAllSections>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                val sectionlist = response.body()?.ResponseData!!.Section
                                if (url == 1) {
                                    if (response.body()?.ResponseData!!.NoOfItems <= 0) {
                                        showToast(applicationContext, "Sections not found ! ", 2)
                                    }

                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.sectionDetailsDao().clear()
                                        var i = 1
                                        for (obj in sectionlist) {
                                            db.sectionDetailsDao().save(
                                                Section_details(
                                                    i++,
                                                    response.body()?.ResponseData!!.UserID,
                                                    obj.SectionID,
                                                    obj.SectionCode,
                                                    obj.SectionName,
                                                    obj.Description
                                                )
                                            )
                                        }

                                        progressDialog.dialog.dismiss()

                                        val intent = Intent( applicationContext,NewJobHomeScreenActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                        overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left)
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

                    override fun onFailure(call: Call<GetAllSections>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(applicationContext, "Operation failed ! ", 2)
                    }
                })
        }
    }

    fun RequestPermission() {
        ActivityCompat.requestPermissions(
            this,
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
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
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
                Toast.makeText(this, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            RequestPermission()
        }
    }

    private fun CheckPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
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

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            Log.d(TAG, "your last last location: " + lastLocation.longitude.toString())
            longitude = lastLocation.longitude
            latitude = lastLocation.latitude
            altitude = lastLocation.altitude
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

    fun getPhoneName(): String? {
        val myDevice = BluetoothAdapter.getDefaultAdapter()
        return myDevice.name
    }

    private fun saveDayopen() {
        progressDialog.show(this, "Please Wait...")
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
            "SaveDayOpen",
            "",
            userid,
            1,
            storeid
        )

        var requestDataForSaveDayOpen = RequestDataForSaveDayOpen(getCurrentDate(), userid, storeid)

        var saveDayOpen = SaveDayOpenRequest(credentials, requestDataForSaveDayOpen)

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .saveDayOpen(saveDayOpen)
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
                                    when (response.body()?.ResponseData!!.ReturnStatus) {
                                        1 -> {
                                            progressDialog.dialog.dismiss()
                                            val sharedPref = getSharedPreferences("choithrams",Context.MODE_PRIVATE)
                                            with(sharedPref.edit()) {
                                                putBoolean("StartDayActivityFlag", true)
                                                putString("StartDayDate", getCurrentDate())
                                                apply()
                                            }
                                            findViewById<MaterialButton>(R.id.btn_next).visibility =  View.VISIBLE
                                        }
                                        2 -> {
                                            showToast(applicationContext, "Already day closed ! ", 2)
                                            progressDialog.dialog.dismiss()
                                            showStartDayActivityDialog()
                                        }
                                        else -> {
                                            showToast(applicationContext, "Operation failed ! ", 2)
                                            progressDialog.dialog.dismiss()
                                            showStartDayActivityDialog()
                                        }
                                    }
                                } else {
                                    showToast(
                                        applicationContext,
                                        "" + response.body()?.StatusReturn!!.Message.toString(),
                                        2
                                    )
                                    progressDialog.dialog.dismiss()
                                    showStartDayActivityDialog()
                                }
                            } else {

                                showToast(applicationContext, "Operation failed ! ", 2)
                                progressDialog.dialog.dismiss()
                                showStartDayActivityDialog()
                            }
                        } else {
                            showToast(applicationContext, "Operation failed ! ", 2)
                            progressDialog.dialog.dismiss()
                            showStartDayActivityDialog()
                        }
                    }

                    override fun onFailure(call: Call<SaveDayOpen>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(applicationContext, "Operation failed ! ", 2)
                        showStartDayActivityDialog()
                    }
                })
        }
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }

    private fun getAllSavedJobs() {
        progressDialog.show(this, "Please Wait...")
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
            "GetAllSections",
            "",
            userid,
            1,
            storeid
        )

        val requestDataForGetAllSections = RequestDataForGetAllSections(storeid, userid)

        val getAllSectionsRequest = GetAllSectionsRequest(credentials, requestDataForGetAllSections)


        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .getAllSavedJobs(getAllSectionsRequest)
                .enqueue(object : Callback<GetAllSavedJobsResponse> {
                    override fun onResponse(
                        call: Call<GetAllSavedJobsResponse>,
                        response: Response<GetAllSavedJobsResponse>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                val sectionlist = response.body()?.ResponseData!!.Job
                                if (url == 1) {
                                    if (response.body()?.ResponseData!!.NoOfItems <= 0) {
                                        showToast(applicationContext, "Job not found ! ", 2)
                                        progressDialog.dialog.dismiss()
                                    } else {
                                        CoroutineScope(Dispatchers.IO).launch {

                                        //    db.jobDetailsDao().saveAll(response.body()?.ResponseData!!.Job)
                                            var i = 1
                                            for (obj in sectionlist) {
                                                db.jobDetailsDao().save(
                                                    Job_details(
                                                        0,
                                                        obj.UserID,
                                                        obj.JobNumber,
                                                        obj.JobDate,
                                                        obj.JobEndDate,
                                                        obj.JobStartDate,
                                                        obj.JobStatus,
                                                        obj.NoOfItems,
                                                        obj.Remarks,
                                                        obj.StoreID,
                                                        obj.NetAmount,
                                                        0.00,
                                                        obj.GrossAmount,
                                                        0.00,
                                                        obj.SectionID,
                                                        obj.SectionName,
                                                        obj.SectionCode,
                                                        ""

                                                    )
                                                )
                                            }
                                            val intent = Intent(
                                                this@PickingScreenActivity,
                                                SaveJobHomeScreenActivity::class.java
                                            )
                                            progressDialog.dialog.dismiss()
                                            startActivity(intent)
                                            finish()
                                            overridePendingTransition(
                                                R.anim.slide_in_right,
                                                R.anim.slide_out_left
                                            )
                                        }
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

                    override fun onFailure(call: Call<GetAllSavedJobsResponse>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(applicationContext, "Operation failed ! ", 2)
                    }
                })
        }
    }


}