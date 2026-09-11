package com.artie.chargemenot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.artie.chargemenot.domain.model.MeadowCategories

@Database(
    entities = [BillEntity::class, UserSettingsEntity::class, CompostEntity::class, CategoryEntity::class],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun billDao(): BillDao

    abstract fun userSettingsDao(): UserSettingsDao

    abstract fun compostDao(): CompostDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DATABASE_NAME = "charge_me_not.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(CategorySeedCallback())
                    .build().also { INSTANCE = it }
            }
        }
    }

    private class CategorySeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            MeadowCategories.defaultSeedCategories.forEach { category ->
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO categories (parentName, subCategoryName, isCustom)
                    VALUES ('${category.parentName.replace("'", "''")}', '${category.subCategoryName.replace("'", "''")}', 0)
                    """.trimIndent()
                )
            }
        }
    }
}
