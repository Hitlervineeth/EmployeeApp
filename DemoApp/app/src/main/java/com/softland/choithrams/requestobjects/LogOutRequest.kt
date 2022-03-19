package com.softland.choithrams.requestobjects

data class LogOutRequest(
    val Credentials: Credentials,
    val RequestData: RequestDataForLogOut
)