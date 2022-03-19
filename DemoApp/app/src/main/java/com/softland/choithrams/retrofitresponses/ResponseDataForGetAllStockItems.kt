package com.softland.choithrams.retrofitresponses

import com.softland.choithrams.entitys.Stock_details

data class ResponseDataForGetAllStockItems(
    val NoOfItems: Int,
    val StockItem: List<Stock_details>,
    val UserID: Int
)

//  val StockItem: List<StockItem>,