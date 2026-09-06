package com.artie.chargemenot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowLavender

@Composable
fun SubscriptionBrandIcon(
    bill: Bill,
    modifier: Modifier = Modifier
) {
    val brandStyle = resolveSubscriptionBrandStyle(bill.name)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(if (brandStyle.useCircle) CircleShape else RoundedCornerShape(10.dp))
            .background(brandStyle.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = brandStyle.glyph,
            style = MaterialTheme.typography.titleMedium,
            color = brandStyle.glyphColor,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class SubscriptionBrandStyle(
    val glyph: String,
    val backgroundColor: Color,
    val glyphColor: Color,
    val useCircle: Boolean
)

private fun resolveSubscriptionBrandStyle(name: String): SubscriptionBrandStyle {
    val normalized = name.trim().lowercase()
    return when {
        normalized.contains("netflix") -> SubscriptionBrandStyle(
            glyph = "N",
            backgroundColor = Color(0xFFE50914),
            glyphColor = Color.White,
            useCircle = false
        )
        normalized.contains("spotify") -> SubscriptionBrandStyle(
            glyph = "♪",
            backgroundColor = Color(0xFF1DB954),
            glyphColor = Color.White,
            useCircle = true
        )
        normalized.contains("audible") || normalized.contains("amazon") -> SubscriptionBrandStyle(
            glyph = "A",
            backgroundColor = Color(0xFFFF9900),
            glyphColor = Color(0xFF232F3E),
            useCircle = false
        )
        normalized.contains("disney") -> SubscriptionBrandStyle(
            glyph = "D+",
            backgroundColor = Color(0xFF113CCF),
            glyphColor = Color.White,
            useCircle = false
        )
        normalized.contains("adobe") -> SubscriptionBrandStyle(
            glyph = "Ad",
            backgroundColor = Color(0xFFFF0000),
            glyphColor = Color.White,
            useCircle = false
        )
        else -> SubscriptionBrandStyle(
            glyph = billInitial(name),
            backgroundColor = MeadowLavender.copy(alpha = 0.35f),
            glyphColor = MeadowGreenDark,
            useCircle = true
        )
    }
}
