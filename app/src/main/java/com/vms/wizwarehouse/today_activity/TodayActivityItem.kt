package com.vms.wizwarehouse.today_activity

data class TodayActivityItem (
    val id: String,
    val activityType: String,
    val supervisor: String,
    val fwp: String,
    val timeRange: String,
    val outlet: String
)