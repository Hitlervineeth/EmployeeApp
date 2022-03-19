package com.softland.choithrams.retrofitresponses

data class ResponseDataForGetSavedJobDetails(

    val StockID: Int,
    val StockCode: String,
    val StockName: String,
    val BarCodeID: Int,
    val BarCode: String,
    val UnitID: Int,
    val UnitCode: String,
    val UnitName: String,
    val Quantity: Double,
    val ExpiryDate: String,
    val ActualRate: Double,
    val RuleRate: Double,
    val RoundOff: Double,
    val Rate: Double,
    val GrossAmount: Double,
    val NetAmount: Double,
    val RuleID: Int,
    val RemainingDays: Int,
    val Discount: Double

)