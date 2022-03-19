package com.softland.choithrams.interfaces


interface OnItemClickListners {
    fun onClick(table_id: String,item_name :String,barcode_no : String,current_qty : String,itemrate :String,unitCode:String,unitName: String)
}