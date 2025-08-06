package com.vms.wizwarehouse.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

object BitmapUtils {

fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
@SuppressLint("UnsafeOptInUsageError")
val mediaImage = image.image
        if (mediaImage != null) {
var bitmap = imageToBitmap(mediaImage)
val rotationDegrees = image.imageInfo.rotationDegrees

            if (rotationDegrees != 0) {
val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
bitmap = Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
)
            }

                    return resizeBitmap(bitmap)
        }
                return null
                }

private fun imageToBitmap(image: Image): Bitmap {
    val buffer: ByteBuffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private fun resizeBitmap(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    val aspectRatio = width.toFloat() / height
    val newWidth = minOf(800, width)
    val newHeight = (newWidth / aspectRatio).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}
}
