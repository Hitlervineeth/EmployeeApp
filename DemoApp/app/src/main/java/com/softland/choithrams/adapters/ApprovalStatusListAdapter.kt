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
import com.softland.choithrams.interfaces.OnClickListners


class ApprovalStatusListAdapter(context: Context) : RecyclerView.Adapter<ApprovalStatusListAdapter.ViewHolder>() {
    var context: Context? = null
    var clickListener: OnClickListners? = null
    private var mList=emptyList<Job_details>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.context=context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_recycler_view_approved_status_job_object, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jobdetails = mList[position]
        holder.Picker.text = jobdetails.Picker
        holder.JobNumber.text = jobdetails.JobNumber
        holder.Section.text = jobdetails.SectionName

        if(jobdetails.JobStatus==1){
            holder.Status.text="APPROVED"
            holder.Status.setTextColor(Color.parseColor("#7CB342"));
        }else if(jobdetails.JobStatus==0){
            holder.Status.text="REJECTED"
            holder.Status.setTextColor(Color.parseColor("#E53935"));
        }else{
            holder.Status.text="APPROVED [PARTIAL]"
            holder.Status.setTextColor(Color.parseColor("#7CB342"));
        }

    }

    override fun getItemCount(): Int {
        return mList.size
    }



    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val Picker: TextView = itemView.findViewById(R.id.txt_picker_name)
        val JobNumber: TextView = itemView.findViewById(R.id.txt_job_no_)
        val Section: TextView = itemView.findViewById(R.id.txt_section_)
        val Status: TextView = itemView.findViewById(R.id.txt_status)
    }

    fun setData(newData: List<Job_details>){
        mList = newData
        notifyDataSetChanged()
    }

}
