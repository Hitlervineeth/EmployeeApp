package com.softland.choithrams.retrofitresponses

import com.softland.choithrams.entitys.Sales_rate_details

data class ResponseDataForGetAllSalesRates(
    val NoOfItems: Int,
    val SalesRates: List<Sales_rate_details>,
    val UserID: Int
)