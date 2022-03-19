package com.softland.choithrams.entitys

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stock_details(


    @ColumnInfo(name = "tbl_id")
    @PrimaryKey(autoGenerate = true)
    var tbl_id: Int? = 1,

    val UserID: Int,
    val StockID: Int,
    val StockCode: String,
    val StockName: String,
    val StockDescription: String,
    val UnitID: Int,
    val UnitCode: String,
    val UnitName: String,
    val MCID: Int,
    val MCCode: String,
    val MCName: String,
    val MCDescription: String

    )
