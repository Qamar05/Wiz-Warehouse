package com.vms.wizwarehouse.refresh;

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
        @SerializedName("token")
        val token: String
)
