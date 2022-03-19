package com.softland.choithrams.retrofitresponses

import com.softland.choithrams.entitys.Job_details

data class ResponseDataForGetAllSavedJobs(
    val Job: List<Job_details>,
    val NoOfItems: Int,
    val UserID: Int
)