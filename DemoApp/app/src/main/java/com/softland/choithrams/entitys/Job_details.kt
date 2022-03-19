package com.softland.choithrams.entitys

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Job_details(


    @ColumnInfo(name = "tbl_id")
    @PrimaryKey(autoGenerate = true)
    var tbl_id: Int,

    val UserID: Int,
    val JobNumber: String,
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
    val SectionID: Int,
    val SectionName:String,
    val  SectionCode: String,
    val Picker:String


    )
