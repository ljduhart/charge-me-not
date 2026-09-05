package com.artie.chargemenot.scanner

import android.graphics.Bitmap
import java.time.LocalDate

data class OcrScanResult(
    val amount: Double?,
    val dueDate: LocalDate?,
    val rawText: String,
    val receiptBitmap: Bitmap? = null
) {
    val hasActionableData: Boolean
        get() = amount != null || dueDate != null
}
