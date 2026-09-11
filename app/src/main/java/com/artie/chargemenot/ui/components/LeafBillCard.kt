package com.artie.chargemenot.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

private val GardenForestGreen = Color(0xFF1B3B22)
private val LeafGlassFill = Color.White.copy(alpha = 0.35f)
private val LeafCyanEdge = Color(0xB380DEEA)
private val BudGreen = Color(0xFF4CAF50)
private val BudStem = Color(0xAA81C784)
private val BudCream = Color(0xFFFFF8E1)
private val BudGold = Color(0xFFFFE082)
private val VineDateLabel = Color(0xFFF5F5F5)
private val CategoryChipBackground = Color.White.copy(alpha = 0.35f)
private val OverdueStampBrown = Color(0xCCBF360C)

@Composable
fun leafShapeForIndex(index: Int): RoundedCornerShape {
    return if (index % 2 == 0) {
        RoundedCornerShape(topStart = 0.dp, topEnd = 48.dp, bottomEnd = 0.dp, bottomStart = 48.dp)
    } else {
        RoundedCornerShape(topStart = 48.dp, topEnd = 0.dp, bottomEnd = 48.dp, bottomStart = 0.dp)
    }
}

@Composable
fun dismissShapeForGardenState(gardenState: GardenBillState, index: Int): RoundedCornerShape {
    return when (gardenState) {
        GardenBillState.Paid,
        GardenBillState.Overdue -> RoundedCornerShape(16.dp)
        else -> leafShapeForIndex(index)
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
    OrganicStemBud(
        isSmall = gardenState != GardenBillState.Upcoming,
        alignToStemOnLeft = isLeftLeaf,
        showBud = gardenState != GardenBillState.Paid,
        modifier = modifier
    )
}

@Composable
fun LeafBillCard(
    bill: Bill,
    index: Int,
    gardenState: GardenBillState,
    currencyFormat: NumberFormat,
    isFeaturedPaidRose: Boolean = false,
    modifier: Modifier = Modifier
) {
    when (gardenState) {
        GardenBillState.Paid -> PaidRoseBillCard(
            billName = bill.name,
            isFeatured = isFeaturedPaidRose,
            modifier = modifier
        )
        GardenBillState.Overdue -> OverdueLeafBillCard(
            bill = bill,
            currencyFormat = currencyFormat,
            modifier = modifier
        )
        GardenBillState.Upcoming,
        GardenBillState.FarOff -> GlassLeafBillCard(
            bill = bill,
            index = index,
            gardenState = gardenState,
            currencyFormat = currencyFormat,
            modifier = modifier
        )
    }
}

@Composable
private fun PaidRoseBillCard(
    billName: String,
    isFeatured: Boolean,
    modifier: Modifier = Modifier
) {
    val roseHeight = if (isFeatured) 148.dp else 120.dp
    val stampFontSize = if (isFeatured) 12.sp else 10.sp
    val paidBloomDescription = stringResource(R.string.petals_and_weeds_paid_bloom)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(roseHeight)
            .gardenLeafCyanGlow(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_paid_rose),
            contentDescription = "$paidBloomDescription: $billName",
            modifier = Modifier
                .fillMaxWidth()
                .height(roseHeight),
            contentScale = ContentScale.Fit
        )

        Box(
            modifier = Modifier
                .background(
                    color = Color.White.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 12.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.petals_and_weeds_paid_stamp),
                color = GardenForestGreen,
                fontSize = stampFontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun OverdueLeafBillCard(
    bill: Bill,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    val statusDateFormat = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
            .gardenLeafCyanGlow(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_overdue_leaf),
            contentDescription = stringResource(
                R.string.petals_and_weeds_overdue
            ) + ": ${bill.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = bill.name,
                style = MaterialTheme.typography.titleMedium,
                color = GardenForestGreen,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(
                    R.string.petals_and_weeds_status_overdue,
                    statusDateFormat.format(bill.dueDate)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = GardenForestGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = currencyFormat.format(bill.amount),
                style = MaterialTheme.typography.titleLarge,
                color = GardenForestGreen,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(
            text = stringResource(R.string.petals_and_weeds_overdue),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .background(
                    color = OverdueStampBrown,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 14.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun GlassLeafBillCard(
    bill: Bill,
    index: Int,
    gardenState: GardenBillState,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    val leafShape = leafShapeForIndex(index)
    val supportsNativeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier = modifier
            .fillMaxWidth()
            .gardenLeafCyanGlow()
            .clip(leafShape)
            .border(
                width = 1.4.dp,
                color = LeafCyanEdge,
                shape = leafShape
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = leafShape
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (supportsNativeBlur) {
                        Modifier.blur(
                            radius = 20.dp,
                            edgeTreatment = BlurredEdgeTreatment.Rectangle
                        )
                    } else {
                        Modifier
                    }
                )
                .background(LeafGlassFill)
        )

        DefaultLeafBillContent(
            bill = bill,
            gardenState = gardenState,
            currencyFormat = currencyFormat
        )
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
                color = GardenForestGreen,
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
                color = GardenForestGreen,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = currencyFormat.format(bill.amount),
                style = MaterialTheme.typography.titleLarge,
                color = GardenForestGreen,
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
                tint = GardenForestGreen,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = GardenForestGreen,
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
private fun OrganicStemBud(
    isSmall: Boolean,
    alignToStemOnLeft: Boolean,
    modifier: Modifier = Modifier,
    showBud: Boolean = true
) {
    val budRadius = if (isSmall) 5.dp else 7.dp

    Canvas(
        modifier = modifier.size(width = 34.dp, height = 28.dp)
    ) {
        val stemEndX = if (alignToStemOnLeft) size.width * 0.88f else size.width * 0.12f
        val stemStartX = if (alignToStemOnLeft) size.width * 0.12f else size.width * 0.88f
        val centerY = size.height * 0.58f

        val stemPath = Path().apply {
            moveTo(stemStartX, centerY)
            cubicTo(
                x1 = (stemStartX + stemEndX) * 0.5f,
                y1 = centerY - size.height * 0.22f,
                x2 = (stemStartX + stemEndX) * 0.5f,
                y2 = centerY + size.height * 0.16f,
                x3 = stemEndX,
                y3 = centerY
            )
        }

        drawPath(
            path = stemPath,
            color = Color(0x664DD0E1),
            style = Stroke(width = 5.5f, cap = StrokeCap.Round)
        )
        drawPath(
            path = stemPath,
            color = BudStem,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        if (!showBud) {
            return@Canvas
        }

        val budCenter = Offset(size.width / 2f, size.height * 0.42f)
        val radius = budRadius.toPx()

        rotate(degrees = if (alignToStemOnLeft) -18f else 18f, pivot = budCenter) {
            drawOval(
                color = BudGreen,
                topLeft = Offset(budCenter.x - radius * 0.7f, budCenter.y + radius * 0.15f),
                size = androidx.compose.ui.geometry.Size(radius * 0.55f, radius * 0.9f)
            )
            drawOval(
                color = BudGreen,
                topLeft = Offset(budCenter.x + radius * 0.15f, budCenter.y + radius * 0.15f),
                size = androidx.compose.ui.geometry.Size(radius * 0.55f, radius * 0.9f)
            )
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(BudGold, BudCream, Color(0xFFC8E6C9))
                ),
                topLeft = Offset(budCenter.x - radius * 0.72f, budCenter.y - radius * 1.15f),
                size = androidx.compose.ui.geometry.Size(radius * 1.44f, radius * 1.85f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = radius * 0.28f,
                center = Offset(budCenter.x - radius * 0.18f, budCenter.y - radius * 0.45f)
            )
        }
    }
}
