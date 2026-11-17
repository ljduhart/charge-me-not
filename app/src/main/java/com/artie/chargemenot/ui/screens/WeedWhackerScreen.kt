package com.artie.chargemenot.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.artie.chargemenot.R
import com.artie.chargemenot.ui.motion.MeadowMotion
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
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeedWhackerScreen(
    uiState: WeedWhackerUiState,
    onRecordAuditResponse: (Long, Boolean) -> Unit,
    onRestartAuditSession: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = uiState.selectedCurrency

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weed_whacker_title)) },
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
                currency = currency,
                onRecordAuditResponse = onRecordAuditResponse,
                onRestartAuditSession = onRestartAuditSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.48f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            GardenHealthReportSection(
                reportRows = uiState.costPerUseReport,
                currency = currency,
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
    currency: SupportedCurrency,
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
                    SubscriptionAuditCardStack(
                        currentCard = uiState.currentAuditCard,
                        upcomingCards = uiState.upcomingAuditCards,
                        currency = currency,
                        pendingAuditCount = uiState.pendingAuditCount,
                        onRecordAuditResponse = onRecordAuditResponse
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionAuditCardStack(
    currentCard: SubscriptionAuditCard,
    upcomingCards: List<SubscriptionAuditCard>,
    currency: SupportedCurrency,
    pendingAuditCount: Int,
    onRecordAuditResponse: (Long, Boolean) -> Unit
) {
    val density = LocalDensity.current
    val stackSpacingPx = remember(density) { with(density) { 14.dp.toPx() } }
    val peekCards = upcomingCards.take(2)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            peekCards.asReversed().forEachIndexed { reversedIndex, card ->
                val depth = peekCards.size - reversedIndex
                SubscriptionAuditCardFace(
                    card = card,
                    currency = currency,
                    pendingAuditCount = pendingAuditCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(depth.toFloat())
                        .graphicsLayer {
                            val scale = MeadowMotion.weedWhackerStackScale(depth)
                            scaleX = scale
                            scaleY = scale
                            translationY = MeadowMotion.weedWhackerStackOffsetY(depth, stackSpacingPx)
                            alpha = 0.72f
                        }
                )
            }
            SwipeableAuditCard(
                card = currentCard,
                currency = currency,
                pendingAuditCount = pendingAuditCount,
                onRecordAuditResponse = onRecordAuditResponse,
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex((peekCards.size + 1).toFloat())
            )
        }
        Text(
            text = stringResource(R.string.weed_whacker_swipe_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
private fun SwipeableAuditCard(
    card: SubscriptionAuditCard,
    currency: SupportedCurrency,
    pendingAuditCount: Int,
    onRecordAuditResponse: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val thresholdPx = remember(density) { with(density) { 120.dp.toPx() } }
    val yesLabel = stringResource(R.string.weed_whacker_used_yes)
    val noLabel = stringResource(R.string.weed_whacker_used_no)

    key(card.billId) {
        var dragging by remember { mutableStateOf(false) }
        var dragX by remember { mutableFloatStateOf(0f) }
        var dragY by remember { mutableFloatStateOf(0f) }
        val settledX by animateFloatAsState(
            targetValue = if (dragging) dragX else 0f,
            animationSpec = tween(durationMillis = 220),
            label = "weed_whacker_swipe_x"
        )
        val settledY by animateFloatAsState(
            targetValue = if (dragging) dragY else 0f,
            animationSpec = tween(durationMillis = 220),
            label = "weed_whacker_swipe_y"
        )
        val displayX = if (dragging) dragX else settledX
        val displayY = if (dragging) dragY else settledY

        Box(
            modifier = modifier
                .semantics {
                    customActions = listOf(
                        CustomAccessibilityAction(yesLabel) {
                            onRecordAuditResponse(card.billId, true)
                            true
                        },
                        CustomAccessibilityAction(noLabel) {
                            onRecordAuditResponse(card.billId, false)
                            true
                        }
                    )
                }
                .pointerInput(card.billId, thresholdPx) {
                    detectDragGestures(
                        onDragStart = {
                            dragging = true
                        },
                        onDragEnd = {
                            val used = MeadowMotion.weedWhackerSwipeDecision(dragX, thresholdPx)
                            dragging = false
                            dragX = 0f
                            dragY = 0f
                            if (used != null) {
                                onRecordAuditResponse(card.billId, used)
                            }
                        },
                        onDragCancel = {
                            dragging = false
                            dragX = 0f
                            dragY = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount.x
                        dragY += dragAmount.y * 0.35f
                    }
                }
                .graphicsLayer {
                    translationX = displayX
                    translationY = displayY
                    rotationZ = MeadowMotion.weedWhackerSwipeRotation(displayX)
                }
        ) {
            SubscriptionAuditCardFace(
                card = card,
                currency = currency,
                pendingAuditCount = pendingAuditCount,
                swipeOffsetX = displayX,
                swipeThreshold = thresholdPx,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SubscriptionAuditCardFace(
    card: SubscriptionAuditCard,
    currency: SupportedCurrency,
    pendingAuditCount: Int,
    modifier: Modifier = Modifier,
    swipeOffsetX: Float = 0f,
    swipeThreshold: Float = 1f
) {
    val tintAlpha = MeadowMotion.weedWhackerSwipeTintAlpha(swipeOffsetX, swipeThreshold)
    val overlayColor = when {
        swipeOffsetX > 0f -> LeafGreen.copy(alpha = tintAlpha)
        swipeOffsetX < 0f -> WeedRed.copy(alpha = tintAlpha)
        else -> null
    }
    val labelAlpha = (tintAlpha / 0.42f).coerceIn(0f, 1f)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
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
                        CurrencyFormatter.format(card.amount, currency)
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
            }
            overlayColor?.let { color ->
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(color)
                )
            }
            if (swipeOffsetX > 0f && labelAlpha > 0f) {
                Text(
                    text = stringResource(R.string.weed_whacker_swipe_yes_label),
                    color = LeafGreen.copy(alpha = labelAlpha),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .graphicsLayer { rotationZ = -12f }
                )
            } else if (swipeOffsetX < 0f && labelAlpha > 0f) {
                Text(
                    text = stringResource(R.string.weed_whacker_swipe_no_label),
                    color = WeedRed.copy(alpha = labelAlpha),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .graphicsLayer { rotationZ = 12f }
                )
            }
        }
    }
}

@Composable
private fun GardenHealthReportSection(
    reportRows: List<CostPerUseReportRow>,
    currency: SupportedCurrency,
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
                    currency = currency
                )
            }
        }
    }
}

@Composable
private fun CostPerUseReportCard(
    row: CostPerUseReportRow,
    currency: SupportedCurrency
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
                        CurrencyFormatter.format(row.costPerUse, currency)
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
                text = CurrencyFormatter.format(row.amount, currency),
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
                    amount = 1_549L
                ),
                upcomingAuditCards = listOf(
                    SubscriptionAuditCard(
                        billId = 2L,
                        name = "Spotify Premium",
                        amount = 1_199L
                    )
                ),
                pendingAuditCount = 2,
                costPerUseReport = listOf(
                    CostPerUseReportRow(
                        billId = 1L,
                        name = "Netflix",
                        amount = 1_549L,
                        usageCount = 0,
                        auditPromptCount = 3,
                        costPerUse = 1_549L,
                        isPrimeWeed = true
                    ),
                    CostPerUseReportRow(
                        billId = 2L,
                        name = "Spotify Premium",
                        amount = 1_199L,
                        usageCount = 4,
                        auditPromptCount = 2,
                        costPerUse = 299L,
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
