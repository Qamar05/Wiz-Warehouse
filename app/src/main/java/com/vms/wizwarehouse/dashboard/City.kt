package com.vms.wizwarehouse.dashboard

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.List;

data class City(
    @SerializedName("id")
    @Expose val id: Int,

    @SerializedName("city_name")
    @Expose val cityName: String,  // Remove 'private' to access it directly

    @SerializedName("state_name")
    @Expose val stateName: String,

    @SerializedName("brands")
    @Expose val brands: List<Brand>
)

