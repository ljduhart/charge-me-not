package com.artie.chargemenot.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.MeadowCategories
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

const val GARDEN_PATH_FAR_OFF_DAYS = 7L

enum class GardenBillState {
    Paid,
    Overdue,
    Upcoming,
    FarOff
}

fun resolveGardenBillState(bill: Bill, today: LocalDate = LocalDate.now()): GardenBillState {
    return when {
        bill.isPaid -> GardenBillState.Paid
        bill.dueDate.isBefore(today) -> GardenBillState.Overdue
        ChronoUnit.DAYS.between(today, bill.dueDate) > GARDEN_PATH_FAR_OFF_DAYS -> GardenBillState.FarOff
        else -> GardenBillState.Upcoming
    }
}

fun sortGardenPathBills(bills: List<Bill>): List<Bill> {
    return bills.sortedBy { bill -> bill.dueDate }
}

private val LeafGlassCream = Color(0xFFF9F9F4)
private val WitheredLeafTint = Color(0x99BCAAA4)
private val WitheredBorderDark = Color(0xFF5D4037)
private val WitheredBorderLight = Color(0xFF8D6E63)
private val BudGreen = Color(0xFF4CAF50)
private val BudStem = Color(0xAA81C784)
private val VineDateLabel = Color(0xFFF5F5F5)
private val CategoryChipBackground = Color(0xFFF9F9F4).copy(alpha = 0.45f)
private val OverdueStampBrown = Color(0xCCBF360C)

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
fun GardenPathStemConnector(
    gardenState: GardenBillState,
    isLeftLeaf: Boolean,
    modifier: Modifier = Modifier
) {
    when (gardenState) {
        GardenBillState.Upcoming -> StemBud(
            isSmall = false,
            alignToStemOnLeft = isLeftLeaf,
            modifier = modifier
        )
        GardenBillState.FarOff -> StemBud(
            isSmall = true,
            alignToStemOnLeft = isLeftLeaf,
            modifier = modifier
        )
        else -> Unit
    }
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

    Box(
        modifier = modifier
            .fillMaxWidth()
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

        if (gardenState == GardenBillState.Overdue) {
            Text(
                text = stringResource(R.string.petals_and_weeds_overdue),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer { rotationZ = -16f }
                    .background(
                        color = OverdueStampBrown,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
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
    val categoryIcon = gardenCategoryIcon(bill.parentCategory)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
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
                    GardenBillState.FarOff -> stringResource(
                        R.string.petals_and_weeds_status_far_off,
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
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(CategoryChipBackground, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = categoryLabel,
                tint = GlasshouseForestGreen,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = GlasshouseForestGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun gardenCategoryIcon(parentCategory: String): ImageVector {
    return when (parentCategory) {
        MeadowCategories.CANOPY -> Icons.Default.Park
        MeadowCategories.FERTILIZER -> Icons.Default.Eco
        MeadowCategories.ROOT_SYSTEM -> Icons.Default.WaterDrop
        MeadowCategories.VINES -> Icons.Default.Grass
        MeadowCategories.POLLINATORS -> Icons.Default.LocalFlorist
        MeadowCategories.WILDFLOWERS -> Icons.Default.FilterVintage
        else -> Icons.Default.Spa
    }
}

@Composable
private fun StemBud(
    isSmall: Boolean,
    alignToStemOnLeft: Boolean,
    modifier: Modifier = Modifier
) {
    val budSize = if (isSmall) 8.dp else 12.dp
    val stemHeight = if (isSmall) 10.dp else 14.dp

    Box(
        modifier = modifier.size(width = 24.dp, height = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = stemHeight)
                .background(BudStem)
                .align(if (alignToStemOnLeft) Alignment.CenterEnd else Alignment.CenterStart)
        )
        Box(
            modifier = Modifier
                .size(budSize)
                .background(BudGreen, shape = RoundedCornerShape(50))
                .align(Alignment.Center)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCrackedWitheredOverlay() {
    val crackColor = Color(0x664E342E)
    val crackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)

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
    val edgeStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
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
