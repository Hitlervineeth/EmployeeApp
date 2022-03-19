package com.softland.choithrams.requestobjects

data class JobDetailForApproveJobDetails(
    val ActualRate: Double,
    val BarCodeID: Int,
    val ExpiryDate: String,
    val GrossAmount: Double,
    val NetAmount: Double,
    val Quantity: Float,
    val Rate: Double,
    val RoundOff: Double,
    val RuleRate: Double,
    val StockID: Int,
    val UnitID: Int
)