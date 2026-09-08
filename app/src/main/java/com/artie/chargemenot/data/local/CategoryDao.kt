package com.artie.chargemenot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT DISTINCT parentName FROM categories ORDER BY id ASC")
    fun getDistinctParentNames(): Flow<List<String>>

    @Query(
        """
        SELECT subCategoryName FROM categories
        WHERE parentName = :parent
        ORDER BY isCustom ASC, id ASC
        """
    )
    fun getSubcategoriesForParent(parent: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query(
        """
        SELECT COUNT(*) FROM categories
        WHERE parentName = :parent AND subCategoryName = :subCategory
        """
    )
    suspend fun countSubcategory(parent: String, subCategory: String): Int
}
