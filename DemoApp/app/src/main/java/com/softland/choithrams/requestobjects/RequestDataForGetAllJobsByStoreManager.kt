package com.softland.choithrams.requestobjects

data class RequestDataForGetAllJobsByStoreManager(
    val FromDate: String,
    val SearchTypeID: Int,
    val ToDate: String,
    val UserID: Int
)

