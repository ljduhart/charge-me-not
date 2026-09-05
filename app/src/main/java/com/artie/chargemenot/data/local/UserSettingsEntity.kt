package com.artie.chargemenot.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = SETTINGS_ID,
    val monthlyBudget: Double,
    val isNagModeEnabled: Boolean = false
) {
    companion object {
        const val SETTINGS_ID = 1
    }
}
