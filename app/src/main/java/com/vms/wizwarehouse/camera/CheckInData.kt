package com.vms.wizwarehouse.camera


data class CheckInData(
    val createdAt: String,
    val updatedAt: String,
    var id: Int,
    val userId: Int,
    val activityId: Int,
    val checkInLatitude: Double,
    val checkInLongitude: Double,
    val checkInAddress: String,
    val checkInSelfie: String,
    val checkInOutletSelfie: String,
    val commentCheckIn: String,
    val checkInDistance: Double,
    val checkInTime: String,
    val attendanceDate: String,
    val checkoutTime: String,
    val checkoutLatitude: Double,
    val checkoutLongitude: Double,
    val checkoutAddress: String,
    val checkoutDistance: Double,
    val checkoutSelfie: String,
    val checkoutOutletSelfie: String,
    val commentCheckOut: String
)