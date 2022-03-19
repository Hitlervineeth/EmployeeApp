package com.softland.choithrams.activitys

import CartedItemListAdapter
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.widget.*
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
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.entitys.Carted_item_details
import com.softland.choithrams.networks.ChoithramsAPI
import com.softland.choithrams.requestobjects.*
import com.softland.choithrams.retrofitresponses.InsertJobDetails
import com.softland.choithrams.viewmodel.CartedItemViewModel
import com.softland.choithrams.viewmodel.StockViewModel
import com.softland.choithrams.views.CustomProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class ScanBarcodeActivity : AppCompatActivity() {

    val TAG = "ScanBarcodeActivity"
    var fromSavedJob: Boolean? = null
    lateinit var scanner_type: RadioGroup
    lateinit var external_scanner: RadioButton
    lateinit var internal_scanner: RadioButton
    lateinit var text_input_field_layout: TextInputLayout
    lateinit var text_filed_barcode_number: TextInputEditText
    lateinit var carted_btn: CardView
    lateinit var checkbox_manual_search: CheckBox
    lateinit var saved_item_details: RelativeLayout
    private val db by lazy { ChoithramDB(this@ScanBarcodeActivity) }
    val stockViewModel: StockViewModel = StockViewModel(db.stockDetailsDao())
    val cartedItemViewModel: CartedItemViewModel = CartedItemViewModel(db.cartedItemDetailsDao())
    val adapter: CartedItemListAdapter by lazy { CartedItemListAdapter(this@ScanBarcodeActivity) }
    lateinit var dialog: BottomSheetDialog
    lateinit var broadCastReceiver: BroadcastReceiver
    private val progressDialog = CustomProgressDialog()

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val PERMISSION_ID = 1010

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var userid = 0
    var storeid = 0
    var currency = ""
    var Sectionid = 0
    var itemRate = 0.0;
    var BarcodeID = 0;
    var StockID = 0;
    var UnitID = 0;
    var RuleId = 0;
    var Discount = 0.00;
    var RemainingDays = 0;
    var BarcodeName = "";
    var MCID = ""
    var mode_off_roundoff = ""
    var roundoff_value = 0;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.softland.choithrams.R.layout.activity_scan_barcode)
        val sharedPref = getSharedPreferences("choithrams", Context.MODE_PRIVATE)
        fromSavedJob = sharedPref.getBoolean("fromSavedJob", false)
        var scanner = sharedPref.getString("ScannerType", "Internal")
        var manualEntry = sharedPref.getBoolean("ManualEntry", false)
        scanner_type = findViewById(R.id.scanner_type)
        external_scanner = findViewById(R.id.external_scanner)
        internal_scanner = findViewById(R.id.internal_scanner)
        saved_item_details = findViewById(R.id.saved_item_details)
        carted_btn = findViewById(R.id.carted_btn)
        checkbox_manual_search = findViewById(R.id.checkbox_manual_search)
        text_input_field_layout = findViewById(R.id.text_input_field_layout)
        text_filed_barcode_number = findViewById(R.id.text_filed_barcode_number)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        RequestPermission()
        getLastLocation()

        CoroutineScope(Dispatchers.Main).launch {

            userid = db.loginDetailsDao().getAll().get(0).UserID
            storeid = db.loginDetailsDao().getAll().get(0).StoreID
            currency = db.loginDetailsDao().getAll().get(0).Currency
            roundoff_value = db.loginDetailsDao().getAll().get(0).RoundOffLimit.toInt()
            mode_off_roundoff = db.loginDetailsDao().getAll().get(0).RoundOff
            Sectionid =
                getSharedPreferences("SectionDetails", Context.MODE_PRIVATE).getInt("SectionId", 0);
        }

        if (manualEntry) {
            checkbox_manual_search.isChecked = manualEntry
            text_input_field_layout.visibility = View.VISIBLE
        } else {
            checkbox_manual_search.isChecked = manualEntry
            text_input_field_layout.visibility = View.GONE
        }

        if (scanner.equals("Internal")) {
            internal_scanner.isChecked = true
        } else {
            external_scanner.isChecked = true
        }

        val intentFilter = IntentFilter();
        intentFilter.addAction("nlscan.action.SCANNER_RESULT")

        broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                var scanResult_1 = ""
                var ScanStatus = ""
                scanResult_1 = intent?.getStringExtra("SCAN_BARCODE1").toString()
                ScanStatus = intent?.getStringExtra("SCAN_STATE").toString()
                try {
                    progressDialog.dialog.dismiss()
                } catch (e: Exception) {
                }
                Log.d("abcd", "Barcode : ScanStatus = $ScanStatus")
                Log.d("abcd", "Barcode : scanResult_1 = $scanResult_1")

                if ("ok".equals(ScanStatus)) {
                    searchByBarcodeNumber(scanResult_1)
                } else if (ScanStatus == null) {
                    showToast(this@ScanBarcodeActivity, "Operation failed ! ", 2)
                } else {
                    showToast(this@ScanBarcodeActivity, "Invalid barcode ! ", 2)
                }
            }
        }

        registerReceiver(broadCastReceiver, intentFilter);

        scanner_type.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                com.softland.choithrams.R.id.external_scanner -> {
                    getSharedPreferences("choithrams", Context.MODE_PRIVATE).edit()
                        .putString("ScannerType", "External")
                        .commit()
                }
                com.softland.choithrams.R.id.internal_scanner -> {
                    getSharedPreferences("choithrams", Context.MODE_PRIVATE).edit()
                        .putString("ScannerType", "Internal")
                        .commit()

                }

            }
        })

        CoroutineScope(Dispatchers.Main).launch {
            adapter.setCurrency(db.loginDetailsDao().getAll()[0].Currency)
            cartedItemViewModel.getAllScannedItems().observe(this@ScanBarcodeActivity, { list ->
                list.let {
                    if (it.isNotEmpty()) {
                        saved_item_details.visibility = View.VISIBLE
                    } else {
                        saved_item_details.visibility = View.GONE
                    }
                }
            })
        }
        checkbox_manual_search.setOnClickListener(View.OnClickListener {
            if (checkbox_manual_search.isChecked) {
                text_input_field_layout.visibility = View.VISIBLE
                getSharedPreferences("choithrams", Context.MODE_PRIVATE).edit()
                    .putBoolean("ManualEntry", true)
                    .commit()
            } else {
                text_input_field_layout.visibility = View.GONE
                getSharedPreferences("choithrams", Context.MODE_PRIVATE).edit()
                    .putBoolean("ManualEntry", false)
                    .commit()
            }
        })

        text_input_field_layout.setEndIconOnClickListener(View.OnClickListener {
            if (text_filed_barcode_number.text.toString().trim().isNullOrEmpty()) {
                showToast(applicationContext, "Invalid Barcode number", 2)
            } else {
                searchByBarcodeNumber(text_filed_barcode_number.text.toString().trim())
            }
        })
    }

    override fun onStart() {
        super.onStart()

        try {
            dialog.dismiss()
            dialog.dismiss()
            dialog.dismiss()
            dialog.dismiss()
            dialog.dismiss()
            dialog.dismiss()
        } catch (e: Exception) {

        }
    }

    override fun onBackPressed() {
        var load = true
        CoroutineScope(Dispatchers.Main).launch {
            cartedItemViewModel.getAll().observe(this@ScanBarcodeActivity, { list ->
                list.let {
                    if (it.isNotEmpty()) {

                        val builder = AlertDialog.Builder(this@ScanBarcodeActivity)
                        builder.setTitle("Warning!")
                        builder.setMessage("Do you want to keep the Scanned items ?")
                            .setCancelable(true)
                            .setNegativeButton("Cancel & exit") { dialog, id ->
                                dialog.dismiss()
                                val builder = AlertDialog.Builder(this@ScanBarcodeActivity)
                                builder.setTitle("Warning!")
                                builder.setMessage("If cancelled, all currently scanned items will be lost. Do you want to cancel ?")
                                    .setCancelable(true)
                                    .setPositiveButton("Cancel") { dialog, id ->
                                        val sharedPref =
                                            getSharedPreferences("choithrams", Context.MODE_PRIVATE)
                                        fromSavedJob = sharedPref.getBoolean("fromSavedJob", false)
                                        if (fromSavedJob == true) {
                                            val intent = Intent(
                                                this@ScanBarcodeActivity,
                                                SaveJobHomeScreenActivity::class.java
                                            )
                                            startActivity(intent)
                                            finish()
                                            overridePendingTransition(
                                                R.anim.slide_in_left,
                                                R.anim.slide_out_right
                                            )
                                        } else {
                                            val intent = Intent(
                                                this@ScanBarcodeActivity,
                                                NewJobHomeScreenActivity::class.java
                                            )
                                            startActivity(intent)
                                            finish()
                                            overridePendingTransition(
                                                R.anim.slide_in_left,
                                                R.anim.slide_out_right
                                            )
                                        }
                                    }
                                    .setNegativeButton("No") { dialog, id ->
                                        dialog.dismiss()
                                    }
                                val alert = builder.create()
                                alert.show()
                            }
                            .setPositiveButton("YEs") { dialog, id ->
                                dialog.dismiss()
                                showEndJobtDialog(it)
                            }
                        val alert = builder.create()
                        alert.show()

                    } else {
                        val builder = AlertDialog.Builder(this@ScanBarcodeActivity)
                        builder.setTitle("Warning!")
                        builder.setMessage("Do you want to cancel this job ?")
                            .setCancelable(true)
                            .setPositiveButton("Cancel") { dialog, id ->
                                val sharedPref =
                                    getSharedPreferences("choithrams", Context.MODE_PRIVATE)
                                fromSavedJob = sharedPref.getBoolean("fromSavedJob", false)
                                if (fromSavedJob == true) {
                                    val intent = Intent(
                                        this@ScanBarcodeActivity,
                                        SaveJobHomeScreenActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                    overridePendingTransition(
                                        R.anim.slide_in_left,
                                        R.anim.slide_out_right
                                    )
                                } else {
                                    val intent = Intent(
                                        this@ScanBarcodeActivity,
                                        NewJobHomeScreenActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                    overridePendingTransition(
                                        R.anim.slide_in_left,
                                        R.anim.slide_out_right
                                    )
                                }
                            }
                            .setNegativeButton("No") { dialog, id ->
                                dialog.dismiss()
                            }
                        val alert = builder.create()
                        alert.show()
                    }
                }
            })
        }
    }

    private fun showEndJobtDialog(carted_item_details: List<Carted_item_details>) {
        var dialog = android.app.AlertDialog.Builder(this@ScanBarcodeActivity).create()
        val factory = LayoutInflater.from(this@ScanBarcodeActivity)
        val dialogView: View = factory.inflate(R.layout.layout_confirm_dialog_for_end_job, null)
        dialog.setView(dialogView)
        dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
            .setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                InsertJobs(1, carted_item_details)
            })
        dialogView.findViewById<MaterialButton>(R.id.btn_ok)
            .setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                InsertJobs(2, carted_item_details)
            })
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 50)
        dialog.window?.setBackgroundDrawable(inset)
        dialog.setCancelable(true)
        dialog.show()
    }

    fun scanBarCode(view: View) {
        Log.d("abcd", " android.os.Build.MODEL  : " + android.os.Build.MODEL)

        if (internal_scanner.isChecked) {
            val intentIntegrator = IntentIntegrator(this)
            intentIntegrator.captureActivity = BarcodeCaptureActivity::class.java
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
            intentIntegrator.setPrompt("Scanning code...")
            intentIntegrator.initiateScan()
        } else {

            if (android.os.Build.MODEL.toString().equals("NLS-NFT10")) {

                progressDialog.show(this, "Scanning Wait...")
                var intent = Intent("ACTION_BARCODE_CFG");
                intent.putExtra("CODE_ID", "CODE128");
                intent.putExtra("PROPERTY", "Enable");
                intent.putExtra("VALUE", "1"); // “1” Enable EAN-8, ”0” Disable EAN-8
                sendBroadcast(intent);

                var intent2 = Intent("nlscan.action.SCANNER_TRIG")
                intent2.putExtra("SCAN_TIMEOUT", 5) // SCAN_TIMEOUT value: int, 1-9; unit: second
                intent2.putExtra(
                    "SCAN_TYPE ",
                    1
                ) // SCAN_TYPE: read two barcodes during a scan attempt
                sendBroadcast(intent2)

            } else {
                showToast(this@ScanBarcodeActivity, "External scanner not support ! ", 2)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val intentresult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (intentresult != null && resultCode == -1) {
            var barcodeNo: String = intentresult.contents
            if (barcodeNo.isNullOrBlank()) {
                showToast(this, "Invalid barcode ! ", 2)
            } else {
                searchByBarcodeNumber(barcodeNo)
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun searchByBarcodeNumber(barcodeNumber: String) {
        stockViewModel.searchByBarcode(barcodeNumber).observe(this, { list ->
            list.let {
                if (it.isEmpty()) {
                    showToast(this, "Invalid barcode ! ", 2)
                } else {
                    val intent = Intent(this, ItemDetailsActivity::class.java)
                    intent.putExtra("BarcodeNumber", barcodeNumber)
                    intent.putExtra("StockID", "1111111111111")
                    intent.putExtra("ItemRate", "1111111111111")
                    intent.putExtra("BarcodeID", "1111111111111")
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }
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

    override fun onDestroy() {
        unregisterReceiver(broadCastReceiver)
        super.onDestroy()
    }

    private fun InsertJobs(status: Int, carted_item_details: List<Carted_item_details>) {

        progressDialog.show(this@ScanBarcodeActivity, "Please Wait...")

        Log.d(TAG, "Your  longitude :$longitude")
        Log.d(TAG, "Your  latitude:$latitude")
        Log.d(TAG, "Your  altitude:$altitude")
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
            "InsertJobDetails",
            "",
            0,
            1,
            0
        )

        var jobDetails_list = ArrayList<JobDetail>()
        var jobRule_list = ArrayList<JobRule>()
        var Checksum = 0.00
        var GrossAmount = 0.00
        var NetAmoount = 0.00

        val sharedPref = getSharedPreferences("JobDetails", Context.MODE_PRIVATE)
        val JobStartDate = sharedPref.getString("JobStartDate", getCurrentDateAndTime())
        val JobPrefix = sharedPref.getString("JobNumberPrefix", "ABCD")
        var JobNumber = sharedPref.getInt("JobNumber", 1)
        var finalJobNumber = sharedPref.getString("FinalJobNumber", "")

        if (finalJobNumber.equals("", true)) {
            JobNumber++
            finalJobNumber = "0000000$JobNumber"
            finalJobNumber =
                finalJobNumber.substring(finalJobNumber.length - 5, finalJobNumber.length)
            finalJobNumber = JobPrefix + finalJobNumber
        }


        for (obj in carted_item_details) {
            jobDetails_list.add(
                JobDetail(
                    obj.ActualRate,
                    obj.BarCodeID,
                    obj.ExpiryDate,
                    obj.GrossAmount,
                    obj.NetAmount,
                    obj.Quantity,
                    obj.Rate,
                    obj.RoundOff,
                    obj.RuleRate,
                    obj.StockID,
                    obj.UnitID
                )
            )

            jobRule_list.add(
                JobRule(
                    obj.BarCodeID,
                    obj.Discount,
                    obj.ExpiryDate,
                    obj.Quantity,
                    obj.RemainingDays,
                    obj.RuleID,
                    obj.StockID,
                    obj.UnitID
                )
            )
            GrossAmount += obj.GrossAmount
            NetAmoount += obj.NetAmount
            Checksum += obj.Quantity * obj.StockID

        }

        val insertJobDetailsRequest = InsertJobDetailsRequest(
            String.format("%.3f", Checksum).toDouble(),
            GrossAmount,
            getCurrentDateAndTime(),
            jobDetails_list,
            getCurrentDateAndTime(),
            finalJobNumber.toString(),
            jobRule_list,
            JobStartDate.toString(),
            status,
            NetAmoount,
            jobDetails_list.size,
            jobRule_list.size,
            " ",
            Sectionid,
            storeid,
            userid
        )

        val insertJobRequest = InsertJobRequest(credentials, insertJobDetailsRequest)

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .insertJobDetails(insertJobRequest)
                .enqueue(object : Callback<InsertJobDetails> {
                    override fun onResponse(
                        call: Call<InsertJobDetails>,
                        response: Response<InsertJobDetails>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, response.body().toString())
                            if (response.body()?.StatusReturn != null && response.body()?.ResponseData != null) {
                                var url = response.body()?.StatusReturn!!.Status
                                if (url == 1) {

                                    if (response.body()?.ResponseData!!.JobNumber.equals(
                                            finalJobNumber,
                                            true
                                        )
                                    ) {
                                        if (status == 1) {
                                            showToast(
                                                this@ScanBarcodeActivity,
                                                "Job ended successfully",
                                                1
                                            )
                                        } else {
                                            showToast(
                                                this@ScanBarcodeActivity,
                                                "Job saved successfully",
                                                1
                                            )
                                        }

                                        val sharedPref =
                                            getSharedPreferences("JobDetails", Context.MODE_PRIVATE)
                                        var finalJobNumber =
                                            sharedPref.getString("FinalJobNumber", "")
                                        if (finalJobNumber.equals("", true)) {
                                            getSharedPreferences(
                                                "JobDetails",
                                                Context.MODE_PRIVATE
                                            ).edit().putInt("JobNumber", JobNumber).apply()
                                        } else {
                                            getSharedPreferences(
                                                "JobDetails",
                                                Context.MODE_PRIVATE
                                            ).edit().putString("FinalJobNumber", "").apply()
                                        }

                                        progressDialog.dialog.dismiss()
                                        val intent = Intent(
                                            this@ScanBarcodeActivity,
                                            HomeScreenPickerActivity::class.java
                                        )
                                        startActivity(intent)
                                        overridePendingTransition(
                                            R.anim.slide_in_right,
                                            R.anim.slide_out_left
                                        )
                                        finish()
                                    } else {
                                        progressDialog.dialog.dismiss()
                                        showToast(applicationContext, "Operation failed ! ", 2)
                                        showEndJobtDialog(carted_item_details)
                                    }

                                } else {
                                    showToast(
                                        applicationContext,
                                        "" + response.body()?.StatusReturn!!.Message.toString(),
                                        2
                                    )
                                    progressDialog.dialog.dismiss()
                                    showEndJobtDialog(carted_item_details)
                                }
                            } else {
                                showToast(applicationContext, "Operation failed ! ", 2)
                                progressDialog.dialog.dismiss()
                                showEndJobtDialog(carted_item_details)
                            }
                        } else {
                            showToast(applicationContext, "Operation failed ! ", 2)
                            progressDialog.dialog.dismiss()
                            showEndJobtDialog(carted_item_details)
                        }
                    }

                    override fun onFailure(call: Call<InsertJobDetails>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(applicationContext, "Operation failed ! ", 2)
                        showEndJobtDialog(carted_item_details)
                    }
                })
        }
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(Date())
    }

    fun getCurrentDateAndTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
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

    fun shoeCartedList(view: View) {
        CoroutineScope(Dispatchers.Main).launch {
            cartedItemViewModel.getAllScannedItems().observe(this@ScanBarcodeActivity, { list ->
                list.let {
                    val view1: View =
                        layoutInflater.inflate(R.layout.layout_bottom_dilalog_for_carted_list, null)
                    dialog = BottomSheetDialog(this@ScanBarcodeActivity)
                    dialog.setContentView(view1)
                    val recyclerview =
                        view1.findViewById<RecyclerView>(R.id.recycler_view_live_jobs)
                    val llm = LinearLayoutManager(this@ScanBarcodeActivity)
                    llm.orientation = LinearLayoutManager.VERTICAL
                    recyclerview.setLayoutManager(llm)
                    recyclerview.adapter = adapter
                    adapter.setData(it)
                    if (!dialog.isShowing) {
                        dialog.show()
                    }
                }
            })
        }
    }

}