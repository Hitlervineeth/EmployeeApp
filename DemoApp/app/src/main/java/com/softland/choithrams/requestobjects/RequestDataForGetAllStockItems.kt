package com.softland.choithrams.requestobjects

data class RequestDataForGetAllStockItems(
    val SectionID: Int,
    val StoreID: Int,
    val UserID: Int,
    val JobNumber:String

)