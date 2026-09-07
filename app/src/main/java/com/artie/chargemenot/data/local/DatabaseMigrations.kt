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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE user_settings
            ADD COLUMN isNagModeEnabled INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE bills
            ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
        db.execSQL(
            """
            ALTER TABLE bills
            ADD COLUMN auditPromptCount INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE bills
            ADD COLUMN parentBillId INTEGER DEFAULT NULL
            """.trimIndent()
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE user_settings
            ADD COLUMN selectedCurrency TEXT NOT NULL DEFAULT 'USD'
            """.trimIndent()
        )
        db.execSQL(
            """
            ALTER TABLE user_settings
            ADD COLUMN isOnboardingComplete INTEGER NOT NULL DEFAULT 1
            """.trimIndent()
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE user_settings
            ADD COLUMN displayName TEXT NOT NULL DEFAULT 'Sarah'
            """.trimIndent()
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE bills
            ADD COLUMN receiptImagePath TEXT DEFAULT NULL
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS compost_table USING FTS4(
                billId INTEGER NOT NULL,
                rawText TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}
