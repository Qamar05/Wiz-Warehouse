package com.vms.wizwarehouse.utils;



import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.location.LocationManager
import com.vms.wizwarehouse.BuildConfig
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.login.LoginActivity

object Utility {

@JvmStatic
fun isGpsEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    locationManager.getProvider(LocationManager.GPS_PROVIDER)
    return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

@JvmStatic
fun printMultiLineAddressOnImage(originalBitmap: Bitmap, address: String): Bitmap {
    val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 50f
        textAlign = Paint.Align.CENTER
    }

    val bitmapWidth = mutableBitmap.width
    val bitmapHeight = mutableBitmap.height

    val lines = getWrappedText(address, textPaint, bitmapWidth - 40)
    val lineHeight = 60 // 50 text size + 10 spacing
    val totalTextHeight = lines.size * lineHeight

    val padding = 20
    val backgroundHeight = totalTextHeight + (2 * padding)
    val backgroundTop = bitmapHeight - backgroundHeight

    val backgroundPaint = Paint().apply {
        color = Color.BLACK
        alpha = 180
    }

    canvas.drawRect(0f, backgroundTop.toFloat(), bitmapWidth.toFloat(), bitmapHeight.toFloat(), backgroundPaint)

    var textY = bitmapHeight - backgroundHeight + padding + 50f
    val textX = bitmapWidth / 2f

    for (line in lines) {
        canvas.drawText(line, textX, textY, textPaint)
        textY += lineHeight
    }

    return mutableBitmap
}

private fun getWrappedText(text: String, paint: Paint, maxWidth: Int): List<String> {
    val lines = mutableListOf<String>()
    val words = text.split(" ")
    var currentLine = StringBuilder()

    for (word in words) {
        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
        val bounds = Rect()
        paint.getTextBounds(testLine, 0, testLine.length, bounds)

        if (bounds.width() > maxWidth) {
            lines.add(currentLine.toString())
            currentLine = StringBuilder(word)
        } else {
            currentLine = StringBuilder(testLine)
        }
    }
    lines.add(currentLine.toString()) // Add the last line

    return lines
}

@JvmStatic
fun setVersionName(context: Context): String {
    return context.getString(R.string.app_version) + " " + BuildConfig.VERSION_NAME
}

@JvmStatic
fun logoutUser(context: Context) {
    SharedPreferenceUtils.saveBoolean(context, SharedPreferenceUtils.IS_LOGGED_IN_FWP, false)
    SharedPreferenceUtils.saveBoolean(context, SharedPreferenceUtils.IS_LOGGED_IN_WAREHOUSE, false)
    SharedPreferenceUtils.saveBoolean(context, SharedPreferenceUtils.IS_CHECKED_IN_FWP, false)

    val intent = Intent(context, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
}
