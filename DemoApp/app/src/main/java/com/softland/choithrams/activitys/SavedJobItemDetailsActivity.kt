package com.softland.choithrams.activitys

import SavedJobItemListAdapter
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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.isEmpty
import androidx.core.util.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.entitys.Saved_job_item_details
import com.softland.choithrams.interfaces.OnItemClickListners
import com.softland.choithrams.networks.ChoithramsAPI
import com.softland.choithrams.requestobjects.*
import com.softland.choithrams.retrofitresponses.InsertJobDetails
import com.softland.choithrams.viewmodel.JobViewModel
import com.softland.choithrams.viewmodel.SavedJobItemsViewModel
import com.softland.choithrams.views.CustomProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class SavedJobItemDetailsActivity : AppCompatActivity(), OnItemClickListners {
    lateinit var item_search_input: EditText
    var Job_id: String = ""
    lateinit var btn_reject: MaterialButton
    lateinit var btn_approved: MaterialButton
    lateinit var item_clear_click_parent: RelativeLayout
    lateinit var savedJobItemsViewModel: SavedJobItemsViewModel
    lateinit var jobViewModel: JobViewModel

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var userid = 0
    var storeid = 0
    var Sectionid = 0
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val TAG = "SavedJobItemDetailsActivity"
    private val progressDialog = CustomProgressDialog()
    val PERMISSION_ID = 1010
    val db by lazy { ChoithramDB(this@SavedJobItemDetailsActivity) }
    val adapter: SavedJobItemListAdapter by lazy { SavedJobItemListAdapter(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_job_item_details)
        item_search_input = findViewById(R.id.item_search_input)
        val checkbox_select_all: CheckBox = findViewById(R.id.checkbox_select_all)
        item_clear_click_parent = findViewById(R.id.item_clear_click_parent)
        btn_reject = findViewById(R.id.btn_reject)
        btn_approved = findViewById(R.id.btn_approved)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        RequestPermission()
        getLastLocation()

        savedJobItemsViewModel = SavedJobItemsViewModel(db.savedJobItemDetailsDao())
        jobViewModel = JobViewModel(db.jobDetailsDao())
        val recyclerview = findViewById<RecyclerView>(R.id.recycler_view_live_jobs)
        adapter.setListner(this)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter

        checkbox_select_all.setOnClickListener(View.OnClickListener {
            if (checkbox_select_all.isChecked) {
                adapter.checkCheckBox(true)
            } else {
                adapter.checkCheckBox(false)
            }
        })

        CoroutineScope(Dispatchers.Main).launch {
            userid = db.loginDetailsDao().getAll().get(0).UserID
            storeid = db.loginDetailsDao().getAll().get(0).StoreID
            adapter.setCurrency(db.loginDetailsDao().getAll().get(0).Currency)
            savedJobItemsViewModel.readData.observe(this@SavedJobItemDetailsActivity) {
                adapter.setData(it)
            }
        }

        intent.getStringExtra("JobID")?.let { str ->
            Job_id = str
        }

        item_search_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (item_search_input.length() > 0) {
                    if (item_clear_click_parent.visibility != View.VISIBLE) {
                        item_clear_click_parent.visibility = View.VISIBLE
                    }
                    if (!item_search_input.text.toString().trim().isNullOrBlank()) {
                        savedJobItemsViewModel.searchJob(
                            item_search_input.text.toString().trim()
                        ).observe(this@SavedJobItemDetailsActivity, { list ->
                            list.let {
                                adapter.setData(it)
                            }
                        })
                    }
                } else {
                    item_clear_click_parent.visibility = View.GONE
                    savedJobItemsViewModel.searchJob("")
                        .observe(this@SavedJobItemDetailsActivity, { list ->
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

        btn_reject.setOnClickListener(View.OnClickListener {
            var selectsize=0
            if (adapter.getSelectedIds()?.isEmpty() == true) {
                selectsize=0
            } else {
                selectsize= adapter.getSelectedIds()!!.size()
            }

            savedJobItemsViewModel.readData.observe(this) {
                if(selectsize==0) {
                    ApproveJobDetails(3, it,3)
                }else{
                    if(selectsize==it.size){
                        ApproveJobDetails(3, it,3)
                    }else {
                        showPartialApproveAlertDialog(3,it.size,selectsize,it)
                    }
                }
            }

        })

        btn_approved.setOnClickListener(View.OnClickListener {

            var selectsize=0
            if (adapter.getSelectedIds()?.isEmpty() == true) {
                selectsize=0
            } else {
                selectsize= adapter.getSelectedIds()!!.size()
            }

            savedJobItemsViewModel.readData.observe(this) {
                if(selectsize==0) {
                    ApproveJobDetails(2, it,2)
                }else{
                    if(selectsize==it.size){
                        ApproveJobDetails(2, it,2)
                    }else {
                        showPartialApproveAlertDialog(2,it.size,selectsize,it)
                    }
                }
            }


        })

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onBackPressed() {
        val intent = Intent(this, PendingApprovalActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(
        table_id: String,
        item_name: String,
        barcode_no: String,
        current_qty: String,
        itemrate: String,unitCode:String,unitName: String
    ) {
        showEditQtyDialog(table_id, item_name, barcode_no, current_qty, itemrate,unitCode,unitName)
    }

    private fun showEditQtyDialog(
        id: String,
        iten_name: String,
        barcode_no: String,
        current_qty: String,
        itemrate: String,unitCode:String,unitName: String
    ) {
        var dialog = AlertDialog.Builder(this).create()
        val factory = LayoutInflater.from(this)
        val dialogView: View = factory.inflate(R.layout.layout_dialog_for_update_qty_item, null)
        dialog.setView(dialogView)
        var txt_qty = dialogView.findViewById<TextInputEditText>(R.id.txt_qty)
        var text_item_name = dialogView.findViewById<TextInputEditText>(R.id.text_item_name)
        var text_barcode = dialogView.findViewById<TextInputEditText>(R.id.text_barcode)
        var text_item_rate = dialogView.findViewById<TextInputEditText>(R.id.text_item_rate)

        text_item_rate.setText(itemrate)
        text_barcode.setText(barcode_no)
        text_item_name.setText(iten_name)

        dialogView.findViewById<MaterialButton>(R.id.btn_ok) .setOnClickListener(View.OnClickListener {

                if (txt_qty.length() > 0) {
                    if (txt_qty.text.toString().contentEquals(",")) {
                        showToast(this@SavedJobItemDetailsActivity, "Invalid Quantity ! ", 2)
                        return@OnClickListener;
                    } else {
                        if (txt_qty.text.toString().trim().isNullOrBlank()) {
                            showToast(this@SavedJobItemDetailsActivity, "Invalid Quantity ! ", 2)
                            return@OnClickListener;
                        } else {
                            try {
                                if (txt_qty.text.toString().trim().toDouble() > 0) {
                                    Thread {
                                        savedJobItemsViewModel.updateQty(
                                            String.format(getdecimalPointCount(unitCode,unitName),txt_qty.text.toString().trim().toDouble()),
                                            id
                                        )
                                    }.start()
                                } else {
                                    showToast(
                                        this@SavedJobItemDetailsActivity,
                                        "Invalid Quantity ! ",
                                        2
                                    )
                                    return@OnClickListener;
                                }
                            } catch (e: Exception) {
                                showToast(
                                    this@SavedJobItemDetailsActivity,
                                    "Invalid Quantity ! ",
                                    2
                                )
                                return@OnClickListener;
                            }
                        }
                    }
                } else {
                    showToast(this@SavedJobItemDetailsActivity, "Invalid Quantity ! ", 2)
                    return@OnClickListener;
                }

                dialog.dismiss()


            })

        if(unitCode.toString().trim().equals("KG",true) ||unitName.toString().trim().equals("KG",true)){
            txt_qty.setFilters(arrayOf<InputFilter>(DecimalDigitsInputFilter(7, 3)))
        }else{
            txt_qty.inputType= InputType.TYPE_CLASS_NUMBER
            txt_qty.setFilters(arrayOf<InputFilter>(DecimalDigitsInputFilter(7, 2)))
        }

        txt_qty.setText(current_qty)
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 50)
        dialog.window?.setBackgroundDrawable(inset)
        dialog.setCancelable(true)
        dialog.show()

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

    internal class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
        InputFilter {
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

    fun showPartialApproveAlertDialog(from : Int, totalSize:Int,selectSize :Int, saved_job_item_details: List<Saved_job_item_details>){

        var approveCount=0
        var rejectedCount=0;
        if(from==2){
            approveCount=selectSize;
            rejectedCount=totalSize-selectSize
        }else{
            rejectedCount=selectSize;
            approveCount=totalSize-selectSize
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(this@SavedJobItemDetailsActivity)
        builder.setTitle("Confirm ?")
        builder.setMessage("Out of $totalSize articles $approveCount will be approved and $rejectedCount articles will be rejected !.\nDo you want to proceed ?")
            .setCancelable(true)
            .setPositiveButton("yes") { dialog, id ->
                ApproveJobDetails(4, saved_job_item_details,from)
            }.setNegativeButton("No") { dialog, id ->

            }

        val alert = builder.create()
        alert.show()

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

    fun getCurrentDateAndTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }


    private fun ApproveJobDetails(
        status: Int,
        saved_job_item_details: List<Saved_job_item_details>,
        from:Int
    ) {

        progressDialog.show(this@SavedJobItemDetailsActivity, "Please Wait...")

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
            "ApproveJobDetails",
            "",
            0,
            1,
            0
        )

        var jobDetailForApproveJobDetails_list = ArrayList<JobDetailForApproveJobDetails>()
        var Checksum = 0.00
        var GrossAmount = 0.00
        var NetAmoount = 0.00


        val sharedPref = getSharedPreferences("JobDetails", Context.MODE_PRIVATE)
        val jobDate = sharedPref.getString("jobDate", getCurrentDateAndTime()).toString()
        val PickerId = sharedPref.getInt("PickerId", 0)
        var JobNumber = sharedPref.getString("JobNumber", "0000").toString()
        Sectionid = sharedPref.getInt("SectionID", 0)

        if(status==4) {
            for (obj in saved_job_item_details) {

                if (from == 2) {
                    val sts = adapter.getSelectedIds()?.get(obj.tbl_id)
                    if (sts == true) {
                        jobDetailForApproveJobDetails_list.add(
                            JobDetailForApproveJobDetails(
                                obj.ActualRate,
                                obj.BarCodeID,
                                obj.ExpiryDate.replace("T", " "),
                                obj.GrossAmount,
                                obj.NetAmount,
                                obj.Quantity.toFloat(),
                                obj.Rate,
                                obj.RoundOff,
                                obj.RuleRate,
                                obj.StockID,
                                obj.UnitID
                            )
                        )

                        GrossAmount += obj.GrossAmount
                        NetAmoount += obj.NetAmount
                        Checksum +=   obj.Quantity.toFloat() * obj.StockID
                    }
                } else {
                    val sts = adapter.getSelectedIds()?.get(obj.tbl_id)
                    if (sts == false) {
                        jobDetailForApproveJobDetails_list.add(
                            JobDetailForApproveJobDetails(
                                obj.ActualRate,
                                obj.BarCodeID,
                                obj.ExpiryDate.replace("T", " "),
                                obj.GrossAmount,
                                obj.NetAmount,
                                obj.Quantity.toFloat(),
                                obj.Rate,
                                obj.RoundOff,
                                obj.RuleRate,
                                obj.StockID,
                                obj.UnitID
                            )
                        )

                        GrossAmount += obj.GrossAmount
                        NetAmoount += obj.NetAmount
                        Checksum +=   obj.Quantity.toFloat() * obj.StockID
                    }
                }
            }
        }else if(status==2 ){
            for (obj in saved_job_item_details) {
                        jobDetailForApproveJobDetails_list.add(
                            JobDetailForApproveJobDetails(
                                obj.ActualRate,
                                obj.BarCodeID,
                                obj.ExpiryDate.replace("T", " "),
                                obj.GrossAmount,
                                obj.NetAmount,
                                obj.Quantity.toFloat(),
                                obj.Rate,
                                obj.RoundOff,
                                obj.RuleRate,
                                obj.StockID,
                                obj.UnitID
                            )
                        )

                        GrossAmount += obj.GrossAmount
                        NetAmoount += obj.NetAmount
                        Checksum +=   obj.Quantity.toFloat() * obj.StockID
                    }
        }else{

        }

        val requestDataForApproveJobDetails = RequestDataForApproveJobDetails(
            String.format("%.3f",Checksum).toDouble(),
            GrossAmount,
            jobDate,
            jobDetailForApproveJobDetails_list,
            JobNumber,
            NetAmoount,
            jobDetailForApproveJobDetails_list.size,
            " ",
            Sectionid,
            storeid,
            PickerId,
            userid,
            getCurrentDateAndTime(),
            status
        )

        val approveJobDetailsRequest = ApproveJobDetailsRequest(credentials, requestDataForApproveJobDetails)

        CoroutineScope(Dispatchers.Main).launch {
            ChoithramsAPI.getRetrofit().create(ChoithramsAPI::class.java)
                .approveJobDetails(approveJobDetailsRequest)
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
                                            JobNumber,
                                            true
                                        )
                                    ) {
                                        if (status == 3) {
                                            showToast(
                                                this@SavedJobItemDetailsActivity,
                                                " Successfully rejected",
                                                1
                                            )
                                        } else {
                                            showToast(
                                                this@SavedJobItemDetailsActivity,
                                                "Successfully Approved",
                                                1
                                            )
                                        }

                                        progressDialog.dialog.dismiss()
                                        val intent = Intent(
                                            this@SavedJobItemDetailsActivity,
                                            HomeScreenManagerActivity::class.java
                                        )
                                        startActivity(intent)
                                        finish()
                                        overridePendingTransition(
                                            R.anim.slide_in_left,
                                            R.anim.slide_out_right
                                        )
                                    } else {
                                        showToast(applicationContext, "Operation failed ! ", 2)
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

                    override fun onFailure(call: Call<InsertJobDetails>, t: Throwable) {
                        progressDialog.dialog.dismiss()
                        showToast(applicationContext, "Operation failed ! ", 2)
                    }
                })
        }

    }


    private fun getdecimalPointCount(unitCode:String,unitName: String) : String{
        return  if(unitCode.toString().trim().equals("KG",true) ||unitName.toString().trim().equals("KG",true)){
            "%.3f"
        }else{
            "%.0f"
        }
    }


}