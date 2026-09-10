package com.artie.chargemenot.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.ui.theme.WeedRed
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class GardenBillState(val timelineOrder: Int) {
    Overdue(0),
    Upcoming(1),
    Paid(2)
}

fun resolveGardenBillState(bill: Bill, today: LocalDate = LocalDate.now()): GardenBillState {
    return when {
        bill.isPaid -> GardenBillState.Paid
        bill.dueDate.isBefore(today) -> GardenBillState.Overdue
        else -> GardenBillState.Upcoming
    }
}

fun sortGardenPathBills(bills: List<Bill>, today: LocalDate = LocalDate.now()): List<Bill> {
    return bills.sortedWith(
        compareBy<Bill> { bill -> resolveGardenBillState(bill, today).timelineOrder }
            .thenBy { bill -> bill.dueDate }
    )
}

private val LeafGlassCream = Color(0xFFF9F9F4)
private val WitheredLeafTint = Color(0x99BCAAA4)
private val WitheredBorderDark = Color(0xFF5D4037)
private val WitheredBorderLight = Color(0xFF8D6E63)
private val BudGreen = Color(0xFF4CAF50)
private val BudStem = Color(0xAA81C784)
private val VineDateLabel = Color(0xFFF5F5F5)
private val CategoryChipBackground = Color(0xFFF9F9F4).copy(alpha = 0.45f)

@Composable
fun leafShapeForIndex(index: Int): RoundedCornerShape {
    return if (index % 2 == 0) {
        RoundedCornerShape(topStart = 4.dp, topEnd = 40.dp, bottomEnd = 4.dp, bottomStart = 40.dp)
    } else {
        RoundedCornerShape(topStart = 40.dp, topEnd = 4.dp, bottomEnd = 40.dp, bottomStart = 4.dp)
    }
}

@Composable
fun GardenPathDateMarker(
    dueDate: LocalDate,
    modifier: Modifier = Modifier
) {
    val dateLabel = DateTimeFormatter.ofPattern("MMM d", Locale.US).format(dueDate)
    Text(
        text = dateLabel,
        color = VineDateLabel,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
fun LeafBillCard(
    bill: Bill,
    index: Int,
    gardenState: GardenBillState,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {
        DefaultLeafBillContent(
            bill = bill,
            gardenState = gardenState,
            currencyFormat = currencyFormat
        )
    }
) {
    val leafShape = leafShapeForIndex(index)
    val supportsNativeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isLeftLeaf = index % 2 == 0
    val glassColor = when (gardenState) {
        GardenBillState.Overdue -> WitheredLeafTint
        else -> LeafGlassCream.copy(alpha = 0.6f)
    }
    val borderModifier = when (gardenState) {
        GardenBillState.Overdue -> Modifier
            .border(width = 2.dp, color = WitheredBorderDark, shape = leafShape)
            .border(width = 1.dp, color = WitheredBorderLight.copy(alpha = 0.75f), shape = leafShape)
        else -> Modifier.border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.35f),
            shape = leafShape
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        if (gardenState == GardenBillState.Upcoming) {
            StemBud(
                alignToStemOnLeft = isLeftLeaf,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .align(if (isLeftLeaf) Alignment.CenterStart else Alignment.CenterEnd)
                .clip(leafShape)
                .then(borderModifier)
                .then(
                    if (gardenState == GardenBillState.Overdue) {
                        Modifier.drawWithContent {
                            drawContent()
                            drawCrackedWitheredOverlay()
                            drawRuggedLeafEdge()
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            if (gardenState != GardenBillState.Overdue) {
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
                        .background(glassColor)
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(glassColor)
                )
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }

            if (gardenState == GardenBillState.Paid) {
                BloomedRoseAnchor(
                    modifier = Modifier
                        .align(if (isLeftLeaf) Alignment.CenterStart else Alignment.CenterEnd)
                        .offset(x = if (isLeftLeaf) (-30).dp else 30.dp)
                )
            }

            if (gardenState == GardenBillState.Overdue) {
                Text(
                    text = stringResource(R.string.petals_and_weeds_overdue),
                    color = WeedRed,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(
                            color = Color(0xFF3E2723).copy(alpha = 0.82f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DefaultLeafBillContent(
    bill: Bill,
    gardenState: GardenBillState,
    currencyFormat: NumberFormat
) {
    val statusDateFormat = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    val categoryLabel = BloomCategoryDefinitions.fromParentName(bill.parentCategory)?.displayName
        ?: bill.subCategory

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = bill.name,
            style = MaterialTheme.typography.titleMedium,
            color = GlasshouseForestGreen,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = when (gardenState) {
                GardenBillState.Paid -> stringResource(
                    R.string.petals_and_weeds_status_paid,
                    statusDateFormat.format(bill.dueDate)
                )
                GardenBillState.Overdue -> stringResource(
                    R.string.petals_and_weeds_status_overdue,
                    statusDateFormat.format(bill.dueDate)
                )
                GardenBillState.Upcoming -> stringResource(
                    R.string.petals_and_weeds_status_upcoming,
                    statusDateFormat.format(bill.dueDate)
                )
            },
            style = MaterialTheme.typography.bodyMedium,
            color = GlasshouseForestGreen.copy(alpha = 0.82f),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = currencyFormat.format(bill.amount),
            style = MaterialTheme.typography.titleLarge,
            color = GlasshouseForestGreen,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .background(CategoryChipBackground, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = GlasshouseForestGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StemBud(
    alignToStemOnLeft: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = 28.dp, height = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = 14.dp)
                .background(BudStem)
                .align(if (alignToStemOnLeft) Alignment.CenterEnd else Alignment.CenterStart)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(BudGreen, shape = RoundedCornerShape(50))
                .align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = 8.dp)
                .offset(x = if (alignToStemOnLeft) 6.dp else (-6).dp)
                .background(BudStem.copy(alpha = 0.7f))
                .align(if (alignToStemOnLeft) Alignment.CenterStart else Alignment.CenterEnd)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCrackedWitheredOverlay() {
    val crackColor = Color(0x664E342E)
    val crackStroke = Stroke(width = 1.5f)

    val cracks = listOf(
        Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.12f)
            lineTo(size.width * 0.34f, size.height * 0.42f)
            lineTo(size.width * 0.28f, size.height * 0.78f)
        },
        Path().apply {
            moveTo(size.width * 0.72f, size.height * 0.18f)
            lineTo(size.width * 0.58f, size.height * 0.52f)
            lineTo(size.width * 0.66f, size.height * 0.86f)
        },
        Path().apply {
            moveTo(size.width * 0.48f, size.height * 0.08f)
            lineTo(size.width * 0.44f, size.height * 0.55f)
            lineTo(size.width * 0.50f, size.height * 0.92f)
        }
    )

    cracks.forEach { path ->
        drawPath(path = path, color = crackColor, style = crackStroke)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRuggedLeafEdge() {
    val edgeColor = Color(0x885D4037)
    val edgeStroke = Stroke(width = 1.2f)
    val inset = 4f

    val ruggedPath = Path().apply {
        moveTo(inset, size.height * 0.2f)
        lineTo(inset + 6f, size.height * 0.35f)
        lineTo(inset, size.height * 0.5f)
        lineTo(inset + 5f, size.height * 0.68f)
        lineTo(inset, size.height * 0.85f)
    }
    drawPath(path = ruggedPath, color = edgeColor, style = edgeStroke)

    val rightPath = Path().apply {
        moveTo(size.width - inset, size.height * 0.18f)
        lineTo(size.width - inset - 5f, size.height * 0.38f)
        lineTo(size.width - inset, size.height * 0.55f)
        lineTo(size.width - inset - 6f, size.height * 0.72f)
        lineTo(size.width - inset, size.height * 0.88f)
    }
    drawPath(path = rightPath, color = edgeColor, style = edgeStroke)
}
