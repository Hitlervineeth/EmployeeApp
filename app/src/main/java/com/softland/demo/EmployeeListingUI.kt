package com.softland.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.softland.choithrams.interfaces.OnClickListners
import com.softland.demo.adapter.EmployeeListAdapter
import com.softland.demo.database.DemoAppDB
import com.softland.demo.viewmodel.EmployeeViewModel

class EmployeeListingUI : AppCompatActivity() , OnClickListners {

    private val db by lazy { DemoAppDB(this@EmployeeListingUI) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_listing_ui)
        val item_search_input: EditText = findViewById(R.id.item_search_input)
        val item_clear_click_parent: RelativeLayout = findViewById(R.id.item_clear_click_parent)
        val recyclerview = findViewById<RecyclerView>(R.id.recycler_view_saved_job)
        val adapter: EmployeeListAdapter by lazy { EmployeeListAdapter(this) }
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter
        adapter.clickListener(this)

        val employeeViewModel: EmployeeViewModel = EmployeeViewModel(db.employeeDetailsDao())

        item_search_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (item_search_input.length() > 0) {
                    if (item_clear_click_parent.visibility != View.VISIBLE) {
                        item_clear_click_parent.visibility = View.VISIBLE
                    }

                    if (!item_search_input.text.toString().trim().isNullOrBlank()) {
                        employeeViewModel.getAllByNameOrEmail(
                            item_search_input.text.toString().trim()
                        ).observe(this@EmployeeListingUI, { list ->
                            list.let {
                                adapter.setData(it)
                            }
                        })
                    }

                } else {
                    item_clear_click_parent.visibility = View.GONE
                    employeeViewModel.getAllByNameOrEmail(
                       ""
                    ).observe(this@EmployeeListingUI, { list ->
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

        employeeViewModel.readData.observe(this) {
            adapter.setData(it)
        }

    }

    override fun onClick(EmployeeId: Int, EmployeeName: String) {
        val intent = Intent(this, EmployeeDetails::class.java)
        intent.putExtra("EmployeeName", EmployeeName)
        intent.putExtra("EmployeeId", EmployeeId.toString())
        startActivity(intent)
    }


}