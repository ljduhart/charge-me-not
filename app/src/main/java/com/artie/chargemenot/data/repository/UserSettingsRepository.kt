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
                    isNagModeEnabled = false
                )
            )
        }
    }
}
