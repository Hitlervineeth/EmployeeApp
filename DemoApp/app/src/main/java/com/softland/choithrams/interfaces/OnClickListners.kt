package com.softland.choithrams.interfaces


interface OnClickListners {
    fun onClick(sectioncode: String, sectionName:String, jobNumber:String,JobStartDate:String,SectionID :Int)
    fun onClick(sectioncode: String,sectionName:String)
}