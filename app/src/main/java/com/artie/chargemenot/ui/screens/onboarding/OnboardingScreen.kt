package com.artie.chargemenot.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Yard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.ui.theme.LeafGreen
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowGreenLight
import com.artie.chargemenot.ui.theme.MeadowOnboardingCream
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.viewmodels.OnboardingUiState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

private const val ONBOARDING_PAGE_COUNT = 5

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onBudgetEnabledChange: (Boolean) -> Unit,
    onBudgetAmountChange: (Float) -> Unit,
    onCurrencySelected: (SupportedCurrency) -> Unit,
    onRequestWateringSchedule: () -> Unit,
    onNotificationPermissionResult: (Boolean) -> Unit,
    onNotificationPermissionRequestHandled: () -> Unit,
    onRefreshNotificationPermissionState: () -> Unit,
    onSaveOnboardingData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val isLastPage = pagerState.currentPage == ONBOARDING_PAGE_COUNT - 1

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onNotificationPermissionResult(isGranted)
    }

    LaunchedEffect(uiState.shouldRequestNotificationPermission) {
        if (uiState.shouldRequestNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            onNotificationPermissionRequestHandled()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 3) {
            onRefreshNotificationPermissionState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MeadowOnboardingCream)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> SeedWelcomePage()
                1 -> SoilBoundaryPage(
                    budgetEnabled = uiState.budgetEnabled,
                    budgetAmount = uiState.budgetAmount,
                    currencyFormat = currencyFormat,
                    onBudgetEnabledChange = onBudgetEnabledChange,
                    onBudgetAmountChange = onBudgetAmountChange
                )
                2 -> LocalClimatePage(
                    selectedCurrency = uiState.selectedCurrency,
                    onCurrencySelected = onCurrencySelected
                )
                3 -> WateringSchedulePage(
                    nagModeEnabled = uiState.nagModeEnabled,
                    notificationPermissionGranted = uiState.notificationPermissionGranted,
                    onRequestWateringSchedule = onRequestWateringSchedule
                )
                4 -> FirstSproutPage()
            }
        }

        OnboardingPageIndicator(
            pageCount = ONBOARDING_PAGE_COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isLastPage) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = ONBOARDING_PAGE_COUNT - 1,
                                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                            )
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MeadowEarth
                    )
                ) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (isLastPage) {
                Button(
                    onClick = onSaveOnboardingData,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeadowGreen,
                        contentColor = MeadowWhite
                    )
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_enter_garden),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeadowGreen,
                        contentColor = MeadowWhite
                    )
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_next),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SeedWelcomePage() {
    OnboardingPageScaffold(
        title = stringResource(R.string.onboarding_seed_title),
        subtitle = stringResource(R.string.onboarding_seed_subtitle)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_onboarding_seed),
            contentDescription = stringResource(R.string.onboarding_seed_image),
            modifier = Modifier.size(140.dp)
        )
    }
}

@Composable
private fun SoilBoundaryPage(
    budgetEnabled: Boolean,
    budgetAmount: Float,
    currencyFormat: NumberFormat,
    onBudgetEnabledChange: (Boolean) -> Unit,
    onBudgetAmountChange: (Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "trowel_swing")
    val trowelRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trowel_rotation"
    )

    OnboardingPageScaffold(
        title = stringResource(R.string.onboarding_soil_title),
        subtitle = stringResource(R.string.onboarding_soil_subtitle)
    ) {
        Icon(
            imageVector = Icons.Rounded.Yard,
            contentDescription = stringResource(R.string.onboarding_trowel_icon),
            tint = MeadowEarth,
            modifier = Modifier
                .size(96.dp)
                .rotate(trowelRotation)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.onboarding_monthly_budget_toggle),
                style = MaterialTheme.typography.titleMedium,
                color = MeadowGreenDark,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = budgetEnabled,
                onCheckedChange = onBudgetEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MeadowWhite,
                    checkedTrackColor = MeadowSage,
                    uncheckedThumbColor = MeadowWhite,
                    uncheckedTrackColor = MeadowEarth.copy(alpha = 0.35f)
                )
            )
        }

        if (budgetEnabled) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = currencyFormat.format(budgetAmount.toDouble()),
                style = MaterialTheme.typography.headlineSmall,
                color = MeadowGreenDark,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = budgetAmount,
                onValueChange = onBudgetAmountChange,
                valueRange = UserSettings.MIN_MONTHLY_BUDGET.toFloat()..10_000f,
                steps = 38,
                colors = SliderDefaults.colors(
                    thumbColor = MeadowGreen,
                    activeTrackColor = MeadowSage,
                    inactiveTrackColor = MeadowEarth.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(
                    R.string.onboarding_budget_range,
                    currencyFormat.format(UserSettings.MIN_MONTHLY_BUDGET),
                    currencyFormat.format(10_000.0)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LocalClimatePage(
    selectedCurrency: SupportedCurrency,
    onCurrencySelected: (SupportedCurrency) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_climate_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MeadowGreenDark,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_climate_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Icon(
            imageVector = Icons.Rounded.Public,
            contentDescription = null,
            tint = MeadowGreen,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SupportedCurrency.entries, key = { currency -> currency.code }) { currency ->
                CurrencySelectionRow(
                    currency = currency,
                    isSelected = currency == selectedCurrency,
                    onClick = { onCurrencySelected(currency) }
                )
            }
        }
    }
}

@Composable
private fun CurrencySelectionRow(
    currency: SupportedCurrency,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) MeadowSage.copy(alpha = 0.35f) else MeadowWhite
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MeadowGreenDark,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${currency.symbol} · ${currency.code}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = stringResource(R.string.onboarding_currency_selected),
                tint = LeafGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun WateringSchedulePage(
    nagModeEnabled: Boolean,
    notificationPermissionGranted: Boolean,
    onRequestWateringSchedule: () -> Unit
) {
    OnboardingPageScaffold(
        title = stringResource(R.string.onboarding_watering_title),
        subtitle = stringResource(R.string.onboarding_watering_subtitle)
    ) {
        Icon(
            imageVector = Icons.Rounded.WaterDrop,
            contentDescription = null,
            tint = MeadowGreen,
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onRequestWateringSchedule,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MeadowGreen,
                contentColor = MeadowWhite
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.onboarding_enable_watering),
                fontWeight = FontWeight.SemiBold
            )
        }

        if (nagModeEnabled || notificationPermissionGranted) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.onboarding_watering_enabled),
                style = MaterialTheme.typography.bodyMedium,
                color = LeafGreen,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FirstSproutPage() {
    OnboardingPageScaffold(
        title = stringResource(R.string.onboarding_sprout_title),
        subtitle = stringResource(R.string.onboarding_sprout_subtitle)
    ) {
        Icon(
            imageVector = Icons.Rounded.Eco,
            contentDescription = stringResource(R.string.onboarding_sprout_icon),
            tint = LeafGreen,
            modifier = Modifier.size(120.dp)
        )
    }
}

@Composable
private fun OnboardingPageScaffold(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MeadowGreenDark,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        content()
    }
}

@Composable
private fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            if (index == currentPage) {
                LeafIndicatorDot(
                    modifier = Modifier.size(14.dp),
                    color = MeadowGreen
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MeadowEarth, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun LeafIndicatorDot(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val leafPath = Path().apply {
            moveTo(width * 0.5f, 0f)
            cubicTo(width * 0.9f, height * 0.2f, width, height * 0.65f, width * 0.5f, height)
            cubicTo(width * 0.1f, height * 0.65f, 0f, height * 0.2f, width * 0.5f, 0f)
            close()
        }
        drawPath(leafPath, color = color, style = Fill)
        drawLine(
            color = MeadowGreenDark.copy(alpha = 0.5f),
            start = Offset(width * 0.5f, height * 0.15f),
            end = Offset(width * 0.5f, height * 0.9f),
            strokeWidth = min(width, height) * 0.06f
        )
    }
}

@Composable
fun OnboardingLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MeadowOnboardingCream),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Eco,
            contentDescription = null,
            tint = MeadowGreenLight,
            modifier = Modifier.size(56.dp)
        )
    }
}
