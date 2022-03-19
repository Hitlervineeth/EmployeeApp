package com.softland.choithrams.activitys

import LiveJobsListAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.softland.choithrams.R
import com.softland.choithrams.database.ChoithramDB
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.viewmodel.JobViewModel
import com.softland.choithrams.viewmodel.SectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LiveJobsScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_jobs_screen)

        val item_search_input: EditText = findViewById(R.id.item_search_input)
        val item_clear_click_parent: RelativeLayout = findViewById(R.id.item_clear_click_parent)
        val db by lazy { ChoithramDB(this@LiveJobsScreenActivity) }
        val TAG = "LiveJobsScreenActivity"
        val adapter: LiveJobsListAdapter by lazy { LiveJobsListAdapter(this@LiveJobsScreenActivity) }
        val jobViewModel: JobViewModel = JobViewModel(db.jobDetailsDao())
        val recyclerview = findViewById<RecyclerView>(R.id.recycler_view_live_jobs)
        recyclerview.layoutManager = LinearLayoutManager(this@LiveJobsScreenActivity)
        recyclerview.adapter = adapter


        jobViewModel.readData.observe(this) {
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
                        jobViewModel.searchJob(item_search_input.text.toString().trim()).observe(this@LiveJobsScreenActivity, { list ->
                            list.let {
                                adapter.setData(it)
                            }
                        })
                    }
                } else {
                    item_clear_click_parent.visibility = View.GONE
                    jobViewModel.searchJob("").observe(this@LiveJobsScreenActivity, { list ->
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
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
    }


}