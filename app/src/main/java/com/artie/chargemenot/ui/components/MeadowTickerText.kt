package com.artie.chargemenot.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import java.text.NumberFormat
import kotlin.math.roundToLong

private const val TICKER_TRANSITION_MS = 280

@Composable
fun MeadowTickerAmount(
    amount: Double,
    formatter: NumberFormat,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    fontWeight: FontWeight? = FontWeight.Bold
) {
    val tickerKey = amount.roundToCentsKey()

    AnimatedContent(
        targetState = tickerKey,
        modifier = modifier,
        transitionSpec = {
            (
                slideInVertically(
                    animationSpec = tween(TICKER_TRANSITION_MS),
                    initialOffsetY = { height -> height }
                ) + fadeIn(animationSpec = tween(TICKER_TRANSITION_MS))
                ) togetherWith (
                slideOutVertically(
                    animationSpec = tween(TICKER_TRANSITION_MS),
                    targetOffsetY = { height -> -height }
                ) + fadeOut(animationSpec = tween(TICKER_TRANSITION_MS))
                )
        },
        label = "meadowTickerAmount"
    ) { keyedAmount ->
        Text(
            text = formatter.format(keyedAmount),
            style = style,
            color = color,
            fontWeight = fontWeight
        )
    }
}

@Composable
fun MeadowTickerCurrencyLine(
    amount: Double,
    formatter: NumberFormat,
    prefix: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = FontWeight.SemiBold
) {
    val tickerKey = amount.roundToCentsKey()

    AnimatedContent(
        targetState = tickerKey,
        modifier = modifier,
        transitionSpec = {
            (
                slideInVertically(
                    animationSpec = tween(TICKER_TRANSITION_MS),
                    initialOffsetY = { height -> height }
                ) + fadeIn(animationSpec = tween(TICKER_TRANSITION_MS))
                ) togetherWith (
                slideOutVertically(
                    animationSpec = tween(TICKER_TRANSITION_MS),
                    targetOffsetY = { height -> -height }
                ) + fadeOut(animationSpec = tween(TICKER_TRANSITION_MS))
                )
        },
        label = "meadowTickerCurrencyLine"
    ) { keyedAmount ->
        Text(
            text = prefix + formatter.format(keyedAmount),
            style = style,
            color = color,
            fontWeight = fontWeight
        )
    }
}

private fun Double.roundToCentsKey(): Double =
    (this * 100.0).roundToLong() / 100.0
