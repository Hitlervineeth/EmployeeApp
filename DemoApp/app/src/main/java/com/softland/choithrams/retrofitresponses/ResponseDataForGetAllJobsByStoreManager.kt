package com.softland.choithrams.retrofitresponses

import com.softland.choithrams.entitys.Job_details

data class ResponseDataForGetAllJobsByStoreManager(
    val JobByStoreManager: List<Job_details>,
    val NoOfItems: Int,
    val UserID: Int
)