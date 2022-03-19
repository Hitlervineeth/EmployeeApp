package com.softland.choithrams.retrofitresponses

data class StockItem(
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