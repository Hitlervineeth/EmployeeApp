package com.softland.choithrams.activitys

import RulesListAdapter
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
import android.text.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.entitys.Carted_item_details
import com.softland.choithrams.entitys.Rule_details
import com.softland.choithrams.networks.ChoithramsAPI
import com.softland.choithrams.requestobjects.*
import com.softland.choithrams.retrofitresponses.GetAllRules
import com.softland.choithrams.retrofitresponses.InsertJobDetails
import com.softland.choithrams.viewmodel.CartedItemViewModel
import com.softland.choithrams.viewmodel.RuleViewModel
import com.softland.choithrams.viewmodel.StockViewModel
import com.softland.choithrams.views.CustomProgressDialog
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs


class ItemDetailsActivity : AppCompatActivity() {
    private val outputdateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val outputdateFormat2 = SimpleDateFormat("dd/MM/yyyy")
    lateinit var txt_expiry_date: TextView
    lateinit var txt_qty: TextView
    lateinit var btn_submit: MaterialButton
    lateinit var txt_rule_price: TextView
    lateinit var txt_item_rsp: TextView
    lateinit var txt_round_off_price: TextView
    lateinit var txt_unit_name: TextView

    lateinit var txt_currancy2: TextView
    lateinit var txt_currancy1: TextView
    lateinit var txt_currancy: TextView
    lateinit var txt_barcode: TextView


    val TAG = "ItemDetailsActivity"
    private val db by lazy { ChoithramDB(this@ItemDetailsActivity) }
    val stockViewModel: StockViewModel = StockViewModel(db.stockDetailsDao())
    val ruleViewModel: RuleViewModel = RuleViewModel(db.ruleDetailsDao())
    val cartedItemViewModel: CartedItemViewModel = CartedItemViewModel(db.cartedItemDetailsDao())
    private val adapter: RulesListAdapter by lazy { RulesListAdapter(this) }
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val progressDialog = CustomProgressDialog()
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
    var CurentDate="";
    var mode_off_roundoff = ""
    var roundoff_value = 0;
    var showdialog=true
    var isKgItem=true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)
        txt_expiry_date = findViewById(R.id.txt_expiry_date)
        txt_item_rsp = findViewById(R.id.txt_item_rsp)
        txt_rule_price = findViewById(R.id.txt_rule_price)
        txt_round_off_price = findViewById(R.id.txt_round_off_price)
        txt_currancy2 = findViewById(R.id.txt_currancy2)
        txt_currancy1 = findViewById(R.id.txt_currancy1)
        txt_currancy = findViewById(R.id.txt_currancy)
        txt_unit_name = findViewById(R.id.txt_unit_name)
        txt_barcode=findViewById(R.id.txt_barcode)


        txt_qty = findViewById(R.id.txt_qty)
        btn_submit = findViewById<MaterialButton>(R.id.btn_submit)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        RequestPermission()
        getLastLocation()

        val recyclerview = findViewById<RecyclerView>(R.id.recylerview_rules)
        recyclerview.layoutManager = LinearLayoutManager(this@ItemDetailsActivity)
        recyclerview.adapter = adapter


        var txt_category_name: TextView = findViewById<TextView>(R.id.txt_category_name);
        var txt_item_name: TextView = findViewById<TextView>(R.id.txt_item_name);

        txt_expiry_date.text = "00/00/0000"
        CurentDate="0000-00-00"
        txt_qty.text = "0"

        btn_submit.isEnabled = false
        adapter.select_rule_id(0)

        intent.getStringExtra("BarcodeNumber")?.let { str ->
            stockViewModel.searchByBarcode(str).observe(this, { list ->
                list.let {
                    txt_category_name.text = it[0].MCName
                    txt_item_name.text = it[0].StockName
                    txt_unit_name.text = it[0].UnitCode
                    UnitID = it[0].UnitID
                    if(it[0].UnitCode.toString().trim().equals("KG",true) || it[0].UnitName.toString().trim().equals("KG",true)){
                        isKgItem=true
                        txt_qty.setFilters(arrayOf<InputFilter>(DecimalDigitsInputFilter(7, 3)))
                    }else{
                        isKgItem=false
                        txt_qty.inputType=InputType.TYPE_CLASS_NUMBER
                        txt_qty.setFilters(arrayOf<InputFilter>(DecimalDigitsInputFilter(7, 0)))
                    }
                    setAsZero()
                    MCID = it[0].MCID.toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        ruleViewModel.searchByMCID(MCID).observe(this@ItemDetailsActivity, { list ->
                            list.let {
                                if (it.isNotEmpty()) {
                                    adapter.setData(it,0)
                                } else {
                                    var rulelist= emptyList<Rule_details>()
                                    adapter.setData(rulelist,0)
                                    showToast(this@ItemDetailsActivity, "Rule not found ! ", 2)
                                }

                                showdialog=false

                            }
                        })
                    }


                }
            })
        }

        intent.getStringExtra("BarcodeNumber")?.let { str ->
            stockViewModel.searchSalesRateByBarcode(str).observe(this, { list ->
                list.let {
                    itemRate = it[0].Rate.toDouble()
                    txt_item_rsp.text = String.format("%.2f", itemRate)
                    BarcodeID = it[0].BarCodeID;
                    BarcodeName = it[0].BarCode
                    StockID = it[0].StockID
                    txt_barcode.text=BarcodeName
                    setAsZero()

                }
            })
        }

        CoroutineScope(Dispatchers.Main).launch {

            userid = db.loginDetailsDao().getAll().get(0).UserID
            storeid = db.loginDetailsDao().getAll().get(0).StoreID
            currency = db.loginDetailsDao().getAll().get(0).Currency
            roundoff_value= db.loginDetailsDao().getAll().get(0).RoundOffLimit.toInt()
            mode_off_roundoff = db.loginDetailsDao().getAll().get(0).RoundOff

            txt_currancy2.text = currency
            txt_currancy1.text = currency
            txt_currancy.text = currency

            Sectionid = getSharedPreferences("SectionDetails", Context.MODE_PRIVATE).getInt("SectionId", 0);
        }

        txt_qty.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (txt_qty.length() > 0) {
                    if (txt_qty.text.toString().contentEquals(",")) {
                        setAsZero()
                    } else {
                        if (txt_qty.text.toString().trim().isNullOrBlank()) {
                            txt_qty.text = ""
                            setAsZero()
                        } else {
                            txt_item_rsp.text = String.format("%.2f", itemRate)
                            try {
                                if (txt_qty.text.toString().trim().toDouble() > 0) {
                                    searchRuleByMCID(MCID, false)
                                } else {
                                    setAsZero()
                                }

                            } catch (e: Exception) {
                                setAsZero()
                            }
                        }
                    }
                } else {
                    setAsZero()
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        btn_submit.setOnClickListener(View.OnClickListener {
            var load = true
            CoroutineScope(Dispatchers.Main).launch {
                var details = db.cartedItemDetailsDao().getAllByStockIDAndExpryDate(StockID,CurentDate.trim() + " 00:00:00")
                if (details.isNotEmpty()) {

                    cartedItemViewModel.updateQuantityByStockIDAndExpryDate(
                        StockID,CurentDate.trim() + " 00:00:00",
                        String.format(getdecimalPointCount(), txt_qty.text.toString().toDouble()).toDouble(),
                        String.format(getdecimalPointCount(), txt_qty.text.toString().toDouble() * txt_rule_price.text.toString().toDouble()).toDouble(),
                        String.format(getdecimalPointCount(), txt_qty.text.toString().toDouble() * txt_rule_price.text.toString().toDouble()).toDouble()
                    ).observe(this@ItemDetailsActivity, { list ->
                        list.let {
                            if (it.isNotEmpty()) {
                                if (load) {
                                    load = false

                                    showToast(this@ItemDetailsActivity, "Successfully saved.", 1)
                                    val intent = Intent(this@ItemDetailsActivity, ScanBarcodeActivity::class.java)
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                    finish()
                                   // showScanNextDialog(it)
                                }
                            }
                        }
                    })

                } else {
                    cartedItemViewModel.save(
                        Carted_item_details(
                            0,
                            StockID,
                            UnitID,
                            BarcodeID,
                            String.format(getdecimalPointCount(), txt_qty.text.toString().toDouble()).toDouble(),
                            txt_item_rsp.text.toString().toDouble(),
                            txt_rule_price.text.toString().toDouble(),
                            String.format("%.2f", txt_round_off_price.text.toString().toDouble() - txt_rule_price.text.toString().toDouble()).toDouble(),
                            txt_round_off_price.text.toString().toDouble(),
                            String.format( getdecimalPointCount(), txt_qty.text.toString().toDouble() * txt_round_off_price.text.toString().toDouble()).toDouble(),
                            String.format( getdecimalPointCount(), txt_qty.text.toString().toDouble() * txt_round_off_price.text.toString().toDouble()).toDouble(),
                            RuleId,
                            CurentDate.trim() + " 00:00:00",
                            RemainingDays,
                            Discount
                        )
                    ).observe(this@ItemDetailsActivity, { list ->
                        list.let {
                            if (it.isNotEmpty()) {
                                if (load) {
                                    load = false
                                    showToast(this@ItemDetailsActivity, "Successfully saved.", 1)
                                    val intent = Intent(this@ItemDetailsActivity, ScanBarcodeActivity::class.java)
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                    finish()

                                    //showScanNextDialog(it)
                                }
                            }
                        }
                    })
                }
            }
        })

    }

    fun setAsZero() {
        btn_submit.isEnabled = false
        adapter.select_rule_id(0)
        txt_rule_price.text = "0.00"
        txt_round_off_price.text = "0.00"
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onRestart() {
        super.onRestart()
        txt_expiry_date.text = "00/00/0000"
        CurentDate="0000-00-00"
        txt_qty.text = "0"
    }

    private fun getdecimalPointCount(): String{
        return if(isKgItem){
            "%.3f"
        }else{
            "%.2f"
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, ScanBarcodeActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun showDatePicker(view: View) {
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("SELECT EXPIRY DATE")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder.build())
            .build()


        datePicker.addOnPositiveButtonClickListener {
            val dateValue = outputdateFormat.format(it)
            val dateValue2 = outputdateFormat2.format(it)
            txt_expiry_date.text = dateValue2
            CurentDate=dateValue
            searchRuleByMCID(MCID, true)

        }
        datePicker.show(supportFragmentManager, "Date Selector")
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

    private fun getAllRules() {
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

                                            progressDialog.dialog.dismiss()
                                            showdialog=true
                                            searchRuleByMCID(MCID, true)

                                        }
                                    } else {
                                        showToast(
                                            this@ItemDetailsActivity,
                                            "Rule not found ! ",
                                            2
                                        )
                                        progressDialog.dialog.dismiss()
                                    }

                                } else {
                                    showToast(
                                        this@ItemDetailsActivity,
                                        "" + response.body()?.StatusReturn!!.Message.toString(),
                                        2
                                    )
                                    progressDialog.dialog.dismiss()

                                }
                            } else {
                                showToast(this@ItemDetailsActivity, "Operation failed ! ", 2)
                                progressDialog.dialog.dismiss()

                            }
                        } else {
                            showToast(this@ItemDetailsActivity, "Operation failed ! ", 2)
                            progressDialog.dialog.dismiss()

                        }
                    }

                    override fun onFailure(call: Call<GetAllRules>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(this@ItemDetailsActivity, "Operation failed ! ", 2)

                    }
                })
        }
    }

    fun searchRuleByMCID(searchtext: String, fromqty: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            ruleViewModel.searchByMCID(searchtext).observe(this@ItemDetailsActivity, { list ->
                list.let {
                    if (it.isNotEmpty()) {
                        adapter.setData(it,0)
                        calculateDiscountRate( getdatedifference( getCurrentDate(),  CurentDate.trim()), it,fromqty)
                    } else {
                        var rulelist= emptyList<Rule_details>()
                        adapter.setData(rulelist,0)
                    }

                    showdialog=false

                }
            })
        }
    }

    private fun calculateDiscountRate(dateDiff: Long, rule_details: List<Rule_details>,fromqty: Boolean) {
        btn_submit.isEnabled = false
        adapter.select_rule_id(0)
        setAsZero()
        try{
            if (txt_qty.length() > 0){
                if (txt_qty.text.toString().trim().isNullOrBlank()) {
                    txt_qty.text = ""
                    setAsZero()
                    if(fromqty) {
                        showToast(this, "Invalid Quantity ! ", 2)
                    }
                    return
                } else {
                    try {
                        if (txt_qty.text.toString().trim().toDouble() > 0) {
                        } else {
                            setAsZero()
                            if(fromqty) {
                                showToast(this, "Invalid Quantity ! ", 2)
                            }
                            return
                        }

                    } catch (e: Exception) {
                        setAsZero()
                        if(fromqty) {
                            showToast(this, "Invalid Quantity ! ", 2)
                        }
                        return
                    }
                }
            }else{
                if(fromqty) {
                    showToast(this, "Invalid Quantity ! ", 2)
                }
                return
            }
        }catch (E:Exception){
            if(fromqty) {
                showToast(this, "Invalid Quantity ! ", 2)
            }
            return
        }

        if (rule_details.isNullOrEmpty()) {
            btn_submit.isEnabled = false
            adapter.select_rule_id(0)
            setAsZero()
        } else {
            var count = rule_details.size
            for (obj in rule_details) {
                if (obj.RemainingDays > dateDiff) {
                    RuleId = obj.RuleID
                    Discount = obj.Discount
                    RemainingDays = obj.RemainingDays

                    var itemrate = txt_item_rsp.text.toString().trim().toDouble();
                    var discperc = obj.Discount;
                    var value: Double = discperc / 100.toDouble();
                    var fvalue = value * itemrate
                    txt_rule_price.text = String.format("%.2f", itemrate - fvalue)
                    calculateRoundOff()
                    adapter.setData(rule_details,obj.RuleID)
                    btn_submit.isEnabled = true

                    break;
                }
                count--
            }
            if(count!=0){
                btn_submit.isEnabled = true
            }else{
                if(fromqty) {
                    showToast(this@ItemDetailsActivity, "Rule not found ! ", 2)
                }
                btn_submit.isEnabled = false
            }


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

    private fun showScanNextDialog(details: List<Carted_item_details>) {
        var dialog = AlertDialog.Builder(this).create()
        val factory = LayoutInflater.from(this)
        val dialogView: View =
            factory.inflate(R.layout.layout_confirm_dialog_for_next_item_scan, null)
        dialog.setView(dialogView)
        dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
            .setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                showEndJobtDialog(details)
            })
        dialogView.findViewById<MaterialButton>(R.id.btn_ok)
            .setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                val intent = Intent(this@ItemDetailsActivity, ScanBarcodeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            })
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 50)
        dialog.window?.setBackgroundDrawable(inset)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showEndJobtDialog(carted_item_details: List<Carted_item_details>) {
        var dialog = AlertDialog.Builder(this@ItemDetailsActivity).create()
        val factory = LayoutInflater.from(this@ItemDetailsActivity)
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
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun InsertJobs(status: Int, carted_item_details: List<Carted_item_details>) {

        progressDialog.show(this@ItemDetailsActivity, "Please Wait...")

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
            String.format("%.3f",Checksum).toDouble(),
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
                                                this@ItemDetailsActivity,
                                                "Job ended successfully",
                                                1
                                            )
                                        } else {
                                            showToast(
                                                this@ItemDetailsActivity,
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
                                            this@ItemDetailsActivity,
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

    internal class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) : InputFilter {
        private val mPattern: Pattern
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val matcher = mPattern.matcher(dest)
            return if (!matcher.matches()) "" else null
        }

        init {
            mPattern = if(digitsAfterZero>0) {
                Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")
            }else{
                Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}")
            }
        }
    }

    private fun calculateRoundOff() {
        var rulerate = txt_rule_price.text.toString().toDouble()
        Log.d("calculateRoundOff"," calculateRoundOff()  => rulerate  = $rulerate")
        Log.d("calculateRoundOff"," calculateRoundOff()  => roundoff_value  = $roundoff_value")
        when (mode_off_roundoff) {
            "Auto" -> {
                when(roundoff_value){
                    25 ->{
                        var mode = rulerate % .25.toDouble()
                        val bal=.25-mode
                        if(bal==.25){
                            txt_round_off_price.text = txt_rule_price.text.toString()
                        }else if(bal<=.12){
                            txt_round_off_price.text= String.format("%.2f", rulerate-mode)
                        }else{
                            val bal=.25-mode
                            txt_round_off_price.text= String.format("%.2f", rulerate+bal)
                        }
                    }
                    50 ->{

                        var mode = rulerate % .50.toDouble()
                        val bal=.50-mode
                        if(bal==.50){
                            txt_round_off_price.text = txt_rule_price.text.toString()
                        }else if(bal<=.25){
                            txt_round_off_price.text= String.format("%.2f", rulerate-mode)
                        }else{
                            val bal=.50-mode
                            txt_round_off_price.text= String.format("%.2f", rulerate+bal)
                        }

                    }
                    else ->{
                        txt_round_off_price.text = txt_rule_price.text.toString()
                    }
                }
            }
            "Up" -> {

                when(roundoff_value){
                    25 ->{
                        var mode = rulerate % .25.toDouble()
                        val bal=.25-mode
                        if(bal==.25){
                            txt_round_off_price.text = txt_rule_price.text.toString()
                        }else{
                            txt_round_off_price.text= String.format("%.2f", rulerate+bal)
                        }
                    }
                    50 ->{

                        var mode = rulerate % .50.toDouble()
                        val bal=.50-mode
                        if(bal==.50){
                            txt_round_off_price.text = txt_rule_price.text.toString()
                        }else{
                            val bal=.50-mode
                            txt_round_off_price.text= String.format("%.2f", rulerate+bal)
                        }
                    }
                    else ->{
                        txt_round_off_price.text = txt_rule_price.text.toString()
                    }
                }
                }
            "Down" -> {

                when(roundoff_value){
                    25 ->{
                        var mode = rulerate % .25.toDouble()

                        if(mode<=0){
                            txt_round_off_price.text = txt_rule_price.text.toString()
                        }else{
                            txt_round_off_price.text= String.format("%.2f", rulerate-mode)
                        }
                    }
                    50 ->{

                        var mode = rulerate % .50.toDouble()
                        if(mode<=0){
                            txt_round_off_price.text = txt_rule_price.text.toString()
                        }else{
                            txt_round_off_price.text= String.format("%.2f", rulerate-mode)
                        }

                    }
                    else ->{
                        txt_round_off_price.text = txt_rule_price.text.toString()
                    }
                }


            }
            "None" -> {
                txt_round_off_price.text = txt_rule_price.text.toString()
            }
        }
    }

}
