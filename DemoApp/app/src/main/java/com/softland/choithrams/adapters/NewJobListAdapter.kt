import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.softland.choithrams.R
import com.softland.choithrams.entitys.Section_details
import com.softland.choithrams.interfaces.OnClickListners


class NewJobListAdapter(context: Context) : RecyclerView.Adapter<NewJobListAdapter.ViewHolder>() {
    var context: Context? = null
    var clickListener: OnClickListners? = null
    private var mList=emptyList<Section_details>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.context=context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_recycler_view_new_job_object, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sectionDetails = mList[position]
        holder.SectionName.text = sectionDetails.SectionName
        holder.SectionCode.text = sectionDetails.SectionCode
        holder.Layout.setOnClickListener(View.OnClickListener {
            clickListener?.onClick(sectionDetails.SectionCode,sectionDetails.SectionName);
        })
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setListner(clickListener: OnClickListners){
        this.clickListener=clickListener
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val SectionCode: TextView = itemView.findViewById(R.id.txt_section_code)
        val SectionName: TextView = itemView.findViewById(R.id.txt_section)
        var Layout: CardView =itemView.findViewById(R.id.cardview_main_layout)
    }

    fun setData(newData: List<Section_details>){
        mList = newData
        notifyDataSetChanged()
    }

}
