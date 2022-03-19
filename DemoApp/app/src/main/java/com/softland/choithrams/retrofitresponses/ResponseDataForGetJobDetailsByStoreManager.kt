package com.softland.choithrams.retrofitresponses

data class ResponseDataForGetJobDetailsByStoreManager(
    val ActualRate: Double,
    val BarCode: String,
    val BarCodeID: Int,
    val Discount: Double,
    val ExpiryDate: String,
    val GrossAmount: Double,
    val NetAmount: Double,
    val Quantity: Double,
    val Rate: Double,
    val RemainingDays: Int,
    val RoundOff: Double,
    val RuleID: Int,
    val RuleRate: Double,
    val StockCode: String,
    val StockID: Int,
    val StockName: String,
    val UnitCode: String,
    val UnitID: Int,
    val UnitName: String
)