package com.vms.wizwarehouse.dashboard;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

data class TodayActivityResponse(
        @SerializedName("id")
        @Expose
        var id: Int,

        @SerializedName("city_id")
        @Expose
        val cityId: Int,

        @SerializedName("outlet_id")
        @Expose
        val outletId: Int,

        @SerializedName("activity_type")
        @Expose
        val activityType: String,

        @SerializedName("start_date")
        @Expose
        val startDate: String,

        @SerializedName("end_date")
        @Expose
        val endDate: String,

        @SerializedName("start_time")
        @Expose
        val startTime: String,

        @SerializedName("end_time")
        @Expose
        val endTime: String,

        @SerializedName("activity_day")
        @Expose
        val activityDay: String,

        @SerializedName("supervisor_id")
        @Expose
        val supervisorId: Int,

        @SerializedName("fwp1_id")
        @Expose
        val fwp1Id: Int,

        @SerializedName("fwp2_id")
        @Expose
        val fwp2Id: Int,

        @SerializedName("created_at")
        @Expose
        val createdAt: String,

        @SerializedName("updated_at")
        @Expose
        val updatedAt: String,

        @SerializedName("outlet")
        @Expose
        val outlet: Outlet,

        @SerializedName("msg")
        @Expose
        var message: String,

        @SerializedName("isGeofencing")
        @Expose
        val isGeofencing: Boolean

)
