package com.softland.choithrams.retrofitresponses

data class ResponseDataForGetAllSections(
    val NoOfItems: Int,
    val Section: List<Section>,
    val UserID: Int
)