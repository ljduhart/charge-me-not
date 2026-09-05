package com.artie.chargemenot.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.artie.chargemenot.domain.model.UserSettings

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_settings (
                id INTEGER NOT NULL PRIMARY KEY,
                monthlyBudget REAL NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO user_settings (id, monthlyBudget)
            VALUES (${UserSettingsEntity.SETTINGS_ID}, ${UserSettings.DEFAULT_MONTHLY_BUDGET})
            """.trimIndent()
        )
    }
}
