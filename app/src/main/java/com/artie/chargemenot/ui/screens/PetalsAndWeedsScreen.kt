package com.artie.chargemenot.ui.screens

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.ui.components.GardenBillState
import com.artie.chargemenot.ui.components.GardenPathDateMarker
import com.artie.chargemenot.ui.components.GardenPathStemConnector
import com.artie.chargemenot.ui.components.GlasshouseForestGreen
import com.artie.chargemenot.ui.components.LeafBillCard
import com.artie.chargemenot.ui.components.MeadowHubScaffold
import com.artie.chargemenot.ui.components.decorativeVineBorder
import com.artie.chargemenot.ui.components.dismissShapeForGardenState
import com.artie.chargemenot.ui.components.gardenPathVineBackground
import com.artie.chargemenot.ui.components.resolveGardenBillState
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.theme.WeedRed
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PetalsAndWeedsScreen(
    gardenBills: List<Bill>,
    parallaxOffset: Pair<Float, Float>,
    onStartParallaxSensor: () -> Unit,
    onStopParallaxSensor: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onSelectBillForEdit: (Bill) -> Unit,
    onShowManualBillEntry: () -> Unit,
    onDeleteBill: (Bill) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = rememberCurrencyFormat()
    val featuredPaidBillId = remember(gardenBills) {
        gardenBills.firstOrNull { bill ->
            resolveGardenBillState(bill) == GardenBillState.Paid
        }?.id
    }
    var billPendingDelete by remember { mutableStateOf<Bill?>(null) }
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val estimatedItemHeightPx = remember(density) { with(density) { 168.dp.toPx() } }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, onStartParallaxSensor, onStopParallaxSensor) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onStartParallaxSensor()
                Lifecycle.Event.ON_STOP -> onStopParallaxSensor()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            onStartParallaxSensor()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onStopParallaxSensor()
        }
    }

    val (tiltX, tiltY) = parallaxOffset
    val parallaxDistancePx = with(density) { 12.dp.toPx() }
    val backgroundTranslationX = tiltX * 0.5f * parallaxDistancePx
    val backgroundTranslationY = tiltY * 0.5f * parallaxDistancePx

    billPendingDelete?.let { bill ->
        AlertDialog(
            onDismissRequest = { billPendingDelete = null },
            title = {
                Text(text = stringResource(R.string.petals_and_weeds_delete_title))
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.petals_and_weeds_delete_message,
                        bill.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBill(bill)
                        billPendingDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.petals_and_weeds_delete_confirm),
                        color = WeedRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { billPendingDelete = null }) {
                    Text(text = stringResource(R.string.petals_and_weeds_delete_cancel))
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_greenhouse),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = backgroundTranslationX
                    translationY = backgroundTranslationY
                    scaleX = 1.10f
                    scaleY = 1.10f
                }
        )

        MeadowHubScaffold(
            title = stringResource(R.string.meadow_route_petals_and_weeds),
            onOpenDrawer = onOpenDrawer,
            onNavigateBack = onNavigateBack,
            containerColor = Color.Transparent,
            topBarContainerColor = Color.Transparent,
            titleContentColor = MeadowWhite,
            navigationIconContentColor = MeadowWhite,
            floatingActionButton = {
                GardenPathAddBillFab(onClick = onShowManualBillEntry)
            }
        ) { innerPadding ->
            if (gardenBills.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.petals_and_weeds_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MeadowWhite,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.petals_and_weeds_header),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MeadowWhite.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.petals_and_weeds_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MeadowWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .gardenPathVineBackground(
                            listState = listState,
                            itemCount = gardenBills.size + 1,
                            estimatedItemHeightPx = estimatedItemHeightPx
                        ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "garden_path_title") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.petals_and_weeds_title),
                                color = MeadowWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.petals_and_weeds_header),
                                color = MeadowWhite.copy(alpha = 0.88f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.6.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    itemsIndexed(
                        items = gardenBills,
                        key = { _, bill -> "petal_bill_${bill.id}" }
                    ) { index, bill ->
                        GardenPathTimelineRow(
                            bill = bill,
                            index = index,
                            currencyFormat = currencyFormat,
                            isFeaturedPaidRose = bill.id == featuredPaidBillId,
                            onSelectBillForEdit = onSelectBillForEdit,
                            onRequestDelete = { billPendingDelete = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GardenPathTimelineRow(
    bill: Bill,
    index: Int,
    currencyFormat: NumberFormat,
    isFeaturedPaidRose: Boolean,
    onSelectBillForEdit: (Bill) -> Unit,
    onRequestDelete: (Bill) -> Unit
) {
    val gardenState = resolveGardenBillState(bill)
    val isLeftLeaf = index % 2 == 0
    val isPaid = gardenState == GardenBillState.Paid
    val isOverdue = gardenState == GardenBillState.Overdue
    val rowMinHeight = when {
        isFeaturedPaidRose -> 168.dp
        isPaid -> 148.dp
        isOverdue -> 140.dp
        else -> 128.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = rowMinHeight)
    ) {
        GardenPathDateMarker(
            dueDate = bill.dueDate,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLeftLeaf) {
                Box(
                    modifier = Modifier
                        .weight(0.42f)
                        .padding(end = 4.dp)
                ) {
                    SwipeableGardenPathBillCard(
                        bill = bill,
                        index = index,
                        gardenState = gardenState,
                        currencyFormat = currencyFormat,
                        isFeaturedPaidRose = isFeaturedPaidRose,
                        onSelectBillForEdit = onSelectBillForEdit,
                        onRequestDelete = onRequestDelete
                    )
                }
                Box(
                    modifier = Modifier.width(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isPaid) {
                        GardenPathStemConnector(
                            gardenState = gardenState,
                            isLeftLeaf = true,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Box(modifier = Modifier.weight(0.42f))
            } else {
                Box(modifier = Modifier.weight(0.42f))
                Box(
                    modifier = Modifier.width(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isPaid) {
                        GardenPathStemConnector(
                            gardenState = gardenState,
                            isLeftLeaf = false,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(0.42f)
                        .padding(start = 4.dp)
                ) {
                    SwipeableGardenPathBillCard(
                        bill = bill,
                        index = index,
                        gardenState = gardenState,
                        currencyFormat = currencyFormat,
                        isFeaturedPaidRose = isFeaturedPaidRose,
                        onSelectBillForEdit = onSelectBillForEdit,
                        onRequestDelete = onRequestDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun GardenPathAddBillFab(
    onClick: () -> Unit
) {
    val supportsNativeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val pillShape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .clip(pillShape)
            .clickable(onClick = onClick)
            .decorativeVineBorder()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (supportsNativeBlur) {
                        Modifier.blur(
                            radius = 16.dp,
                            edgeTreatment = BlurredEdgeTreatment.Rectangle
                        )
                    } else {
                        Modifier
                    }
                )
                .background(Color(0xFFF9F9F4).copy(alpha = 0.6f), pillShape)
        )

        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.dashboard_manual_bill_entry),
                tint = GlasshouseForestGreen
            )
            Text(
                text = stringResource(R.string.dashboard_manual_bill_entry),
                color = GlasshouseForestGreen,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableGardenPathBillCard(
    bill: Bill,
    index: Int,
    gardenState: GardenBillState,
    currencyFormat: NumberFormat,
    isFeaturedPaidRose: Boolean,
    onSelectBillForEdit: (Bill) -> Unit,
    onRequestDelete: (Bill) -> Unit
) {
    val dismissShape = dismissShapeForGardenState(gardenState, index)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { direction ->
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                onRequestDelete(bill)
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = WeedRed.copy(alpha = 0.88f),
                        shape = dismissShape
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.petals_and_weeds_swipe_delete),
                    tint = MeadowWhite
                )
            }
        }
    ) {
        LeafBillCard(
            bill = bill,
            index = index,
            gardenState = gardenState,
            currencyFormat = currencyFormat,
            isFeaturedPaidRose = isFeaturedPaidRose,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectBillForEdit(bill) }
        )
    }
}

@Composable
private fun rememberCurrencyFormat(): NumberFormat {
    return androidx.compose.runtime.remember {
        NumberFormat.getCurrencyInstance(Locale.US)
    }
}
