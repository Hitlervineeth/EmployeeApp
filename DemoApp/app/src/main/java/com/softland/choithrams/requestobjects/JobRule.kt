package com.softland.choithrams.requestobjects

data class JobRule(
    val BarCodeID: Int,
    val Discount: Double,
    val ExpiryDate: String,
    val Quantity: Double,
    val RemainingDays: Int,
    val RuleID: Int,
    val StockID: Int,
    val UnitID: Int
)