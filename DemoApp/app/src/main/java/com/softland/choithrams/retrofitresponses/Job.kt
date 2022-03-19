package com.softland.choithrams.retrofitresponses

data class Job(
    val UserID: Int,
    val JobNumber: String,
    val SectionID:Int,
    val JobDate: String,
    val JobEndDate: String,
    val JobStartDate: String,
    val JobStatus: Int,
    val NoOfItems: Int,
    val Remarks: String,
    val StoreID: Int,
    val NetAmount: Double,
    val RoundOff: Double,
    val GrossAmount: Double,
    val TotalDiscount: Double,
    val SectionName:String,
    val  SectionCode: String,
    val Picker:String
)