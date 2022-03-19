package com.softland.choithrams.activitys

import NewJobListAdapter
import android.Manifest
import android.app.AlertDialog
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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.entitys.Rule_details
import com.softland.choithrams.entitys.Sales_rate_details
import com.softland.choithrams.entitys.Stock_details
import com.softland.choithrams.interfaces.OnClickListners
import com.softland.choithrams.networks.ChoithramsAPI
import com.softland.choithrams.requestobjects.*
import com.softland.choithrams.retrofitresponses.GetAllRules
import com.softland.choithrams.retrofitresponses.GetAllSalesRates
import com.softland.choithrams.retrofitresponses.GetAllStockItems
import com.softland.choithrams.viewmodel.SectionViewModel
import com.softland.choithrams.viewmodel.StockViewModel
import com.softland.choithrams.views.CustomProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class NewJobHomeScreenActivity : AppCompatActivity(), OnClickListners {
    private val db by lazy { ChoithramDB(this@NewJobHomeScreenActivity) }
    val TAG = "NewJobHomeScreenActivity"
    private var count = MutableLiveData<Int>()
    private val adapter: NewJobListAdapter by lazy { NewJobListAdapter(this) }
    val sectionViewModel: SectionViewModel = SectionViewModel(db.sectionDetailsDao())
    val stockViewModel: StockViewModel = StockViewModel(db.stockDetailsDao())

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var userid = 0
    var storeid = 0
    var Sectionid = 0
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val progressDialog = CustomProgressDialog()
    val PERMISSION_ID = 1010


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_job_home_screen)

        val item_search_input: EditText = findViewById(R.id.item_search_input)
        val item_clear_click_parent: RelativeLayout = findViewById(R.id.item_clear_click_parent)
        val recyclerview = findViewById<RecyclerView>(R.id.recycler_view_new_job)
        recyclerview.layoutManager = LinearLayoutManager(this@NewJobHomeScreenActivity)
        recyclerview.adapter = adapter
        adapter.setListner(this@NewJobHomeScreenActivity)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        RequestPermission()
        getLastLocation()

        sectionViewModel.readData.observe(this) {
            adapter.setData(it)
        }

        CoroutineScope(Dispatchers.Main).launch {
            userid = db.loginDetailsDao().getAll().get(0).UserID
            storeid = db.loginDetailsDao().getAll().get(0).StoreID
        }

        item_search_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (item_search_input.length() > 0) {
                    if (item_clear_click_parent.visibility != View.VISIBLE) {
                        item_clear_click_parent.visibility = View.VISIBLE
                    }
                    if (!item_search_input.text.toString().trim().isNullOrBlank()) {
                        searchDatabase(item_search_input.text.toString().trim())
                    }
                } else {
                    item_clear_click_parent.visibility = View.GONE
                    searchDatabase("")
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
        val intent = Intent(this, PickingScreenActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(sectioncode: String, sectionName: String, jobNumber: String,str :String,sts :Int) {
        TODO("Not yet implemented")
    }

    override fun onClick(sectioncode: String, sectionName: String) {
        showStartNewJobDialog(sectioncode, sectionName)
    }

    private fun showStartNewJobDialog(sectioncode: String, sectionName: String) {
        var dialog = AlertDialog.Builder(this).create()
        val factory = LayoutInflater.from(this)
        val dialogView: View = factory.inflate(R.layout.layout_confirm_dialog, null)
        dialog.setView(dialogView)
        dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
            .setOnClickListener(View.OnClickListener {
                dialog.dismiss()
            })
        dialogView.findViewById<MaterialButton>(R.id.btn_ok)
            .setOnClickListener(View.OnClickListener {
                dialog.dismiss()

                val sharedPref = getSharedPreferences("JobDetails", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("JobStartDate",getCurrentDate())
                    putString("FinalJobNumber","")
                    apply()
                }

                Thread {
                    db.cartedItemDetailsDao().clear()
                    stockViewModel.clear()
                    db.salesRateDetailsDao().clear()
                    db.ruleDetailsDao().clear()
                }.start()

                fetchSectionideID(sectioncode,sectionName)
            })
        dialogView.findViewById<TextView>(R.id.txt_main_tittle).text = "START NEW JOB"
        dialogView.findViewById<TextView>(R.id.txt_sub_tittle).text =
            "$sectioncode  |  $sectionName"
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 50)
        dialog.window?.setBackgroundDrawable(inset)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun searchDatabase(searchtext: String) {
        sectionViewModel.searchDatabase(searchtext).observe(this, { list ->
            list.let {
                adapter.setData(it)

            }
        })
    }

    private fun fetchSectionideID(sectioncode: String,sectionName: String){
        sectionViewModel.searchBySectionCodeAndSectionName(sectioncode,sectionName).observe(this, { list ->
            list.let {
                Sectionid=it.get(0).SectionID

                val sharedPref = getSharedPreferences("SectionDetails", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putInt("SectionId", Sectionid)
                    apply()
                }

                getAllStockItems()

            }
        })
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

    private fun getAllStockItems() {
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
            "GetAllStockItems",
            "",
            userid,
            1,
            storeid
        )

        val requestDataForGetAllStockItems= RequestDataForGetAllStockItems(Sectionid,storeid,userid,"")

       val getAllStockItemsRequest=GetAllStockItemsRequest(credentials,requestDataForGetAllStockItems)

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .getAllStockItems(getAllStockItemsRequest)
                .enqueue(object : Callback<GetAllStockItems> {
                    override fun onResponse(
                        call: Call<GetAllStockItems>,
                        response: Response<GetAllStockItems>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                if (url == 1) {
                                    if (response.body()?.ResponseData!!.StockItem.isNotEmpty()) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            db.stockDetailsDao().saveAll(response.body()?.ResponseData!!.StockItem)
                                        }
                                        getAllSalesRates()
                                    } else {
                                        showToast(this@NewJobHomeScreenActivity, "Stock items not found ! ", 2)
                                        progressDialog.dialog.dismiss()
                                    }

                                } else {
                                    showToast(
                                        this@NewJobHomeScreenActivity,
                                        "" + response.body()?.StatusReturn!!.Message.toString(),
                                        2
                                    )
                                    progressDialog.dialog.dismiss()

                                }
                            } else {
                                showToast(this@NewJobHomeScreenActivity, "Operation failed ! ", 2)
                                progressDialog.dialog.dismiss()

                            }
                        } else {
                            showToast(this@NewJobHomeScreenActivity, "Operation failed ! ", 2)
                            progressDialog.dialog.dismiss()

                        }
                    }

                    override fun onFailure(call: Call<GetAllStockItems>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(this@NewJobHomeScreenActivity, "Operation failed ! ", 2)

                    }
                })
        }
    }

    private fun getAllSalesRates() {
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
            "GetAllSalesRates",
            "",
            userid,
            1,
            storeid
        )

        val requestDataForGetAllStockItems= RequestDataForGetAllStockItems(Sectionid,storeid,userid,"")

        val getAllSalesRatesRequest=GetAllSalesRatesRequest(credentials,requestDataForGetAllStockItems)

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .getAllSalesRates(getAllSalesRatesRequest)
                .enqueue(object : Callback<GetAllSalesRates> {
                    override fun onResponse(
                        call: Call<GetAllSalesRates>,
                        response: Response<GetAllSalesRates>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                if (url == 1) {
                                    if (response.body()?.ResponseData!!.SalesRates.isNotEmpty()) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            db.salesRateDetailsDao().saveAll(response.body()?.ResponseData!!.SalesRates)
                                        }
                                        getAllRules()
                                    } else {
                                        showToast(this@NewJobHomeScreenActivity, "Stock items not found ! ", 2)
                                        progressDialog.dialog.dismiss()
                                    }

                                } else {
                                    showToast(
                                        this@NewJobHomeScreenActivity,
                                        "" + response.body()?.StatusReturn!!.Message.toString(),
                                        2
                                    )
                                    progressDialog.dialog.dismiss()

                                }
                            } else {
                                showToast(this@NewJobHomeScreenActivity, "Operation failed ! ", 2)
                                progressDialog.dialog.dismiss()

                            }
                        } else {
                            showToast(this@NewJobHomeScreenActivity, "Operation failed ! ", 2)
                            progressDialog.dialog.dismiss()

                        }
                    }

                    override fun onFailure(call: Call<GetAllSalesRates>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(this@NewJobHomeScreenActivity, "Operation failed ! ", 2)

                    }
                })
        }
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }


    private fun getAllRules() {
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
            "GetAllRules",
            "",
            userid,
            1,
            storeid
        )

        val requestDataForGetAllRules = RequestDataForGetAllRules(Sectionid, storeid, userid)

        val getAllRulesRequest = GetAllRulesRequest(credentials, requestDataForGetAllRules)

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .getAllRules(getAllRulesRequest)
                .enqueue(object : Callback<GetAllRules> {
                    override fun onResponse(
                        call: Call<GetAllRules>,
                        response: Response<GetAllRules>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                if (url == 1) {
                                    if (response.body()?.ResponseData!!.Rules.isNotEmpty()) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            var i = 1
                                            for (obj in response.body()?.ResponseData!!.Rules) {
                                                db.ruleDetailsDao().save(
                                                    Rule_details(
                                                        i++,
                                                        obj.Discount,
                                                        obj.MCID,
                                                        obj.RemainingDays,
                                                        obj.RuleID,
                                                        obj.RuleCode,
                                                        obj.RuleName,
                                                    )
                                                )
                                            }

                                            val sharedPref = getSharedPreferences("choithrams",Context.MODE_PRIVATE)
                                            with(sharedPref.edit()) {
                                                putBoolean("fromSavedJob", false)
                                                apply()
                                            }
                                            progressDialog.dialog.dismiss()
                                            val intent = Intent(this@NewJobHomeScreenActivity, ScanBarcodeActivity::class.java)
                                            startActivity(intent)
                                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                            finish()


                                        }
                                    } else {
                                        showToast(
                                            this@NewJobHomeScreenActivity,
                                            "Rule not found ! ",
                                            2
                                        )
                                        progressDialog.dialog.dismiss()
                                    }

                                } else {
                                    showToast(
                                        this@NewJobHomeScreenActivity,
                                        "" + response.body()?.StatusReturn!!.Message.toString(),
                                        2
                                    )
                                    progressDialog.dialog.dismiss()

                                }
                            } else {
                                showToast(this@NewJobHomeScreenActivity, "Operation failed ! ", 2)
                                progressDialog.dialog.dismiss()

                            }
                        } else {
                            showToast(this@NewJobHomeScreenActivity, "Operation failed ! ", 2)
                            progressDialog.dialog.dismiss()

                        }
                    }

                    override fun onFailure(call: Call<GetAllRules>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(this@NewJobHomeScreenActivity, "Operation failed ! ", 2)

                    }
                })
        }
    }




}