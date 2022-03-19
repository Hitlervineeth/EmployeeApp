import android.content.Context
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.softland.choithrams.R
import com.softland.choithrams.entitys.Saved_job_item_details
import com.softland.choithrams.interfaces.OnClickListners
import com.softland.choithrams.interfaces.OnItemClickListners


class CartedItemListAdapter(context: Context) : RecyclerView.Adapter<CartedItemListAdapter.ViewHolder>() {

    var context: Context? = null
    private var Currency = ""
    var clickListener: OnItemClickListners? = null
    private var mSelectedItemsIds: SparseBooleanArray = SparseBooleanArray()
    private var mList = emptyList<Saved_job_item_details>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.context = context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_recycler_view_carted_item_object, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemdetails = mList[position]
       // holder.checkbox_select.isChecked = mSelectedItemsIds.get(itemdetails.tbl_id)
        holder.item_name.text=itemdetails.StockName
        holder.item_price.text= String.format("%.2f",itemdetails.ActualRate)+" "+Currency
        holder.rule_price.text= String.format("%.2f",itemdetails.RuleRate)+" "+Currency
        holder.round_off.text= String.format("%.2f",itemdetails.RuleRate+itemdetails.RoundOff)+" "+Currency
        holder.txt_tbl_id.text=itemdetails.tbl_id.toString()
        var qty=String.format(getdecimalPointCount(itemdetails.UnitCode,itemdetails.UnitName),itemdetails.Quantity.toFloat())
        holder.txt_qty.text="QTY :  $qty ${itemdetails.UnitCode}"
        holder.expiry_date.text=itemdetails.ExpiryDate.split(" ")[0].toString()
        holder.setIsRecyclable(false)
//        holder.checkbox_select.setOnClickListener(View.OnClickListener {
//            if(holder.checkbox_select.isChecked){
//                checkCheckBox(itemdetails.tbl_id,true)
//            }else{
//                checkCheckBox(itemdetails.tbl_id,false)
//            }
//        })
//
//        holder.btn_edit.setOnClickListener(View.OnClickListener {
//            clickListener?.onClick(itemdetails.tbl_id.toString(),itemdetails.StockName,itemdetails.BarCode,String.format(getdecimalPointCount(itemdetails.UnitCode,itemdetails.UnitName),itemdetails.Quantity.toFloat()),String.format("%.2f",itemdetails.RuleRate+itemdetails.RoundOff),itemdetails.UnitCode,itemdetails.UnitName);
//        })

        Log.e("abcd", " onBindViewHolder $position")

    }

    fun setListner(clickListener: OnItemClickListners){
        this.clickListener=clickListener
    }

    fun setCurrency(Currency: String){
        this.Currency=Currency
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

       // val checkbox_select: CheckBox = itemView.findViewById(R.id.checkbox_select)
        val item_name: TextView = itemView.findViewById(R.id.txt_item_name)
        val expiry_date: TextView = itemView.findViewById(R.id.txt_expiry_date)
        val item_price: TextView = itemView.findViewById(R.id.txt_item_price)
        val rule_price: TextView = itemView.findViewById(R.id.txt_rule_price)
        val round_off: TextView = itemView.findViewById(R.id.txt_round_off_rate)
      //  val btn_edit: ImageView = itemView.findViewById(R.id.btn_edit)
        val txt_tbl_id: TextView = itemView.findViewById(R.id.txt_tbl_id)
        val txt_qty: TextView = itemView.findViewById(R.id.txt_qty)

    }

    fun setData(newData: List<Saved_job_item_details>) {
        mList = newData
        notifyDataSetChanged()
    }

    private fun getdecimalPointCount(unitCode:String,unitName: String) : String{
        return  if(unitCode.toString().trim().equals("KG",true) ||unitName.toString().trim().equals("KG",true)){
            "%.3f"
        }else{
            "%.0f"
        }
    }


    fun removeSelection() {
        mSelectedItemsIds = SparseBooleanArray()
        notifyDataSetChanged()
    }

    fun checkCheckBox(id : Int, value: Boolean) {
        if (value) mSelectedItemsIds.put(id, true) else mSelectedItemsIds.delete(id)
        notifyDataSetChanged()
    }

    fun checkCheckBox(value: Boolean) {
        for (obj in mList) {
            if (value) mSelectedItemsIds.put(obj.tbl_id, true) else mSelectedItemsIds.delete(obj.tbl_id)
        }
        notifyDataSetChanged()
    }

    fun getSelectedIds(): SparseBooleanArray? {
        return mSelectedItemsIds
    }

}
