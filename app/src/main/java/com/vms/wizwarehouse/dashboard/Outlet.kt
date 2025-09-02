package com.vms.wizwarehouse.dashboard

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Outlet(
    @SerializedName("id")
    @Expose
    private var id: Int,

    @SerializedName("outlet_name")
    @Expose
    private val outletName: String,

    @SerializedName("city")
    @Expose val city: City
)