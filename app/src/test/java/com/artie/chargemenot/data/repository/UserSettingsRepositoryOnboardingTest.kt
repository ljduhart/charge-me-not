package com.artie.chargemenot.data.repository

import com.artie.chargemenot.data.local.UserSettingsDao
import com.artie.chargemenot.data.local.UserSettingsEntity
import com.artie.chargemenot.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest

class UserSettingsRepositoryOnboardingTest {

    @Test
    fun saveOnboardingPreferences_persistsAllFields() = runTest {
        val dao = TrackingSettingsDao()
        val repository = UserSettingsRepository(dao)

        repository.saveOnboardingPreferences(
            monthlyBudget = 310_000L,
            selectedCurrency = "EUR",
            isNagModeEnabled = true
        )

        val saved = dao.lastSaved
        requireNotNull(saved)
        assertEquals(310_000L, saved.monthlyBudget)
        assertEquals("EUR", saved.selectedCurrency)
        assertTrue(saved.isNagModeEnabled)
        assertTrue(saved.isOnboardingComplete)
    }

    @Test
    fun updateMonthlyBudget_preservesOnboardingAndCurrency() = runTest {
        val dao = TrackingSettingsDao(
            initial = UserSettingsEntity(
                monthlyBudget = UserSettings.DEFAULT_MONTHLY_BUDGET,
                isNagModeEnabled = false,
                selectedCurrency = "GBP",
                isOnboardingComplete = true
            )
        )
        val repository = UserSettingsRepository(dao)

        repository.updateMonthlyBudget(400_000L)

        val saved = dao.lastSaved
        requireNotNull(saved)
        assertEquals(400_000L, saved.monthlyBudget)
        assertEquals("GBP", saved.selectedCurrency)
        assertTrue(saved.isOnboardingComplete)
    }

    private class TrackingSettingsDao(
        initial: UserSettingsEntity = UserSettingsEntity(
            monthlyBudget = UserSettings.DEFAULT_MONTHLY_BUDGET,
            isOnboardingComplete = false
        )
    ) : UserSettingsDao {
        var lastSaved: UserSettingsEntity? = null
        private val settings = MutableStateFlow(initial)

        override fun observeSettings(settingsId: Int): Flow<UserSettingsEntity?> = settings

        override suspend fun upsertSettings(settings: UserSettingsEntity) {
            lastSaved = settings
            this.settings.value = settings
        }

        override suspend fun getSettings(settingsId: Int): UserSettingsEntity? = settings.value

        override suspend fun getSettingsCount(settingsId: Int): Int = 1
    }
}
