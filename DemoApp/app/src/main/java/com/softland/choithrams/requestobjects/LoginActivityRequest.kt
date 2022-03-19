package com.softland.choithrams.requestobjects

import com.google.gson.annotations.SerializedName

data class LoginActivityRequest(
    @SerializedName("Credentials")
    val Credentials: Credentials?,

    @SerializedName("RequestData")
    val RequestData: RequestDataForLoginActivity?
) {
}