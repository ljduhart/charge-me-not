package com.artie.chargemenot.data.repository

import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.data.local.toDomain
import com.artie.chargemenot.data.local.toEntity
import com.artie.chargemenot.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsRepository(
    private val userSettingsDao: UserSettingsDao
) {

    fun observeUserSettings(): Flow<UserSettings> =
        userSettingsDao.observeSettings().map { entity ->
            entity?.toDomain() ?: UserSettings()
        }

    fun observeMonthlyBudget(): Flow<Double> =
        observeUserSettings().map { settings -> settings.monthlyBudget }

    fun observeNagModeEnabled(): Flow<Boolean> =
        observeUserSettings().map { settings -> settings.isNagModeEnabled }

    fun observeOnboardingComplete(): Flow<Boolean> =
        observeUserSettings().map { settings -> settings.isOnboardingComplete }

    suspend fun getOnboardingComplete(): Boolean {
        return userSettingsDao.getSettings()?.isOnboardingComplete ?: false
    }

    suspend fun saveOnboardingPreferences(
        monthlyBudget: Double,
        selectedCurrency: String,
        isNagModeEnabled: Boolean
    ) {
        val sanitizedBudget = monthlyBudget.coerceAtLeast(UserSettings.MIN_MONTHLY_BUDGET)
        val current = userSettingsDao.getSettings()?.toDomain() ?: UserSettings()
        userSettingsDao.upsertSettings(
            current.copy(
                monthlyBudget = sanitizedBudget,
                selectedCurrency = selectedCurrency,
                isNagModeEnabled = isNagModeEnabled,
                isOnboardingComplete = true
            ).toEntity()
        )
    }

    suspend fun updateMonthlyBudget(monthlyBudget: Double) {
        val current = userSettingsDao.getSettings()?.toDomain() ?: UserSettings()
        val sanitizedBudget = monthlyBudget.coerceAtLeast(UserSettings.MIN_MONTHLY_BUDGET)
        userSettingsDao.upsertSettings(
            current.copy(monthlyBudget = sanitizedBudget).toEntity()
        )
    }

    suspend fun updateNagModeEnabled(isEnabled: Boolean) {
        val current = userSettingsDao.getSettings()?.toDomain() ?: UserSettings()
        userSettingsDao.upsertSettings(
            current.copy(isNagModeEnabled = isEnabled).toEntity()
        )
    }

    suspend fun getNagModeEnabled(): Boolean {
        return userSettingsDao.getSettings()?.isNagModeEnabled ?: false
    }

    suspend fun ensureDefaultSettingsIfNeeded() {
        if (userSettingsDao.getSettingsCount() == 0) {
            userSettingsDao.upsertSettings(
                UserSettingsEntity(
                    monthlyBudget = UserSettings.DEFAULT_MONTHLY_BUDGET,
                    isNagModeEnabled = false,
                    selectedCurrency = UserSettings.DEFAULT_CURRENCY,
                    isOnboardingComplete = false
                )
            )
        }
    }
}
