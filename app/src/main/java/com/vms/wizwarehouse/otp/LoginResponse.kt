package com.vms.wizwarehouse.otp

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("msg")
    @Expose
    val msg: String?,

    @SerializedName("access_token")
    @Expose
    val accessToken: String,

    @SerializedName("role")
    @Expose
    val role: String
)
