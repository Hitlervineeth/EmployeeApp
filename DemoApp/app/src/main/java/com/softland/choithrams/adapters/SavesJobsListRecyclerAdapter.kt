package com.softland.choithrams.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.softland.choithrams.R
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.interfaces.OnClickListners


class SavesJobsListRecyclerAdapter(context: Context) : RecyclerView.Adapter<SavesJobsListRecyclerAdapter.ViewHolder>() {
    var context: Context? = null
    var clickListener: OnClickListners? = null
    private var mList=emptyList<Job_details>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.context=context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_recycler_view_saved_job_object, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val savedJob = mList[position]
        holder.SectionName.text = savedJob.SectionName
        holder.SectionCode.text = savedJob.SectionCode
        holder.JobNumber.text = savedJob.JobNumber
        holder.Layout.setOnClickListener(View.OnClickListener {
            clickListener?.onClick(savedJob.SectionCode,savedJob.SectionName,savedJob.JobNumber,savedJob.JobStartDate,savedJob.SectionID);
        })


    }

    override fun getItemCount(): Int {
        return mList.size
    }


    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val SectionCode: TextView = itemView.findViewById(R.id.txt_section_code)
        val SectionName: TextView = itemView.findViewById(R.id.txt_section)
        val JobNumber: TextView = itemView.findViewById(R.id.txt_job_no)
        var Layout:CardView=itemView.findViewById(R.id.cardview_main_layout)
    }

    fun setData(newData: List<Job_details>){
        mList = newData
        notifyDataSetChanged()
    }

    fun clickListener(clickListener: OnClickListners) {
        this.clickListener=clickListener

    }

}
