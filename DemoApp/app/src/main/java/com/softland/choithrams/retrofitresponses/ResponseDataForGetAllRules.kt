package com.softland.choithrams.retrofitresponses

data class ResponseDataForGetAllRules(
    val NoOfItems: Int,
    val Rules: List<Rule>,
    val UserID: Int
)