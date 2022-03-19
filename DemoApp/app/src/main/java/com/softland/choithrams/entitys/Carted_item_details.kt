package com.softland.choithrams.entitys

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Carted_item_details(

    @PrimaryKey(autoGenerate = true)
    val tbl_id: Int,

    val StockID: Int,
    val UnitID: Int,
    val BarCodeID: Int,
    val Quantity: Double,
    val ActualRate: Double,
    val RuleRate: Double,
    val RoundOff: Double,
    val Rate: Double,
    val GrossAmount: Double,
    val NetAmount: Double,
    val RuleID: Int,
    val ExpiryDate: String,
    val RemainingDays: Int,
    val Discount: Double

)
