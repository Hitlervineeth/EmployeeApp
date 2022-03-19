import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.softland.choithrams.R
import com.softland.choithrams.entitys.Rule_details
import com.softland.choithrams.entitys.Section_details
import com.softland.choithrams.interfaces.OnClickListners
import com.softland.choithrams.objects.SavedJob
import com.softland.choithrams.retrofitresponses.Rule

class RulesListAdapter(context: Context) : RecyclerView.Adapter<RulesListAdapter.ViewHolder>() {
    var context: Context? = null
    var clickListener: OnClickListners? = null
    private var mList=emptyList<Rule_details>()
    private  var  select_rule_id=0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.context=context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_recycler_view_rule_object, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rule = mList[position]
        holder.RuleName.text = rule.RuleName
        holder.RemDays.text = rule.RemainingDays.toString()
        holder.Disc.text = rule.Discount.toString()+" %"

        if(select_rule_id==rule.RuleID){

            holder.cardview_main_layout.setBackgroundResource( R.color.select_color)
        }else{
            holder.cardview_main_layout.setBackgroundResource(R.color.white)
        }

    }

    override fun getItemCount(): Int {
        return mList.size
    }


    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val RuleName: TextView = itemView.findViewById(R.id.txt_ruleName)
        val RemDays : TextView = itemView.findViewById(R.id.txt_remDays)
        var Disc: TextView =itemView.findViewById(R.id.txt_disc_perc)
        var cardview_main_layout:CardView=itemView.findViewById(R.id.cardview_main_layout)
    }

    fun setData(newData: List<Rule_details>,select_rule_id:Int){
        mList = newData
        this.select_rule_id=select_rule_id
        notifyDataSetChanged()
    }

    fun select_rule_id(select_rule_id:Int){
        this.select_rule_id=select_rule_id
        notifyDataSetChanged()
    }


}
