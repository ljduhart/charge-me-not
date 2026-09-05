package com.artie.chargemenot.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageUtil {

    private const val RECEIPTS_DIRECTORY = "receipts"
    private const val JPEG_QUALITY = 85

    fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        filename: String
    ): String {
        val receiptsDir = File(context.filesDir, RECEIPTS_DIRECTORY)
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs()
        }

        val sanitizedFilename = filename.ifBlank { "receipt_${UUID.randomUUID()}.jpg" }
        val targetFile = File(receiptsDir, sanitizedFilename)

        FileOutputStream(targetFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        }

        return targetFile.absolutePath
    }

    fun deleteReceiptImage(receiptImagePath: String?) {
        if (receiptImagePath.isNullOrBlank()) {
            return
        }
        runCatching {
            File(receiptImagePath).delete()
        }
    }
}
