package com.artie.chargemenot.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.util.CurrencyFormatter
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

enum class GardenLeafVisual {
    FeaturedRose,
    GlassLeaf,
    DriedOverdueLeaf
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
    return bills.sortedWith(
        compareBy<Bill> { bill -> bill.dueDate }.thenBy { bill -> bill.id }
    )
}

fun featuredPaidBillIdFrom(
    bills: List<Bill>,
    today: LocalDate = LocalDate.now()
): Long? {
    return sortGardenPathBills(bills).firstOrNull { bill ->
        resolveGardenBillState(bill, today) == GardenBillState.Paid
    }?.id
}

fun gardenLeafVisualFor(
    gardenState: GardenBillState,
    isFeaturedPaidRose: Boolean
): GardenLeafVisual {
    return when {
        gardenState == GardenBillState.Paid && isFeaturedPaidRose -> GardenLeafVisual.FeaturedRose
        gardenState == GardenBillState.Overdue -> GardenLeafVisual.DriedOverdueLeaf
        else -> GardenLeafVisual.GlassLeaf
    }
}

fun leafTipsLeftForIndex(index: Int): Boolean = index % 2 == 0

private val GardenForestGreen = Color(0xFF1B3B22)
private val DriedLeafInk = Color(0xFF5D3310)
internal val GardenGlassFill = Color.White.copy(alpha = 0.25f)
internal val GardenGlassBlurRadius = 24.dp
private val SageLeafTint = Color(0x55C5E1A5)
private val GlassLeafEdge = Color.White.copy(alpha = 0.45f)
private val DriedLeafHighlight = Color(0xFFD9B77A)
private val DriedLeafMid = Color(0xFFC19655)
private val DriedLeafShadow = Color(0xFF8D6234)
private val DriedLeafEdge = Color(0x99A06B3A)
private val BudCream = Color(0xFFFFF8E1)
private val BudGold = Color(0xFFFFE082)
private val VineDateLabel = Color(0xFFF5F5F5)
private val CategoryChipBackground = Color.White.copy(alpha = 0.35f)
private val PaidRoseSize = 120.dp
private val FrostedPillFill = Color.White.copy(alpha = 0.45f)
private val OverdueLeafHeight = 136.dp
private val GardenDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d", Locale.US)

data class LeafCornerPercents(
    val topStartPercent: Int,
    val topEndPercent: Int,
    val bottomEndPercent: Int,
    val bottomStartPercent: Int
)

data class OrganicLeafAnchors(
    val stemXFraction: Float,
    val tipXFraction: Float
)

fun leafCornerPercentsForIndex(index: Int): LeafCornerPercents {
    return if (leafTipsLeftForIndex(index)) {
        LeafCornerPercents(
            topStartPercent = 0,
            topEndPercent = 100,
            bottomEndPercent = 0,
            bottomStartPercent = 100
        )
    } else {
        LeafCornerPercents(
            topStartPercent = 100,
            topEndPercent = 0,
            bottomEndPercent = 100,
            bottomStartPercent = 0
        )
    }
}

fun organicLeafAnchors(tipOnLeft: Boolean): OrganicLeafAnchors {
    return if (tipOnLeft) {
        OrganicLeafAnchors(stemXFraction = 0.96f, tipXFraction = 0.04f)
    } else {
        OrganicLeafAnchors(stemXFraction = 0.04f, tipXFraction = 0.96f)
    }
}

fun organicLeafShapeForIndex(index: Int): OrganicLeafShape {
    return OrganicLeafShape(tipOnLeft = leafTipsLeftForIndex(index))
}

fun leafShapeForIndex(index: Int): Shape {
    return organicLeafShapeForIndex(index)
}

fun organicLeafContentPadding(tipOnLeft: Boolean): PaddingValues {
    return if (tipOnLeft) {
        PaddingValues(start = 28.dp, end = 14.dp, top = 16.dp, bottom = 16.dp)
    } else {
        PaddingValues(start = 14.dp, end = 28.dp, top = 16.dp, bottom = 16.dp)
    }
}

fun organicLeafChipAlignment(tipOnLeft: Boolean): Alignment {
    return if (tipOnLeft) Alignment.BottomEnd else Alignment.BottomStart
}

fun gardenBillDetailDate(dueDate: LocalDate): String {
    return GardenDateFormatter.format(dueDate)
}

fun Modifier.frostedGardenGlass(supportsNativeBlur: Boolean): Modifier {
    return this
        .background(GardenGlassFill)
        .then(
            if (supportsNativeBlur) {
                Modifier.blur(radius = GardenGlassBlurRadius)
            } else {
                Modifier
            }
        )
}

fun dismissShapeForGardenState(
    gardenState: GardenBillState,
    index: Int,
    isFeaturedPaidRose: Boolean = false
): Shape {
    return when (gardenLeafVisualFor(gardenState, isFeaturedPaidRose)) {
        GardenLeafVisual.FeaturedRose -> RoundedCornerShape(16.dp)
        GardenLeafVisual.GlassLeaf,
        GardenLeafVisual.DriedOverdueLeaf -> organicLeafShapeForIndex(index)
    }
}

data class OrganicLeafShape(
    val tipOnLeft: Boolean
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(organicLeafPath(size = size, tipOnLeft = tipOnLeft))
    }
}

internal fun organicLeafPath(size: Size, tipOnLeft: Boolean): Path {
    val path = Path()
    if (
        !size.width.isFinite() ||
        !size.height.isFinite() ||
        size.width <= 0f ||
        size.height <= 0f
    ) {
        return path
    }
    val anchors = organicLeafAnchors(tipOnLeft)
    val stemX = size.width * anchors.stemXFraction
    val tipX = size.width * anchors.tipXFraction
    val midY = size.height * 0.50f
    path.moveTo(stemX, midY)
    path.cubicTo(
        x1 = lerpFraction(stemX, tipX, 0.22f),
        y1 = size.height * 0.06f,
        x2 = lerpFraction(stemX, tipX, 0.52f),
        y2 = size.height * 0.03f,
        x3 = lerpFraction(stemX, tipX, 0.78f),
        y3 = size.height * 0.20f
    )
    path.cubicTo(
        x1 = lerpFraction(stemX, tipX, 0.93f),
        y1 = size.height * 0.32f,
        x2 = lerpFraction(stemX, tipX, 0.99f),
        y2 = size.height * 0.42f,
        x3 = tipX,
        y3 = midY
    )
    path.cubicTo(
        x1 = lerpFraction(stemX, tipX, 0.99f),
        y1 = size.height * 0.58f,
        x2 = lerpFraction(stemX, tipX, 0.93f),
        y2 = size.height * 0.68f,
        x3 = lerpFraction(stemX, tipX, 0.78f),
        y3 = size.height * 0.80f
    )
    path.cubicTo(
        x1 = lerpFraction(stemX, tipX, 0.52f),
        y1 = size.height * 0.97f,
        x2 = lerpFraction(stemX, tipX, 0.22f),
        y2 = size.height * 0.94f,
        x3 = stemX,
        y3 = midY
    )
    path.close()
    return path
}

internal fun lerpFraction(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

@Composable
fun GardenPathDateMarker(
    dueDate: LocalDate,
    modifier: Modifier = Modifier
) {
    val dateLabel = gardenBillDetailDate(dueDate)
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
    currency: SupportedCurrency,
    isFeaturedPaidRose: Boolean = false,
    modifier: Modifier = Modifier
) {
    when (gardenLeafVisualFor(gardenState, isFeaturedPaidRose)) {
        GardenLeafVisual.FeaturedRose -> PaidRoseBillCard(
            bill = bill,
            currency = currency,
            modifier = modifier
        )
        GardenLeafVisual.DriedOverdueLeaf -> OverdueLeafBillCard(
            bill = bill,
            index = index,
            currency = currency,
            modifier = modifier
        )
        GardenLeafVisual.GlassLeaf -> GlassLeafBillCard(
            bill = bill,
            index = index,
            gardenState = gardenState,
            currency = currency,
            modifier = modifier
        )
    }
}

@Composable
private fun PaidRoseBillCard(
    bill: Bill,
    currency: SupportedCurrency,
    modifier: Modifier = Modifier
) {
    val paidBloomDescription = stringResource(R.string.petals_and_weeds_paid_bloom)
    val supportsNativeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Column(
        modifier = modifier
            .fillMaxWidth()
            .gardenLeafCyanGlow(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_paid_rose),
                contentDescription = "$paidBloomDescription: ${bill.name}",
                modifier = Modifier.size(PaidRoseSize),
                contentScale = ContentScale.Fit
            )

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(50))
                        .background(FrostedPillFill)
                        .then(
                            if (supportsNativeBlur) {
                                Modifier.blur(radius = 12.dp)
                            } else {
                                Modifier
                            }
                        )
                )
                Text(
                    text = stringResource(R.string.petals_and_weeds_paid_stamp),
                    color = GardenForestGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .frostedGardenGlass(supportsNativeBlur)
            )
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = GardenForestGreen,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.petals_and_weeds_status_paid,
                        gardenBillDetailDate(bill.dueDate)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = GardenForestGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = CurrencyFormatter.format(bill.amount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    color = GardenForestGreen,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun OverdueLeafBillCard(
    bill: Bill,
    index: Int,
    currency: SupportedCurrency,
    modifier: Modifier = Modifier
) {
    val leafShape = organicLeafShapeForIndex(index)
    val tipOnLeft = leafTipsLeftForIndex(index)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(OverdueLeafHeight)
            .clip(leafShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(DriedLeafHighlight, DriedLeafMid, DriedLeafShadow)
                )
            )
            .border(width = 1.2.dp, color = DriedLeafEdge, shape = leafShape)
    ) {
        OrganicLeafMidrib(
            tipOnLeft = tipOnLeft,
            color = DriedLeafInk.copy(alpha = 0.28f)
        )
        DefaultLeafBillContent(
            bill = bill,
            gardenState = GardenBillState.Overdue,
            currency = currency,
            index = index,
            contentColor = DriedLeafInk,
            showCategoryChip = false
        )
    }
}

@Composable
private fun GlassLeafBillCard(
    bill: Bill,
    index: Int,
    gardenState: GardenBillState,
    currency: SupportedCurrency,
    modifier: Modifier = Modifier
) {
    val leafShape = organicLeafShapeForIndex(index)
    val tipOnLeft = leafTipsLeftForIndex(index)
    val supportsNativeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(leafShape)
            .border(width = 1.2.dp, color = GlassLeafEdge, shape = leafShape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .frostedGardenGlass(supportsNativeBlur)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(SageLeafTint)
        )
        OrganicLeafMidrib(
            tipOnLeft = tipOnLeft,
            color = GardenForestGreen.copy(alpha = 0.22f)
        )
        DefaultLeafBillContent(
            bill = bill,
            gardenState = gardenState,
            currency = currency,
            index = index,
            contentColor = GardenForestGreen,
            showCategoryChip = true
        )
    }
}

@Composable
private fun DefaultLeafBillContent(
    bill: Bill,
    gardenState: GardenBillState,
    currency: SupportedCurrency,
    index: Int,
    contentColor: Color,
    showCategoryChip: Boolean
) {
    val tipOnLeft = leafTipsLeftForIndex(index)
    val categoryLabel = BloomCategoryDefinitions.fromParentName(bill.parentCategory)?.displayName
        ?: bill.subCategory
    val categoryIcon = gardenCategoryIcon(bill.parentCategory)
    val chipAlignment = organicLeafChipAlignment(tipOnLeft)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(organicLeafContentPadding(tipOnLeft))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (showCategoryChip) 26.dp else 0.dp)
        ) {
            Text(
                text = bill.name,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when (gardenState) {
                    GardenBillState.Paid -> stringResource(
                        R.string.petals_and_weeds_status_paid,
                        gardenBillDetailDate(bill.dueDate)
                    )
                    GardenBillState.Overdue -> stringResource(
                        R.string.petals_and_weeds_status_overdue,
                        gardenBillDetailDate(bill.dueDate)
                    )
                    GardenBillState.FarOff -> stringResource(
                        R.string.petals_and_weeds_status_far_off,
                        gardenBillDetailDate(bill.dueDate)
                    )
                    GardenBillState.Upcoming -> stringResource(
                        R.string.petals_and_weeds_status_upcoming,
                        gardenBillDetailDate(bill.dueDate)
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = CurrencyFormatter.format(bill.amount, currency),
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (showCategoryChip) {
            Row(
                modifier = Modifier
                    .align(chipAlignment)
                    .background(CategoryChipBackground, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = categoryLabel,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OrganicLeafMidrib(
    tipOnLeft: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val anchors = organicLeafAnchors(tipOnLeft)
        val stemX = size.width * anchors.stemXFraction
        val tipX = size.width * anchors.tipXFraction
        val midY = size.height * 0.50f
        val midrib = Path().apply {
            moveTo(stemX, midY)
            cubicTo(
                x1 = lerpFraction(stemX, tipX, 0.34f),
                y1 = midY - size.height * 0.04f,
                x2 = lerpFraction(stemX, tipX, 0.68f),
                y2 = midY + size.height * 0.03f,
                x3 = tipX,
                y3 = midY
            )
        }
        drawPath(
            path = midrib,
            color = color,
            style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        )
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
            color = OrganicVineBase,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = stemPath,
            color = OrganicVineCore,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )

        if (!showBud) {
            return@Canvas
        }

        val budCenter = Offset(size.width / 2f, size.height * 0.42f)
        val radius = budRadius.toPx()

        rotate(degrees = if (alignToStemOnLeft) -18f else 18f, pivot = budCenter) {
            drawOval(
                color = Color(0xFF4CAF50),
                topLeft = Offset(budCenter.x - radius * 0.7f, budCenter.y + radius * 0.15f),
                size = androidx.compose.ui.geometry.Size(radius * 0.55f, radius * 0.9f)
            )
            drawOval(
                color = Color(0xFF4CAF50),
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
