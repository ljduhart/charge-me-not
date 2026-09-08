package com.artie.chargemenot.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.ui.components.MeadowHubScaffold
import com.artie.chargemenot.ui.theme.LeafGreen
import com.artie.chargemenot.ui.theme.MeadowCream
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowWhite
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RichSoilScreen(
    monthlyBudget: Double,
    totalUpcoming: Double,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onMonthlyBudgetChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val nutrientsRemaining = (monthlyBudget - totalUpcoming).coerceAtLeast(0.0)
    val soilRichnessPercent = if (monthlyBudget <= 0.0) {
        0
    } else {
        ((nutrientsRemaining / monthlyBudget) * 100).toInt().coerceIn(0, 100)
    }
    var isEditingBudget by remember { mutableStateOf(false) }
    var budgetInput by remember(monthlyBudget) {
        mutableStateOf(monthlyBudget.toInt().toString())
    }

    MeadowHubScaffold(
        title = stringResource(R.string.meadow_route_rich_soil),
        onOpenDrawer = onOpenDrawer,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MeadowCream)
        ) {
            RichSoilRoseWatermark(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.rich_soil_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                EditableRichSoilMetricCard(
                    label = stringResource(R.string.rich_soil_monthly_budget),
                    monthlyBudget = monthlyBudget,
                    currencyFormat = currencyFormat,
                    isEditingBudget = isEditingBudget,
                    budgetInput = budgetInput,
                    onBudgetInputChange = { budgetInput = it },
                    onToggleEditing = {
                        if (isEditingBudget) {
                            onMonthlyBudgetChange(budgetInput)
                            isEditingBudget = false
                        } else {
                            budgetInput = monthlyBudget.toInt().toString()
                            isEditingBudget = true
                        }
                    }
                )
                RichSoilMetricCard(
                    label = stringResource(R.string.rich_soil_upcoming_outflow),
                    value = currencyFormat.format(totalUpcoming)
                )
                RichSoilMetricCard(
                    label = stringResource(R.string.rich_soil_nutrients_remaining),
                    value = currencyFormat.format(nutrientsRemaining),
                    highlight = true
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MeadowWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.rich_soil_fertility_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = MeadowGreenDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.rich_soil_fertility_value, soilRichnessPercent),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MeadowGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.rich_soil_fertility_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableRichSoilMetricCard(
    label: String,
    monthlyBudget: Double,
    currencyFormat: NumberFormat,
    isEditingBudget: Boolean,
    budgetInput: String,
    onBudgetInputChange: (String) -> Unit,
    onToggleEditing: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                if (isEditingBudget) {
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = onBudgetInputChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Text(
                        text = currencyFormat.format(monthlyBudget),
                        style = MaterialTheme.typography.titleLarge,
                        color = MeadowGreenDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            IconButton(onClick = onToggleEditing) {
                Icon(
                    imageVector = if (isEditingBudget) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = stringResource(
                        if (isEditingBudget) {
                            R.string.rich_soil_monthly_budget_save
                        } else {
                            R.string.rich_soil_monthly_budget_edit
                        }
                    ),
                    tint = MeadowGreenDark
                )
            }
        }
    }
}

@Composable
private fun RichSoilRoseWatermark(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width * 0.5f
        val soilCenterY = size.height * 0.82f
        val soilRadius = size.minDimension * 0.22f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6B4A2E).copy(alpha = 0.16f),
                    Color(0xFF4A3422).copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = Offset(centerX, soilCenterY),
                radius = soilRadius
            ),
            radius = soilRadius,
            center = Offset(centerX, soilCenterY)
        )

        drawCircle(
            color = MeadowEarth.copy(alpha = 0.14f),
            radius = soilRadius * 0.72f,
            center = Offset(centerX, soilCenterY + soilRadius * 0.08f)
        )

        val stemTop = Offset(centerX, soilCenterY - soilRadius * 0.05f)
        val stemBottom = Offset(centerX, soilCenterY - soilRadius * 0.55f)
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    LeafGreen.copy(alpha = 0.35f),
                    MeadowGreen.copy(alpha = 0.28f)
                ),
                startY = stemBottom.y,
                endY = stemTop.y
            ),
            start = stemBottom,
            end = stemTop,
            strokeWidth = soilRadius * 0.08f
        )

        val leafPath = Path().apply {
            moveTo(stemBottom.x, stemBottom.y + soilRadius * 0.08f)
            cubicTo(
                stemBottom.x - soilRadius * 0.22f,
                stemBottom.y + soilRadius * 0.02f,
                stemBottom.x - soilRadius * 0.18f,
                stemBottom.y - soilRadius * 0.08f,
                stemBottom.x - soilRadius * 0.04f,
                stemBottom.y - soilRadius * 0.06f
            )
            close()
        }
        drawPath(
            path = leafPath,
            color = MeadowSage.copy(alpha = 0.3f)
        )

        val roseCenter = Offset(centerX, stemBottom.y - soilRadius * 0.18f)
        val petalRadius = soilRadius * 0.2f
        repeat(6) { index ->
            rotate(degrees = index * 60f, pivot = roseCenter) {
                val petalPath = Path().apply {
                    moveTo(roseCenter.x, roseCenter.y)
                    cubicTo(
                        roseCenter.x - petalRadius * 0.45f,
                        roseCenter.y - petalRadius * 0.15f,
                        roseCenter.x - petalRadius * 0.35f,
                        roseCenter.y - petalRadius * 0.95f,
                        roseCenter.x,
                        roseCenter.y - petalRadius * 1.1f
                    )
                    cubicTo(
                        roseCenter.x + petalRadius * 0.35f,
                        roseCenter.y - petalRadius * 0.95f,
                        roseCenter.x + petalRadius * 0.45f,
                        roseCenter.y - petalRadius * 0.15f,
                        roseCenter.x,
                        roseCenter.y
                    )
                    close()
                }
                drawPath(
                    path = petalPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LeafGreen.copy(alpha = 0.34f),
                            MeadowGreen.copy(alpha = 0.24f),
                            MeadowGreenDark.copy(alpha = 0.14f)
                        ),
                        center = Offset(roseCenter.x, roseCenter.y - petalRadius * 0.55f),
                        radius = petalRadius
                    )
                )
            }
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MeadowSage.copy(alpha = 0.32f),
                    MeadowGreen.copy(alpha = 0.2f)
                ),
                center = roseCenter,
                radius = petalRadius * 0.28f
            ),
            radius = petalRadius * 0.28f,
            center = roseCenter
        )
    }
}

@Composable
private fun RichSoilMetricCard(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MeadowEarth.copy(alpha = 0.12f) else MeadowWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = if (highlight) Color(0xFF1A1A1A) else MeadowGreenDark,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
