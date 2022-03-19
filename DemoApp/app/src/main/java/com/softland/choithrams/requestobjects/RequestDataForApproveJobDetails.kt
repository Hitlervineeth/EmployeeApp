package com.softland.choithrams.requestobjects

data class RequestDataForApproveJobDetails(
    val CheckSum: Double,
    val GrossAmount: Double,
    val JobDate: String,
    val JobDetails: List<JobDetailForApproveJobDetails>,
    val JobNumber: String,
    val NetAmount: Double,
    val NoOfItems: Int,
    val Remarks: String,
    val SectionID: Int,
    val StoreID: Int,
    val UserID: Int,
    val ApprovedBy :Int,
    val ApprovedOn : String,
    val JobStatus :Int
)