package com.artie.chargemenot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = :settingsId LIMIT 1")
    fun observeSettings(settingsId: Int = UserSettingsEntity.SETTINGS_ID): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: UserSettingsEntity)

    @Query("SELECT * FROM user_settings WHERE id = :settingsId LIMIT 1")
    suspend fun getSettings(settingsId: Int = UserSettingsEntity.SETTINGS_ID): UserSettingsEntity?

    @Query("SELECT COUNT(*) FROM user_settings WHERE id = :settingsId")
    suspend fun getSettingsCount(settingsId: Int = UserSettingsEntity.SETTINGS_ID): Int
}
