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

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                parentName TEXT NOT NULL,
                subCategoryName TEXT NOT NULL,
                isCustom INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_categories_parentName_subCategoryName
            ON categories (parentName, subCategoryName)
            """.trimIndent()
        )

        com.artie.chargemenot.domain.model.MeadowCategories.defaultSeedCategories.forEach { category ->
            db.execSQL(
                """
                INSERT OR IGNORE INTO categories (parentName, subCategoryName, isCustom)
                VALUES ('${category.parentName.replace("'", "''")}', '${category.subCategoryName.replace("'", "''")}', 0)
                """.trimIndent()
            )
        }

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS bills_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                amount REAL NOT NULL,
                dueDate TEXT NOT NULL,
                parentCategory TEXT NOT NULL,
                subCategory TEXT NOT NULL,
                isPaid INTEGER NOT NULL DEFAULT 0,
                usageCount INTEGER NOT NULL DEFAULT 0,
                auditPromptCount INTEGER NOT NULL DEFAULT 0,
                parentBillId INTEGER DEFAULT NULL,
                receiptImagePath TEXT DEFAULT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO bills_new (
                id, name, amount, dueDate, parentCategory, subCategory,
                isPaid, usageCount, auditPromptCount, parentBillId, receiptImagePath
            )
            SELECT
                id,
                name,
                amount,
                dueDate,
                CASE category
                    WHEN 'RENT' THEN 'The Canopy'
                    WHEN 'FOOD' THEN 'The Fertilizer'
                    WHEN 'UTILITIES' THEN 'The Root System'
                    WHEN 'SUBSCRIPTIONS' THEN 'The Vines'
                    WHEN 'TRANSPORTATION' THEN 'The Root System'
                    WHEN 'HEALTHCARE' THEN 'The Pollinators'
                    WHEN 'ENTERTAINMENT' THEN 'The Wildflowers'
                    ELSE 'The Wildflowers'
                END,
                CASE category
                    WHEN 'RENT' THEN 'Rent'
                    WHEN 'FOOD' THEN 'Groceries'
                    WHEN 'UTILITIES' THEN 'Utilities'
                    WHEN 'SUBSCRIPTIONS' THEN 'Subscriptions'
                    WHEN 'TRANSPORTATION' THEN 'Transportation'
                    WHEN 'HEALTHCARE' THEN 'Healthcare'
                    WHEN 'ENTERTAINMENT' THEN 'Entertainment'
                    ELSE 'Other'
                END,
                isPaid,
                usageCount,
                auditPromptCount,
                parentBillId,
                receiptImagePath
            FROM bills
            """.trimIndent()
        )

        db.execSQL("DROP TABLE bills")
        db.execSQL("ALTER TABLE bills_new RENAME TO bills")
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
