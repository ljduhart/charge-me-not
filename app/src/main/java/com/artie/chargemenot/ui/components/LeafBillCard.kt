package com.artie.chargemenot.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.Icon
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

private val LeafGlassCream = Color(0xFFF9F9F4)
private val OverdueGlassTint = Color(0x88795548)
private val BudGreen = Color(0xFF4CAF50)
private val BudStem = Color(0xAA81C784)

@Composable
fun leafShapeForIndex(index: Int): RoundedCornerShape {
    return if (index % 2 == 0) {
        RoundedCornerShape(topStart = 4.dp, topEnd = 40.dp, bottomEnd = 4.dp, bottomStart = 40.dp)
    } else {
        RoundedCornerShape(topStart = 40.dp, topEnd = 4.dp, bottomEnd = 40.dp, bottomStart = 4.dp)
    }
}

@Composable
fun LeafBillCard(
    bill: Bill,
    index: Int,
    gardenState: GardenBillState,
    currencyFormat: NumberFormat,
    dateFormat: DateTimeFormatter,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {
        DefaultLeafBillContent(
            bill = bill,
            gardenState = gardenState,
            currencyFormat = currencyFormat,
            dateFormat = dateFormat
        )
    }
) {
    val leafShape = leafShapeForIndex(index)
    val supportsNativeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val glassAlpha = when (gardenState) {
        GardenBillState.Overdue -> 0.72f
        else -> 0.6f
    }
    val glassColor = when (gardenState) {
        GardenBillState.Overdue -> OverdueGlassTint
        else -> LeafGlassCream.copy(alpha = glassAlpha)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        if (gardenState == GardenBillState.Upcoming) {
            StemBud(
                alignToStemOnLeft = index % 2 == 0,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .align(if (index % 2 == 0) Alignment.CenterStart else Alignment.CenterEnd)
                .clip(leafShape)
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.35f), shape = leafShape)
                .then(
                    if (gardenState == GardenBillState.Overdue) {
                        Modifier.drawWithContent {
                            drawContent()
                            drawCrackedWitheredOverlay()
                        }
                    } else {
                        Modifier
                    }
                )
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
                    .background(glassColor)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }

            if (gardenState == GardenBillState.Paid) {
                BloomedFlowerOverlay(
                    anchorOnLeft = index % 2 == 0,
                    modifier = Modifier.align(
                        if (index % 2 == 0) Alignment.CenterStart else Alignment.CenterEnd
                    )
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
                            color = Color.White.copy(alpha = 0.82f),
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
    currencyFormat: NumberFormat,
    dateFormat: DateTimeFormatter
) {
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
            text = stringResource(
                R.string.petals_and_weeds_bill_meta,
                "${bill.parentCategory} · ${bill.subCategory}",
                dateFormat.format(bill.dueDate)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = GlasshouseForestGreen.copy(alpha = 0.78f),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = currencyFormat.format(bill.amount),
            style = MaterialTheme.typography.titleMedium,
            color = GlasshouseForestGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (gardenState == GardenBillState.Paid) {
            Text(
                text = stringResource(R.string.petals_and_weeds_paid_bloom),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp)
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
        modifier = modifier
            .offset(x = if (alignToStemOnLeft) 42.dp else (-42).dp)
            .size(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = 10.dp)
                .background(BudStem)
                .align(if (alignToStemOnLeft) Alignment.CenterEnd else Alignment.CenterStart)
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(BudGreen, shape = RoundedCornerShape(50))
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun BloomedFlowerOverlay(
    anchorOnLeft: Boolean,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Default.LocalFlorist,
        contentDescription = null,
        tint = Color(0xFFE91E63),
        modifier = modifier
            .offset(x = if (anchorOnLeft) (-22).dp else 22.dp)
            .size(44.dp)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCrackedWitheredOverlay() {
    val crackColor = Color(0x553D2817)
    val crackStroke = Stroke(width = 1.4f)

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

    drawCircle(
        color = Color(0x22000000),
        radius = size.minDimension * 0.35f,
        center = Offset(size.width * 0.5f, size.height * 0.5f)
    )
}
