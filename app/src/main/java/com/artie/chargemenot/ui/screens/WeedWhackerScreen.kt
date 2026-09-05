package com.artie.chargemenot.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.LeafGreen
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.theme.WeedRed
import com.artie.chargemenot.ui.viewmodels.CostPerUseReportRow
import com.artie.chargemenot.ui.viewmodels.SubscriptionAuditCard
import com.artie.chargemenot.ui.viewmodels.WeedWhackerUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeedWhackerScreen(
    uiState: WeedWhackerUiState,
    onRecordAuditResponse: (Long, Boolean) -> Unit,
    onRestartAuditSession: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weed_whacker_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.weed_whacker_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MeadowGreen,
                    titleContentColor = MeadowWhite,
                    navigationIconContentColor = MeadowWhite
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MeadowGreen)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            AuditPromptSection(
                uiState = uiState,
                currencyFormat = currencyFormat,
                onRecordAuditResponse = onRecordAuditResponse,
                onRestartAuditSession = onRestartAuditSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.48f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            GardenHealthReportSection(
                reportRows = uiState.costPerUseReport,
                currencyFormat = currencyFormat,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.52f)
            )
        }
    }
}

@Composable
private fun AuditPromptSection(
    uiState: WeedWhackerUiState,
    currencyFormat: NumberFormat,
    onRecordAuditResponse: (Long, Boolean) -> Unit,
    onRestartAuditSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MeadowSage.copy(alpha = 0.18f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.weed_whacker_audit_header),
                style = MaterialTheme.typography.titleMedium,
                color = MeadowGreenDark,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                !uiState.hasSubscriptions -> {
                    Text(
                        text = stringResource(R.string.weed_whacker_no_subscriptions),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                uiState.auditSessionComplete -> {
                    Text(
                        text = stringResource(R.string.weed_whacker_audit_complete),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onRestartAuditSession,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.weed_whacker_restart_audit))
                    }
                }

                uiState.currentAuditCard != null -> {
                    SubscriptionAuditCardContent(
                        card = uiState.currentAuditCard,
                        currencyFormat = currencyFormat,
                        pendingAuditCount = uiState.pendingAuditCount,
                        onRecordAuditResponse = onRecordAuditResponse
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionAuditCardContent(
    card: SubscriptionAuditCard,
    currencyFormat: NumberFormat,
    pendingAuditCount: Int,
    onRecordAuditResponse: (Long, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.weed_whacker_audit_question, card.name),
                style = MaterialTheme.typography.titleLarge,
                color = MeadowGreenDark,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(
                    R.string.weed_whacker_audit_amount,
                    currencyFormat.format(card.amount)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = stringResource(R.string.weed_whacker_pending_count, pendingAuditCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onRecordAuditResponse(card.billId, true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LeafGreen,
                        contentColor = MeadowWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.weed_whacker_used_yes))
                }

                Button(
                    onClick = { onRecordAuditResponse(card.billId, false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WeedRed,
                        contentColor = MeadowWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.weed_whacker_used_no))
                }
            }
        }
    }
}

@Composable
private fun GardenHealthReportSection(
    reportRows: List<CostPerUseReportRow>,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.weed_whacker_report_title),
            style = MaterialTheme.typography.titleMedium,
            color = MeadowGreenDark,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.weed_whacker_report_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
        )

        if (reportRows.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = stringResource(R.string.weed_whacker_report_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = reportRows,
                key = { row -> row.billId }
            ) { row ->
                CostPerUseReportCard(
                    row = row,
                    currencyFormat = currencyFormat
                )
            }
        }
    }
}

@Composable
private fun CostPerUseReportCard(
    row: CostPerUseReportRow,
    currencyFormat: NumberFormat
) {
    val containerColor = if (row.isPrimeWeed) {
        WeedRed.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val nameColor = if (row.isPrimeWeed) WeedRed else MaterialTheme.colorScheme.onSurface
    val costColor = if (row.isPrimeWeed) WeedRed else MeadowGreenDark

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (row.isPrimeWeed) {
                            WeedRed.copy(alpha = 0.2f)
                        } else {
                            MeadowSage.copy(alpha = 0.35f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (row.isPrimeWeed) Icons.Default.ContentCut else Icons.Default.Eco,
                    contentDescription = null,
                    tint = if (row.isPrimeWeed) WeedRed else LeafGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = nameColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(
                        R.string.weed_whacker_cost_per_use,
                        currencyFormat.format(row.costPerUse)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = costColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        R.string.weed_whacker_usage_stats,
                        row.usageCount,
                        row.auditPromptCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (row.isPrimeWeed) {
                    Text(
                        text = stringResource(R.string.weed_whacker_prime_weed_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = WeedRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Text(
                text = currencyFormat.format(row.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WeedWhackerScreenPreview() {
    ChargeMeNotTheme {
        WeedWhackerScreen(
            uiState = WeedWhackerUiState(
                currentAuditCard = SubscriptionAuditCard(
                    billId = 1L,
                    name = "Netflix",
                    amount = 15.49
                ),
                pendingAuditCount = 2,
                costPerUseReport = listOf(
                    CostPerUseReportRow(
                        billId = 1L,
                        name = "Netflix",
                        amount = 15.49,
                        usageCount = 0,
                        auditPromptCount = 3,
                        costPerUse = 15.49,
                        isPrimeWeed = true
                    ),
                    CostPerUseReportRow(
                        billId = 2L,
                        name = "Spotify Premium",
                        amount = 11.99,
                        usageCount = 4,
                        auditPromptCount = 2,
                        costPerUse = 2.9975,
                        isPrimeWeed = false
                    )
                ),
                primeWeedBillIds = setOf(1L),
                isLoading = false,
                auditSessionComplete = false,
                hasSubscriptions = true
            ),
            onRecordAuditResponse = { _, _ -> },
            onRestartAuditSession = {},
            onNavigateBack = {}
        )
    }
}
