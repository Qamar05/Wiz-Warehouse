package com.vms.wizwarehouse.dashboard;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


data class Brand(
        @SerializedName("id")
        @Expose
        var id: Int,

        @SerializedName("brand_name")
        @Expose
        val brandName: String,

        @SerializedName("variants")
        @Expose
        val variants:List<Variant>?
)