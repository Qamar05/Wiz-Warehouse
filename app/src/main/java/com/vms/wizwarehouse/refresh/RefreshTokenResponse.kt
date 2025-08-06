package com.vms.wizwarehouse.refresh

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("msg")
    val msg: String? = null,

    @SerializedName("access_token")
    val accessToken: String? = null,

    @SerializedName("role")
    val role: String? = null
)

