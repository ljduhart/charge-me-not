package com.artie.chargemenot.ui.viewmodels

data class SubscriptionAuditCard(
    val billId: Long,
    val name: String,
    val amount: Double
)

data class CostPerUseReportRow(
    val billId: Long,
    val name: String,
    val amount: Double,
    val usageCount: Int,
    val auditPromptCount: Int,
    val costPerUse: Double,
    val isPrimeWeed: Boolean
)

data class WeedWhackerUiState(
    val currentAuditCard: SubscriptionAuditCard? = null,
    val pendingAuditCount: Int = 0,
    val costPerUseReport: List<CostPerUseReportRow> = emptyList(),
    val primeWeedBillIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val auditSessionComplete: Boolean = false,
    val hasSubscriptions: Boolean = false
)
