package com.artie.chargemenot.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

object ImageProxyConverter {

    fun toBitmap(imageProxy: ImageProxy): Bitmap? {
        val image = imageProxy.image ?: return null
        if (image.format != ImageFormat.YUV_420_888) {
            return null
        }

        val nv21 = yuv420888ToNv21(imageProxy)
        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )
        val outputStream = ByteArrayOutputStream()
        val compressed = yuvImage.compressToJpeg(
            Rect(0, 0, imageProxy.width, imageProxy.height),
            90,
            outputStream
        )
        if (!compressed) {
            return null
        }

        val jpegBytes = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }

    private fun yuv420888ToNv21(imageProxy: ImageProxy): ByteArray {
        val width = imageProxy.width
        val height = imageProxy.height
        val yPlane = imageProxy.planes[0]
        val uPlane = imageProxy.planes[1]
        val vPlane = imageProxy.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val ySize = yBuffer.remaining()
        val nv21 = ByteArray(width * height + (width * height / 2))
        yBuffer.get(nv21, 0, ySize)

        val chromaHeight = height / 2
        val chromaWidth = width / 2
        var outputOffset = width * height

        for (row in 0 until chromaHeight) {
            for (col in 0 until chromaWidth) {
                val vuOffset = row * vPlane.rowStride + col * vPlane.pixelStride
                nv21[outputOffset++] = vBuffer.get(vuOffset)
                nv21[outputOffset++] = uBuffer.get(row * uPlane.rowStride + col * uPlane.pixelStride)
            }
        }

        return nv21
    }
}
