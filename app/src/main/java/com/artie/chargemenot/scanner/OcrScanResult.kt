package com.artie.chargemenot.scanner

import java.time.LocalDate

data class OcrScanResult(
    val amount: Double?,
    val dueDate: LocalDate?,
    val rawText: String
) {
    val hasActionableData: Boolean
        get() = amount != null || dueDate != null
}
