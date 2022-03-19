package com.softland.choithrams.entitys

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Login_details(

    @ColumnInfo(name = "tbl_id")
    @PrimaryKey(autoGenerate = true)
    var tbl_id: Int? = 1,

    @ColumnInfo(name = "UserID") var UserID: Int,
    @ColumnInfo(name = "TokenID") var TokenID: String,
    @ColumnInfo(name = "DisplayName") var DisplayName: String,
    @ColumnInfo(name = "JobNumber") var JobNumber: Int,
    @ColumnInfo(name = "StoreID") var StoreID: Int,
    @ColumnInfo(name = "StoreCode") var StoreCode: String,
    @ColumnInfo(name = "StoreName") var StoreName: String,
    @ColumnInfo(name = "PickerCode") var PickerCode: String,
    @ColumnInfo(name = "FinCode") var FinCode: String,
    @ColumnInfo(name = "EmployeeType") var EmployeeType: String,
    @ColumnInfo(name = "ServerTime") var ServerTime: String,
    @ColumnInfo(name = "RoundOff") var RoundOff: String,

    @ColumnInfo(name = "RoundOffLimit") var RoundOffLimit: Double,
    @ColumnInfo(name = "CurrencyID") var CurrencyID: Int,
    @ColumnInfo(name = "Currency") var Currency: String,
    @ColumnInfo(name = "TimeZone") var TimeZone: String,

    )
