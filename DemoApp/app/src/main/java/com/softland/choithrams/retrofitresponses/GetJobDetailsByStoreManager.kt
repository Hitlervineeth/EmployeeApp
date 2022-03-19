package com.softland.choithrams.retrofitresponses

import com.softland.choithrams.entitys.Saved_job_item_details

data class GetJobDetailsByStoreManager(
    val ResponseData: List<Saved_job_item_details>,
    val StatusReturn: StatusReturn
)
// val ResponseData: List<ResponseDataForGetJobDetailsByStoreManager>,
