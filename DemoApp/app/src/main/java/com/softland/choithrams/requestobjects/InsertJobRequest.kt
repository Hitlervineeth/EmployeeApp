package com.softland.choithrams.requestobjects

data class InsertJobRequest(
    val Credentials: Credentials,
    val RequestData: InsertJobDetailsRequest
)