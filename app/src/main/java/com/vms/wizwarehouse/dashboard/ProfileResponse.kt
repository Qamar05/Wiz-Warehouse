package com.vms.wizwarehouse.dashboard;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

data class ProfileResponse(
        @SerializedName("id")
        @Expose
        var id: Int,

        @SerializedName("firstname")
        @Expose
        val firstname: String,

        @SerializedName("lastname")
        @Expose
        val lastname: String,

        @SerializedName("email")
        @Expose
        val email: String,

        @SerializedName("phone")
        @Expose
        val phone: String,

        @SerializedName("role")
        @Expose
        val role: String,

        @SerializedName("dob")
        @Expose
        val dob: String,

        @SerializedName("state")
        @Expose
        val state: String,

        @SerializedName("city")
        @Expose
        val city: String,

        @SerializedName("c4_validation")
        @Expose
        val c4Validation: Int,

        @SerializedName("user_code")
        @Expose
        val userCode: String,

        @SerializedName("lastCheckStatus")
        @Expose
        val lastCheckStatus: String
)

