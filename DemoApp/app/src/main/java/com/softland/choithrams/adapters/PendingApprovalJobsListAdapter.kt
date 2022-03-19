import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.softland.choithrams.R
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.entitys.Section_details
import com.softland.choithrams.interfaces.JobClickListners
import com.softland.choithrams.interfaces.OnClickListners


class PendingApprovalJobsListAdapter(context: Context) : RecyclerView.Adapter<PendingApprovalJobsListAdapter.ViewHolder>() {
    var context: Context? = null
    var clickListener: JobClickListners? = null
    private var mList=emptyList<Job_details>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.context=context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_recycler_view_pending_approval_job_object, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jobdetails = mList[position]
        holder.Picker.text = jobdetails.Picker
        holder.JobNumber.text = jobdetails.JobNumber
        holder.Section.text = jobdetails.SectionName
        holder.layout.setOnClickListener(View.OnClickListener {
            clickListener?.onClick(jobdetails.JobNumber,jobdetails.tbl_id.toString(),jobdetails.UserID,jobdetails.JobDate,jobdetails.SectionID)
        })
    }

    fun setListner(jobClickListners: JobClickListners){
        this.clickListener=jobClickListners
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val Picker: TextView = itemView.findViewById(R.id.txt_picker)
        val JobNumber: TextView = itemView.findViewById(R.id.txt_job_no)
        val Section: TextView = itemView.findViewById(R.id.txt_section)
        val layout : CardView =itemView.findViewById(R.id.cardview_main_layout)

    }

    fun setData(newData: List<Job_details>){
        mList = newData
        notifyDataSetChanged()
    }

}
