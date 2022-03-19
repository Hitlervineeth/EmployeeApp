package com.softland.choithrams.retrofitresponses

data class GetSavedJobDetails(
    val ResponseData: List<ResponseDataForGetSavedJobDetails>,
    val StatusReturn: StatusReturn
)