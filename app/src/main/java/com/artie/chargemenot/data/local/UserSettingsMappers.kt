package com.artie.chargemenot.data.local

import com.artie.chargemenot.domain.model.UserSettings

fun UserSettingsEntity.toDomain(): UserSettings = UserSettings(
    monthlyBudget = monthlyBudget,
    isNagModeEnabled = isNagModeEnabled
)

fun UserSettings.toEntity(): UserSettingsEntity = UserSettingsEntity(
    monthlyBudget = monthlyBudget,
    isNagModeEnabled = isNagModeEnabled
)
