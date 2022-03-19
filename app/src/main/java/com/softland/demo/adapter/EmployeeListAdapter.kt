package com.softland.demo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.softland.choithrams.interfaces.OnClickListners
import com.softland.demo.R
import com.softland.demo.entitys.Employee_details


class EmployeeListAdapter(context: Context) : RecyclerView.Adapter<EmployeeListAdapter.ViewHolder>() {
    var context: Context? = context
    var clickListener: OnClickListners? = null
    private var mList=emptyList<Employee_details>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.employee_obj_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val emp = mList[position]
        holder.emp_name.text = emp.name
        holder.emp_email.text = emp.email

        Log.e("abcd", "  emp.profile_image == "+emp.profile_image)
        val media = emp.profile_image
        if (media !== null) {
            context?.let {
                var requestOptions = RequestOptions()
                requestOptions = requestOptions.transforms(FitCenter(), RoundedCorners(16))
                Glide.with(context!!)
                    .load(media)
                    .apply(requestOptions)
                    .skipMemoryCache(true)//for caching the image url in case phone is offline
                    .into(holder.emp_img)

            }
        }


        holder.cardview_main_layout.setOnClickListener(View.OnClickListener {
            clickListener?.onClick(emp.id,emp.name);
        })


    }

    override fun getItemCount(): Int {
        return mList.size
    }


    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val emp_name: TextView = itemView.findViewById(R.id.emp_name)
        val emp_email: TextView = itemView.findViewById(R.id.emp_email)
        val emp_img:ImageView=itemView.findViewById(R.id.emp_img)
        val cardview_main_layout:CardView=itemView.findViewById(R.id.cardview_main_layout)
    }


    fun clickListener(clickListener: OnClickListners) {
        this.clickListener=clickListener

    }

    fun setData(newData: List<Employee_details>){
        mList = newData
        notifyDataSetChanged()
    }


}
