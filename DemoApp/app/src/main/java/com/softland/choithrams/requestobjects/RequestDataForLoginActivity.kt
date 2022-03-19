package com.softland.choithrams.requestobjects

data class RequestDataForLoginActivity(
    var APKVersion: String?,
    var AndroidVersion: String?,
    var BluetoothID: String?,
    var DeviceName: String?,
    var ImeiNumber: String?,
    var MacId: String?,
    var Password: String?,
    var UserName: String?
)