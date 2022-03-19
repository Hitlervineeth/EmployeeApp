package com.softland.choithrams.requestobjects

data class InsertJobDetailsRequest(

    val CheckSum: Double,
    val GrossAmount: Double,
    val JobDate: String,
    val JobDetails: List<JobDetail>,
    val JobEndDate: String,
    val JobNumber: String,
    val JobRules: List<JobRule>,
    val JobStartDate: String,
    val JobStatus: Int,
    val NetAmount: Double,
    val NoOfItems: Int,
    val NoOfRuleItems: Int,
    val Remarks: String,
    val SectionID: Int,
    val StoreID: Int,
    val UserID: Int
)