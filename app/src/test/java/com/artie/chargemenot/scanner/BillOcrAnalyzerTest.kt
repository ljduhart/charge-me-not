package com.artie.chargemenot.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class BillOcrAnalyzerTest {

  private val analyzer = BillOcrAnalyzer(
    onScanResult = {},
    onQrPayloadDetected = {}
  )

  @Test
  fun extractBillData_findsLabeledTotalAndNumericDueDate() {
    val result = analyzer.extractBillData(
      """
      Pacific Gas & Electric
      Account Summary
      Total: $94.17
      Due Date: 09/12/2026
      """.trimIndent()
    )

    assertEquals(94.17, result.amount!!, 0.001)
    assertEquals(LocalDate.of(2026, 9, 12), result.dueDate)
  }

  @Test
  fun extractBillData_prefersLabeledAmountOverSmallerCurrencyValues() {
    val result = analyzer.extractBillData(
      "Service fee $1.25 Amount Due: $1,450.00 Payment due 10/01/2026"
    )

    assertEquals(1450.0, result.amount!!, 0.001)
    assertEquals(LocalDate.of(2026, 10, 1), result.dueDate)
  }

  @Test
  fun extractBillData_parsesMonthNameDueDate() {
    val result = analyzer.extractBillData(
      "Balance $54.99 Due Sep 15, 2026"
    )

    assertEquals(54.99, result.amount!!, 0.001)
    assertEquals(LocalDate.of(2026, 9, 15), result.dueDate)
  }

  @Test
  fun extractBillData_returnsNullsWhenNoMatches() {
    val result = analyzer.extractBillData("Thank you for your business")

    assertNull(result.amount)
    assertNull(result.dueDate)
  }
}
