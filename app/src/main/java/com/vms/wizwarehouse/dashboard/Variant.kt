package com.vms.wizwarehouse.dashboard

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.List;

data class Variant(
    @SerializedName("id")
    @Expose
    val id: Int,

    @SerializedName("variant_name")
    @Expose
    val variantName: String,

    @SerializedName("packSize")
    @Expose
    var packSize: List<String>? = null
)