package com.artie.chargemenot.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class BillOcrAnalyzer(
    private val onScanResult: (OcrScanResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val isProcessing = AtomicBoolean(false)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            isProcessing.set(false)
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val result = extractBillData(visionText.text)
                if (result.hasActionableData) {
                    onScanResult(result)
                }
            }
            .addOnFailureListener {
                // Frame failed OCR processing; continue scanning.
            }
            .addOnCompleteListener {
                isProcessing.set(false)
                imageProxy.close()
            }
    }

    fun close() {
        textRecognizer.close()
    }

    internal fun extractBillData(rawText: String): OcrScanResult {
        val normalizedText = rawText.replace('\n', ' ')
        return OcrScanResult(
            amount = extractAmount(normalizedText),
            dueDate = extractDueDate(normalizedText),
            rawText = rawText
        )
    }

    private fun extractAmount(text: String): Double? {
        val labeledMatches = AMOUNT_WITH_LABEL_PATTERN
            .findAll(text)
            .mapNotNull { match -> parseCurrency(match.groupValues[1]) }
            .toList()

        if (labeledMatches.isNotEmpty()) {
            return labeledMatches.maxOrNull()
        }

        return CURRENCY_PATTERN
            .findAll(text)
            .mapNotNull { match -> parseCurrency(match.groupValues[1]) }
            .maxOrNull()
    }

    private fun extractDueDate(text: String): LocalDate? {
        DUE_DATE_WITH_LABEL_PATTERN.find(text)?.let { match ->
            parseDateToken(match.groupValues[1])?.let { return it }
        }

        for (pattern in DATE_PATTERNS) {
            val match = pattern.find(text) ?: continue
            val parsed = if (pattern == NUMERIC_DATE_PATTERN) {
                parseNumericDate(
                    month = match.groupValues[1],
                    day = match.groupValues[2],
                    year = match.groupValues[3]
                )
            } else {
                parseMonthNameDate(
                    month = match.groupValues[1],
                    day = match.groupValues[2],
                    year = match.groupValues[3]
                )
            }
            if (parsed != null) return parsed
        }

        return null
    }

    private fun parseCurrency(value: String): Double? {
        val sanitized = value.replace(",", "")
        return sanitized.toDoubleOrNull()?.takeIf { it > 0.0 }
    }

    private fun parseDateToken(token: String): LocalDate? {
        val trimmed = token.trim()
        NUMERIC_DATE_PATTERN.find(trimmed)?.let { match ->
            return parseNumericDate(
                month = match.groupValues[1],
                day = match.groupValues[2],
                year = match.groupValues[3]
            )
        }
        return parseMonthNameDate(trimmed)
    }

    private fun parseNumericDate(month: String, day: String, year: String): LocalDate? {
        val monthValue = month.toIntOrNull() ?: return null
        val dayValue = day.toIntOrNull() ?: return null
        val yearValue = normalizeYear(year.toIntOrNull() ?: return null)

        return runCatching {
            LocalDate.of(yearValue, monthValue, dayValue)
        }.getOrNull()
    }

    private fun parseMonthNameDate(month: String, day: String, year: String): LocalDate? {
        val normalizedMonth = month.take(3).replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
        }
        val candidate = "$normalizedMonth $day, $year"

        for (formatter in MONTH_NAME_FORMATTERS) {
            try {
                return LocalDate.parse(candidate, formatter)
            } catch (_: DateTimeParseException) {
                // Try next formatter.
            }
        }
        return null
    }

    private fun parseMonthNameDate(value: String): LocalDate? {
        for (formatter in MONTH_NAME_FORMATTERS) {
            try {
                return LocalDate.parse(value.trim(), formatter)
            } catch (_: DateTimeParseException) {
                // Try next formatter.
            }
        }
        return null
    }

    private fun normalizeYear(year: Int): Int {
        return if (year < 100) 2000 + year else year
    }

    companion object {
        private val AMOUNT_WITH_LABEL_PATTERN = Regex(
            pattern = """(?:total|amount|due|balance|pay(?:ment)?)\s*:?\s*\$?\s*(\d{1,3}(?:,\d{3})*(?:\.\d{2})|\d+\.\d{2})""",
            option = RegexOption.IGNORE_CASE
        )

        private val CURRENCY_PATTERN = Regex(
            pattern = """\$\s*(\d{1,3}(?:,\d{3})*\.\d{2}|\d+\.\d{2})"""
        )

        private val DUE_DATE_WITH_LABEL_PATTERN = Regex(
            pattern = """(?:due|date)\s*(?:date)?\s*:?\s*([A-Za-z0-9,\s/\-]+)""",
            option = RegexOption.IGNORE_CASE
        )

        private val NUMERIC_DATE_PATTERN = Regex(
            pattern = """(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})"""
        )

        private val DATE_PATTERNS = listOf(
            NUMERIC_DATE_PATTERN,
            Regex(
                pattern = """(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+(\d{1,2}),?\s+(\d{4})""",
                option = RegexOption.IGNORE_CASE
            )
        )

        private val MONTH_NAME_FORMATTERS = listOf(
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US),
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US),
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US),
            DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.US)
        )
    }
}
