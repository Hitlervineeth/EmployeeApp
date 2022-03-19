package com.softland.choithrams.entitys

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sales_rate_details(


    @ColumnInfo(name = "tbl_id")
    @PrimaryKey(autoGenerate = true)
    var tbl_id: Int? = 1,

    val BarCode: String,
    val BarCodeID: Int,
    val Rate: Double,
    val StockID: Int

)
