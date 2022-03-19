package com.softland.choithrams.entitys

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Saved_job_item_details(


    @ColumnInfo(name = "tbl_id")
    @PrimaryKey(autoGenerate = true)
    var tbl_id: Int = 1,

    val StockID: Int,
    val StockCode: String,
    val StockName: String,
    val BarCodeID: Int,
    val BarCode: String,
    val UnitID: Int,
    val UnitCode: String,
    val UnitName: String,
    val Quantity: String,
    val ExpiryDate: String,
    val ActualRate: Double,
    val RuleRate: Double,
    val RoundOff: Double,
    val Rate: Double,
    val GrossAmount: Double,
    val NetAmount: Double,
    val ApprovedStatus: Int

)
