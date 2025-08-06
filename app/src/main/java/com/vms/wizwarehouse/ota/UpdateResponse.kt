package com.vms.wizwarehouse.ota


import com.google.gson.annotations.SerializedName
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class UpdateResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: UpdateData?
) {
    data class UpdateData(
        @SerializedName("version") val version: String?,
        @SerializedName("fileName") val fileName: String?,
        @SerializedName("apkUrl") val apkUrl: String?,
        @SerializedName("created_at") val createdAt: String?
    ) {
        fun getFormattedDate(): String {
            return formatDate(createdAt)
        }

        private fun formatDate(apiDate: String?): String {
            if (apiDate.isNullOrEmpty()) {
                return "Unknown Date"
            }
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.US)
                val date = inputFormat.parse(apiDate)
                outputFormat.format(date ?: return "Invalid Date")
            } catch (e: ParseException) {
                e.printStackTrace()
                "Invalid Date"
            }
        }
    }
}