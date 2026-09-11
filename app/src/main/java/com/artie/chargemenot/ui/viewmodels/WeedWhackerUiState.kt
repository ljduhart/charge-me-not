package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.domain.model.SupportedCurrency

data class SubscriptionAuditCard(
    val billId: Long,
    val name: String,
    val amount: Long
)

data class CostPerUseReportRow(
    val billId: Long,
    val name: String,
    val amount: Long,
    val usageCount: Int,
    val auditPromptCount: Int,
    val costPerUse: Long,
    val isPrimeWeed: Boolean
)

data class WeedWhackerUiState(
    val currentAuditCard: SubscriptionAuditCard? = null,
    val pendingAuditCount: Int = 0,
    val costPerUseReport: List<CostPerUseReportRow> = emptyList(),
    val primeWeedBillIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val auditSessionComplete: Boolean = false,
    val hasSubscriptions: Boolean = false,
    val selectedCurrency: SupportedCurrency = SupportedCurrency.USD
)
