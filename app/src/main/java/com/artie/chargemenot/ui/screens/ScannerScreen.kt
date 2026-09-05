package com.artie.chargemenot.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.scanner.BillOcrAnalyzer
import com.artie.chargemenot.scanner.OcrScanResult
import com.artie.chargemenot.ui.components.FinancialBloomCanvas
import com.artie.chargemenot.ui.components.categoryColor
import com.artie.chargemenot.ui.components.categoryDisplayName
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowWhite
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.res.stringResource
import com.artie.chargemenot.R
import com.artie.chargemenot.data.model.CrossPollinationPayload
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.viewmodels.PollenReceivedState
import com.artie.chargemenot.ui.viewmodels.PredictiveImpact
import com.artie.chargemenot.ui.viewmodels.ScannerUiState
import java.time.format.DateTimeFormatter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    uiState: ScannerUiState,
    onScanResult: (OcrScanResult) -> Unit,
    onQrPayloadDetected: (com.artie.chargemenot.data.model.CrossPollinationPayload) -> Unit,
    onCategorySelected: (BillCategory) -> Unit,
    onAcceptPollinatedBill: () -> Unit,
    onDiscardPollen: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Scan Bill") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to dashboard"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (cameraPermissionState.status.isGranted) {
                    CameraPreviewSection(
                        onScanResult = onScanResult,
                        onQrPayloadDetected = onQrPayloadDetected
                    )
                } else {
                    CameraPermissionPlaceholder()
                }

                ScanCaptureBanner(
                    message = uiState.detectionBannerMessage,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                )

                ScanCaptureBanner(
                    message = uiState.scanStatusMessage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
            }

            if (uiState.pollenReceived != null) {
                AcceptPollinatedBillCard(
                    pollen = uiState.pollenReceived,
                    onAccept = onAcceptPollinatedBill,
                    onDiscard = onDiscardPollen,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                PredictiveImpactCard(
                    uiState = uiState,
                    onCategorySelected = onCategorySelected,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewSection(
    onScanResult: (OcrScanResult) -> Unit,
    onQrPayloadDetected: (CrossPollinationPayload) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val currentOnScanResult by rememberUpdatedState(onScanResult)
    val currentOnQrPayloadDetected by rememberUpdatedState(onQrPayloadDetected)
    val analyzer = remember {
        BillOcrAnalyzer(
            onScanResult = { result -> currentOnScanResult(result) },
            onQrPayloadDetected = { payload -> currentOnQrPayloadDetected(payload) }
        )
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(lifecycleOwner, analyzer, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = CameraPreview.Builder()
            .build()
            .also { it.surfaceProvider = previewView.surfaceProvider }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor, analyzer)
            }

        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis
        )

        onDispose {
            cameraProvider.unbindAll()
            analyzer.close()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun CameraPermissionPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Camera permission is required to scan bills.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Composable
private fun ScanCaptureBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.72f))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AcceptPollinatedBillCard(
    pollen: PollenReceivedState,
    onAccept: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val dateFormat = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = MeadowGreen,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.scanner_accept_pollinated_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MeadowGreenDark,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.scanner_accept_pollinated_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MeadowSage.copy(alpha = 0.22f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = pollen.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MeadowGreenDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = categoryDisplayName(pollen.category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(pollen.amount),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Due ${pollen.dueDate.format(dateFormat)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDiscard,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.scanner_discard_pollen))
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeadowGreen,
                        contentColor = MeadowWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFlorist,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.scanner_accept_into_garden))
                }
            }
        }
    }
}

@Composable
private fun PredictiveImpactCard(
    uiState: ScannerUiState,
    onCategorySelected: (BillCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val projectedTotals = remember(uiState.categoryTotals, uiState.selectedCategory, uiState.scannedBill.amount) {
        buildProjectedCategoryTotals(
            categoryTotals = uiState.categoryTotals,
            selectedCategory = uiState.selectedCategory,
            scannedAmount = uiState.scannedBill.amount
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "PREDICTIVE IMPACT ON YOUR BLOOM",
                style = MaterialTheme.typography.labelLarge,
                color = MeadowGreenDark,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FinancialBloomCanvas(
                    categoryTotals = uiState.categoryTotals,
                    projectedCategoryTotals = projectedTotals,
                    highlightedCategory = uiState.selectedCategory,
                    monthlyBudget = uiState.monthlyBudget,
                    sizeByMonthlyBudget = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                uiState.predictiveImpact?.let { impact ->
                    ImpactTooltip(
                        impact = impact,
                        currencyFormat = currencyFormat,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }

            Text(
                text = uiState.budgetSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CategorySelectionRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }
    }
}

@Composable
private fun ImpactTooltip(
    impact: PredictiveImpact,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = categoryColor(impact.category).copy(alpha = 0.92f)
        )
    ) {
        Text(
            text = "${categoryDisplayName(impact.category)}: New Petal Size: " +
                "${"%.1f".format(Locale.US, impact.newPetalSizePercent)}% " +
                "(+${currencyFormat.format(impact.scannedAmount)})",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CategorySelectionRow(
    selectedCategory: BillCategory,
    onCategorySelected: (BillCategory) -> Unit
) {
    val categories = listOf(
        BillCategory.RENT to Icons.Default.Home,
        BillCategory.FOOD to Icons.Default.ShoppingCart,
        BillCategory.UTILITIES to Icons.Default.Bolt
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { (category, icon) ->
            val isSelected = category == selectedCategory
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCategorySelected(category) }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                categoryColor(category).copy(alpha = 0.25f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) categoryColor(category) else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = categoryDisplayName(category),
                        tint = if (isSelected) categoryColor(category) else MeadowGreenDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = categoryDisplayName(category),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MeadowGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun buildProjectedCategoryTotals(
    categoryTotals: Map<BillCategory, Double>,
    selectedCategory: BillCategory,
    scannedAmount: Double?
): Map<BillCategory, Double> {
    if (scannedAmount == null) {
        return categoryTotals
    }

    val projected = categoryTotals.toMutableMap()
    projected[selectedCategory] = (projected[selectedCategory] ?: 0.0) + scannedAmount
    return projected
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ScannerScreenPreview() {
    ChargeMeNotTheme {
        ScannerScreen(
            uiState = ScannerUiState(
                scannedBill = com.artie.chargemenot.ui.viewmodels.ScannedBillData(
                    amount = 94.17,
                    dueDate = LocalDate.of(2026, 9, 12)
                ),
                selectedCategory = BillCategory.UTILITIES,
                categoryTotals = mapOf(
                    BillCategory.RENT to 1_450.0,
                    BillCategory.FOOD to 258.72,
                    BillCategory.UTILITIES to 94.17
                ),
                predictiveImpact = PredictiveImpact(
                    category = BillCategory.UTILITIES,
                    newPetalSizePercent = 7.5,
                    scannedAmount = 94.17,
                    withinBudget = true,
                    totalProjectedSpend = 1_897.06
                ),
                scanStatusMessage = "Scanned Details Captured! Date: Sep 12, 2026, Amount: $94.17",
                monthlyBudget = 3_000.0,
                budgetSummary = "Adding this bill keeps you within your $3,000.00 monthly budget."
            ),
            onScanResult = {},
            onQrPayloadDetected = {},
            onCategorySelected = {},
            onAcceptPollinatedBill = {},
            onDiscardPollen = {},
            onNavigateBack = {}
        )
    }
}
