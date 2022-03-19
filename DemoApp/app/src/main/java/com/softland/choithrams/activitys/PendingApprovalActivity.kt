package com.softland.choithrams.activitys

import PendingApprovalJobsListAdapter
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.entitys.Carted_item_details
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.entitys.Saved_job_item_details
import com.softland.choithrams.interfaces.JobClickListners
import com.softland.choithrams.networks.ChoithramsAPI
import com.softland.choithrams.requestobjects.Credentials
import com.softland.choithrams.requestobjects.GetSavedJobDetailsRequest
import com.softland.choithrams.requestobjects.RequestDataForGetAllStockItems
import com.softland.choithrams.retrofitresponses.GetJobDetailsByStoreManager
import com.softland.choithrams.retrofitresponses.GetSavedJobDetails
import com.softland.choithrams.viewmodel.JobViewModel
import com.softland.choithrams.views.CustomProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PendingApprovalActivity : AppCompatActivity(), JobClickListners {

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var userid = 0;
    var storeid = 0;
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val TAG = "HomeScreenManagerActivity"
    private val progressDialog = CustomProgressDialog()
    val PERMISSION_ID = 1010
    val db by lazy { ChoithramDB(this@PendingApprovalActivity) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_approval)
        val item_search_input: EditText = findViewById(R.id.item_search_input)
        val item_clear_click_parent: RelativeLayout = findViewById(R.id.item_clear_click_parent)
        val adapter: PendingApprovalJobsListAdapter by lazy { PendingApprovalJobsListAdapter(this) }
        val jobViewModel: JobViewModel = JobViewModel(db.jobDetailsDao())
        adapter.setListner(this@PendingApprovalActivity)

        val recyclerview = findViewById<RecyclerView>(R.id.recycler_view_live_jobs)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        RequestPermission()
        getLastLocation()

        CoroutineScope(Dispatchers.IO).launch {
            userid = db.loginDetailsDao().getAll()[0].UserID
            storeid = db.loginDetailsDao().getAll()[0].StoreID
        }


        jobViewModel.PendingApprovalData.observe(this) {
            adapter.setData(it)
        }

        item_search_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (item_search_input.length() > 0) {
                    if (item_clear_click_parent.visibility != View.VISIBLE) {
                        item_clear_click_parent.visibility = View.VISIBLE
                    }
                    if (!item_search_input.text.toString().trim().isNullOrBlank()) {
                        jobViewModel.searchPendingApprovalJob(
                            item_search_input.text.toString().trim()
                        ).observe(this@PendingApprovalActivity, { list ->
                            list.let {
                                adapter.setData(it)
                            }
                        })
                    }
                } else {
                    item_clear_click_parent.visibility = View.GONE
                    jobViewModel.searchPendingApprovalJob("")
                        .observe(this@PendingApprovalActivity, { list ->
                            list.let {
                                adapter.setData(it)
                            }
                        })
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        item_clear_click_parent.setOnClickListener { v: View? ->
            item_search_input.text.clear()
        }

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onBackPressed() {
        val intent = Intent(this, HomeScreenManagerActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(jobNumber: String,job_id:String,PickerId :Int,jobDate:String,sectionId:Int) {

        Thread{
            db.savedJobItemDetailsDao().clear()
        }.start()

        getSharedPreferences(
            "JobDetails",
            Context.MODE_PRIVATE
        ).edit().putString("JobNumber", jobNumber)
            .putInt("PickerId",PickerId)
            .putString("jobDate",jobDate)
            .putInt("SectionID",sectionId)
            .apply()
        getJobDetailsByStoreManager(jobNumber,job_id,PickerId)
    }

    fun getJobDetailsByStoreManager(jobNumber: String,job_id:String,PickerId :Int) {
        progressDialog.show(this@PendingApprovalActivity, "Please Wait...")
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
            "GetJobDetailsByStoreManager",
            "",
            userid,
            1,
            storeid
        )

        val requestDataForGetAllStockItems = RequestDataForGetAllStockItems(0, storeid, PickerId, jobNumber)

        val getSavedJobDetailsRequest =  GetSavedJobDetailsRequest(credentials, requestDataForGetAllStockItems)

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .getJobDetailsByStoreManager(getSavedJobDetailsRequest)
                .enqueue(object : Callback<GetJobDetailsByStoreManager> {
                    override fun onResponse(
                        call: Call<GetJobDetailsByStoreManager>,
                        response: Response<GetJobDetailsByStoreManager>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                if (url == 1) {
                                    if (response.body()?.ResponseData!!.isEmpty()) {
                                        showToast(this@PendingApprovalActivity, "Job details not found! ", 2)
                                        progressDialog.dialog.dismiss()
                                    } else {
                                            CoroutineScope(Dispatchers.IO).launch {

                                                db.savedJobItemDetailsDao().saveAll(response.body()?.ResponseData!!)
//                                                for (obj in response.body()?.ResponseData!!) {
//                                                    var sts = 0;
//                                                    db.savedJobItemDetailsDao().save(
//                                                        Saved_job_item_details(
//                                                            0,
//                                                            obj.StockID,
//                                                            obj.StockCode,
//                                                            obj.StockName,
//                                                            obj.BarCodeID,
//                                                            obj.BarCode,
//                                                            obj.UnitID,
//                                                            obj.UnitCode,
//                                                            obj.UnitName,
//                                                            obj.Quantity,
//                                                            obj.ExpiryDate.replace("T", " ")
//                                                                .toString(),
//                                                            obj.ActualRate,
//                                                            obj.RuleRate,
//                                                            obj.RoundOff,
//                                                            obj.Rate,
//                                                            obj.GrossAmount,
//                                                            obj.NetAmount,
//                                                            0
//                                                        )
//                                                    )
//                                                }
                                            }
                                            progressDialog.dialog.dismiss()
                                            val intent = Intent(
                                                applicationContext,
                                                SavedJobItemDetailsActivity::class.java
                                            )
                                            intent.putExtra("JobID", job_id)
                                            startActivity(intent)
                                            finish()
                                            overridePendingTransition(
                                                R.anim.slide_in_right,
                                                R.anim.slide_out_left
                                            )

                                    }
                                } else {
                                    showToast(this@PendingApprovalActivity, "Operation failed ! ", 2)
                                    progressDialog.dialog.dismiss()
                                }

                            } else {
                                showToast(this@PendingApprovalActivity, "Operation failed ! ", 2)
                                progressDialog.dialog.dismiss()


                            }
                        } else {
                            showToast(this@PendingApprovalActivity, "Operation failed ! ", 2)
                            progressDialog.dialog.dismiss()

                        }
                    }

                    override fun onFailure(call: Call<GetJobDetailsByStoreManager>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(this@PendingApprovalActivity, "Operation failed ! ", 2)

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