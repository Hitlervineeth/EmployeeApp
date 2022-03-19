package com.softland.choithrams.requestobjects

data class RequestDataForLogOut(
    val LastBillNumbers: List<LastBillNumber>,
    val UserID: Int
)