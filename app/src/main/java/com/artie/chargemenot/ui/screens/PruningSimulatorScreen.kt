package com.artie.chargemenot.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.ui.components.FinancialBloomCanvas
import com.artie.chargemenot.ui.components.categoryColor
import com.artie.chargemenot.ui.components.categoryDisplayName
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.LeafGreen
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowGreenLight
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.theme.WeedRed
import com.artie.chargemenot.ui.viewmodels.PruningUiState
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruningSimulatorScreen(
    uiState: PruningUiState,
    onToggleBillStatus: (Long, Boolean) -> Unit,
    onResetSandbox: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = DateTimeFormatter.ofPattern("MMM d")

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pruning_simulator_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.pruning_simulator_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MeadowGreen,
                    titleContentColor = MeadowWhite,
                    navigationIconContentColor = MeadowWhite
                )
            )
        },
        bottomBar = {
            PruningActionRow(
                onResetSandbox = onResetSandbox,
                onNavigateBack = onNavigateBack
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
        ) {
            PruningBloomSection(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.42f)
            )

            PruningSummarySection(
                newMonthlyTotal = currencyFormat.format(uiState.newMonthlyTotal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )

            PruningBillsList(
                bills = uiState.bills,
                prunedBillIds = uiState.prunedBillIds,
                currencyFormat = currencyFormat,
                dateFormat = dateFormat,
                onToggleBillStatus = onToggleBillStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.58f)
            )
        }
    }
}

@Composable
private fun PruningBloomSection(
    uiState: PruningUiState,
    modifier: Modifier = Modifier
) {
    val animatedCategoryTotals = animateCategoryTotals(uiState.projectedCategoryTotals)
    val animatedCategoryAlphas = animateCategoryAlphas(uiState.categoryAlphas)

    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MeadowSage.copy(alpha = 0.15f),
                            MeadowGreenLight.copy(alpha = 0.08f),
                            MeadowWhite
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            FinancialBloomCanvas(
                categoryTotals = uiState.originalCategoryTotals,
                projectedCategoryTotals = animatedCategoryTotals,
                categoryAlphas = animatedCategoryAlphas,
                monthlyBudget = uiState.monthlyBudget,
                sizeByMonthlyBudget = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun animateCategoryTotals(
    targetTotals: Map<BillCategory, Double>
): Map<BillCategory, Double> {
    return BillCategory.entries.associateWith { category ->
        val target = (targetTotals[category] ?: 0.0).toFloat()
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = tween(durationMillis = 450),
            label = "pruningCategoryTotal_${category.name}"
        )
        animated.toDouble()
    }.filterValues { amount -> amount > 0.0 }
}

@Composable
private fun animateCategoryAlphas(
    targetAlphas: Map<BillCategory, Float>
): Map<BillCategory, Float> {
    return BillCategory.entries.associateWith { category ->
        val target = targetAlphas[category] ?: 1f
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = tween(durationMillis = 450),
            label = "pruningCategoryAlpha_${category.name}"
        )
        animated
    }
}

@Composable
private fun PruningSummarySection(
    newMonthlyTotal: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MeadowSage.copy(alpha = 0.22f)
        )
    ) {
        Text(
            text = stringResource(R.string.pruning_simulator_summary, newMonthlyTotal),
            style = MaterialTheme.typography.titleMedium,
            color = MeadowGreenDark,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun PruningBillsList(
    bills: List<BillEntity>,
    prunedBillIds: Set<Long>,
    currencyFormat: NumberFormat,
    dateFormat: DateTimeFormatter,
    onToggleBillStatus: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.pruning_simulator_bills_header),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        if (bills.isEmpty()) {
            Text(
                text = stringResource(R.string.pruning_simulator_empty_garden),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = bills,
                key = { bill -> bill.id }
            ) { bill ->
                PruningBillRow(
                    bill = bill,
                    isPruned = bill.id in prunedBillIds,
                    currencyFormat = currencyFormat,
                    dateFormat = dateFormat,
                    onToggleBillStatus = onToggleBillStatus
                )
            }
        }
    }
}

@Composable
private fun PruningBillRow(
    bill: BillEntity,
    isPruned: Boolean,
    currencyFormat: NumberFormat,
    dateFormat: DateTimeFormatter,
    onToggleBillStatus: (Long, Boolean) -> Unit
) {
    val rowAlpha by animateFloatAsState(
        targetValue = if (isPruned) 0.45f else 1f,
        animationSpec = tween(durationMillis = 350),
        label = "pruningBillRowAlpha_${bill.id}"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(categoryColor(bill.category).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bill.name.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MeadowGreenDark,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${categoryDisplayName(bill.category)} · Due ${bill.dueDate.format(dateFormat)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currencyFormat.format(bill.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Switch(
                checked = isPruned,
                onCheckedChange = { isPrunedChecked ->
                    onToggleBillStatus(bill.id, isPrunedChecked)
                },
                thumbContent = {
                    Icon(
                        imageVector = if (isPruned) Icons.Default.ContentCut else Icons.Default.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MeadowWhite,
                    checkedTrackColor = WeedRed.copy(alpha = 0.7f),
                    uncheckedThumbColor = MeadowWhite,
                    uncheckedTrackColor = LeafGreen.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
private fun PruningActionRow(
    onResetSandbox: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onResetSandbox,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(stringResource(R.string.pruning_simulator_reset))
        }

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MeadowGreen,
                contentColor = MeadowWhite
            )
        ) {
            Text(stringResource(R.string.pruning_simulator_looks_good))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PruningSimulatorScreenPreview() {
    val today = LocalDate.of(2026, 9, 5)
    val bills = listOf(
        BillEntity(1, "Spotify Premium", 11.99, today.plusDays(12), BillCategory.SUBSCRIPTIONS),
        BillEntity(2, "Netflix", 15.49, today.plusDays(12), BillCategory.SUBSCRIPTIONS),
        BillEntity(3, "Pacific Gas & Electric", 94.17, today.plusDays(8), BillCategory.UTILITIES)
    )
    val categoryTotals = bills.groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { bill -> bill.amount } }

    ChargeMeNotTheme {
        PruningSimulatorScreen(
            uiState = PruningUiState(
                bills = bills,
                prunedBillIds = setOf(1L),
                originalCategoryTotals = categoryTotals,
                projectedCategoryTotals = mapOf(
                    BillCategory.SUBSCRIPTIONS to 15.49,
                    BillCategory.UTILITIES to 94.17
                ),
                categoryAlphas = mapOf(
                    BillCategory.SUBSCRIPTIONS to 0.56f,
                    BillCategory.UTILITIES to 1f
                ),
                newMonthlyTotal = 109.66,
                isLoading = false
            ),
            onToggleBillStatus = { _, _ -> },
            onResetSandbox = {},
            onNavigateBack = {}
        )
    }
}
