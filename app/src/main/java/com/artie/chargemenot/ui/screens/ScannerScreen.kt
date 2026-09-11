package com.artie.chargemenot.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.scanner.BillOcrAnalyzer
import com.artie.chargemenot.scanner.OcrScanResult
import com.artie.chargemenot.ui.components.FinancialBloomCanvas
import com.artie.chargemenot.ui.components.categoryDisplayName
import com.artie.chargemenot.ui.theme.meadowParentColor
import com.artie.chargemenot.ui.theme.meadowParentIcon
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
import com.artie.chargemenot.util.ImageStorageUtil
import com.artie.chargemenot.ui.viewmodels.PollenReceivedState
import com.artie.chargemenot.ui.viewmodels.PredictiveImpact
import com.artie.chargemenot.ui.viewmodels.ScannerUiState
import java.time.format.DateTimeFormatter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.util.CurrencyFormatter
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    uiState: ScannerUiState,
    onScanResult: (OcrScanResult, String?) -> Unit,
    onQrPayloadDetected: (CrossPollinationPayload) -> Unit,
    onCategorySelected: (String) -> Unit,
    onAcceptPollinatedBill: () -> Unit,
    onDiscardPollen: () -> Unit,
    onSaveScannedBill: () -> Unit,
    onNavigateBack: () -> Unit,
    onEnterBillManually: () -> Unit,
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
                title = { Text(stringResource(R.string.scanner_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.scanner_navigate_back)
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
                        onQrPayloadDetected = onQrPayloadDetected,
                        scanningPaused = uiState.pollenReceived != null
                    )
                    PulsingScanReticle(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    CameraPermissionPlaceholder(
                        onEnterBillManually = onEnterBillManually
                    )
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.pollenReceived != null,
                    modifier = Modifier.fillMaxSize(),
                    enter = slideInVertically(
                        animationSpec = tween(360),
                        initialOffsetY = { fullHeight -> fullHeight }
                    ) + fadeIn(animationSpec = tween(360)),
                    exit = slideOutVertically(
                        animationSpec = tween(280),
                        targetOffsetY = { fullHeight -> fullHeight }
                    ) + fadeOut(animationSpec = tween(280))
                ) {
                    uiState.pollenReceived?.let { pollen ->
                        AcceptPollinatedBillCard(
                            pollen = pollen,
                            currency = uiState.selectedCurrency,
                            onAccept = onAcceptPollinatedBill,
                            onDiscard = onDiscardPollen,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.pollenReceived == null,
                    modifier = Modifier.fillMaxSize(),
                    enter = slideInVertically(
                        animationSpec = tween(360),
                        initialOffsetY = { fullHeight -> fullHeight }
                    ) + fadeIn(animationSpec = tween(360)),
                    exit = slideOutVertically(
                        animationSpec = tween(280),
                        targetOffsetY = { fullHeight -> fullHeight }
                    ) + fadeOut(animationSpec = tween(280))
                ) {
                    PredictiveImpactCard(
                        uiState = uiState,
                        onCategorySelected = onCategorySelected,
                        onSaveScannedBill = onSaveScannedBill,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun PulsingScanReticle(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanReticlePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanReticleScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanReticleAlpha"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .scale(pulseScale)
            .alpha(pulseAlpha)
            .border(
                width = 3.dp,
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
    )
}

@Composable
private fun CameraPreviewSection(
    onScanResult: (OcrScanResult, String?) -> Unit,
    onQrPayloadDetected: (CrossPollinationPayload) -> Unit,
    scanningPaused: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val currentOnScanResult by rememberUpdatedState(onScanResult)
    val currentOnQrPayloadDetected by rememberUpdatedState(onQrPayloadDetected)
    val scanningPausedState = rememberUpdatedState(scanningPaused)
    val analyzer = remember {
        BillOcrAnalyzer(
            onScanResult = { result ->
                if (!scanningPausedState.value) {
                    val receiptImagePath = result.receiptBitmap?.let { bitmap ->
                        val filename = "receipt_${System.currentTimeMillis()}.jpg"
                        ImageStorageUtil.saveBitmapToInternalStorage(
                            context = context,
                            bitmap = bitmap,
                            filename = filename
                        ).also { bitmap.recycle() }
                    }
                    currentOnScanResult(result, receiptImagePath)
                } else {
                    result.receiptBitmap?.recycle()
                }
            },
            onQrPayloadDetected = { payload ->
                if (!scanningPausedState.value) {
                    currentOnQrPayloadDetected(payload)
                }
            }
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
private fun CameraPermissionPlaceholder(
    onEnterBillManually: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.scanner_camera_permission_required),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onEnterBillManually,
            colors = ButtonDefaults.buttonColors(
                containerColor = MeadowSage,
                contentColor = MeadowGreenDark
            )
        ) {
            Text(stringResource(R.string.scanner_enter_manually))
        }
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
    currency: SupportedCurrency,
    onAccept: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                        text = categoryDisplayName(pollen.parentCategory, pollen.subCategory),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(pollen.amount, currency),
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
    onCategorySelected: (String) -> Unit,
    onSaveScannedBill: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projectedTotals = remember(
        uiState.parentCategoryTotals,
        uiState.selectedParentCategory,
        uiState.scannedBill.amount
    ) {
        buildProjectedParentCategoryTotals(
            parentCategoryTotals = uiState.parentCategoryTotals,
            selectedParentCategory = uiState.selectedParentCategory,
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
                    parentCategoryTotals = uiState.parentCategoryTotals,
                    projectedParentCategoryTotals = projectedTotals,
                    highlightedParent = uiState.selectedParentCategory,
                    monthlyBudget = uiState.monthlyBudget,
                    sizeByMonthlyBudget = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                uiState.predictiveImpact?.let { impact ->
                    ImpactTooltip(
                        impact = impact,
                        currency = uiState.selectedCurrency,
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
                selectedParentCategory = uiState.selectedParentCategory,
                onCategorySelected = onCategorySelected
            )

            if (uiState.canSaveScannedBill) {
                Button(
                    onClick = onSaveScannedBill,
                    enabled = !uiState.isSavingScannedBill,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeadowGreen,
                        contentColor = MeadowWhite
                    )
                ) {
                    Text(stringResource(R.string.scanner_save_into_garden))
                }
            }
        }
    }
}

@Composable
private fun ImpactTooltip(
    impact: PredictiveImpact,
    currency: SupportedCurrency,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = meadowParentColor(impact.parentCategory).copy(alpha = 0.92f)
        )
    ) {
        Text(
            text = "${MeadowCategories.shortDisplayName(impact.parentCategory)}: New Petal Size: " +
                "${"%.1f".format(Locale.US, impact.newPetalSizePercent)}% " +
                "(+${CurrencyFormatter.format(impact.scannedAmount, currency)})",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CategorySelectionRow(
    selectedParentCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        MeadowCategories.CANOPY,
        MeadowCategories.FERTILIZER,
        MeadowCategories.ROOT_SYSTEM
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { parentCategory ->
            val isSelected = parentCategory == selectedParentCategory
            val tint = meadowParentColor(parentCategory)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCategorySelected(parentCategory) }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                tint.copy(alpha = 0.25f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) tint else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = meadowParentIcon(parentCategory),
                        contentDescription = MeadowCategories.shortDisplayName(parentCategory),
                        tint = if (isSelected) tint else MeadowGreenDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = MeadowCategories.shortDisplayName(parentCategory),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MeadowGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun buildProjectedParentCategoryTotals(
    parentCategoryTotals: Map<String, Long>,
    selectedParentCategory: String,
    scannedAmount: Long?
): Map<String, Long> {
    if (scannedAmount == null) {
        return parentCategoryTotals
    }

    val projected = parentCategoryTotals.toMutableMap()
    projected[selectedParentCategory] = (projected[selectedParentCategory] ?: 0L) + scannedAmount
    return projected
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ScannerScreenPreview() {
    ChargeMeNotTheme {
        ScannerScreen(
            uiState = ScannerUiState(
                scannedBill = com.artie.chargemenot.ui.viewmodels.ScannedBillData(
                    amount = 9_417L,
                    dueDate = LocalDate.of(2026, 9, 12)
                ),
                selectedParentCategory = MeadowCategories.ROOT_SYSTEM,
                parentCategoryTotals = mapOf(
                    MeadowCategories.CANOPY to 145_000L,
                    MeadowCategories.FERTILIZER to 25_872L,
                    MeadowCategories.ROOT_SYSTEM to 9_417L
                ),
                predictiveImpact = PredictiveImpact(
                    parentCategory = MeadowCategories.ROOT_SYSTEM,
                    newPetalSizePercent = 7.5,
                    scannedAmount = 9_417L,
                    withinBudget = true,
                    totalProjectedSpend = 189_706L
                ),
                scanStatusMessage = "Scanned Details Captured! Date: Sep 12, 2026, Amount: 94.17",
                monthlyBudget = 300_000L,
                budgetSummary = "Adding this bill keeps you within your monthly budget."
            ),
            onScanResult = { _, _ -> },
            onQrPayloadDetected = {},
            onCategorySelected = {},
            onAcceptPollinatedBill = {},
            onDiscardPollen = {},
            onSaveScannedBill = {},
            onNavigateBack = {},
            onEnterBillManually = {}
        )
    }
}
