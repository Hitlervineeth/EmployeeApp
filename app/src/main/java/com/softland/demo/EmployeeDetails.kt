package com.softland.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.softland.demo.database.DemoAppDB
import com.softland.demo.viewmodel.EmployeeViewModel

class EmployeeDetails : AppCompatActivity() {

    private val db by lazy { DemoAppDB(this@EmployeeDetails) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_details)
        val emp_img: ImageView = findViewById(R.id.emp_img)
        val emp_id: TextView = findViewById(R.id.emp_id)
        val emp_name: TextView = findViewById(R.id.emp_name)
        val emp_email: TextView = findViewById(R.id.emp_email)
        val emp_phone: TextView = findViewById(R.id.emp_phone)
        val emp_username: TextView = findViewById(R.id.emp_username)
        val emp_website: TextView = findViewById(R.id.emp_website)

        val employeeViewModel : EmployeeViewModel = EmployeeViewModel(db.employeeDetailsDao())

        intent.getStringExtra("EmployeeId")?.let {
            employeeViewModel.getByEmpID(it).observe(this, { list ->
                list.let {
                    emp_id.text = it[0].id.toString()
                    emp_name.text = it[0].name.toString()
                    emp_email.text = it[0].email.toString()
                    emp_phone.text =it[0].phone.toString()
                    emp_username.text = it[0].username.toString()
                    emp_website.text = it[0].website.toString()
                    val media  = it[0].profile_image.toString()
                    if (media !== null) {
                            var requestOptions = RequestOptions()
                            requestOptions = requestOptions.transforms(FitCenter(), RoundedCorners(16))
                            Glide.with(this@EmployeeDetails)
                                .load(media)
                                .apply(requestOptions)
                                .skipMemoryCache(true)//for caching the image url in case phone is offline
                                .into(emp_img)

                        }

                }
            })
        }

    }
}