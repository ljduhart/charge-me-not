package com.artie.chargemenot.data.repository

import com.artie.chargemenot.data.local.CategoryDao
import com.artie.chargemenot.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    fun getDistinctParentNames(): Flow<List<String>> = categoryDao.getDistinctParentNames()

    fun getSubcategoriesForParent(parent: String): Flow<List<String>> =
        categoryDao.getSubcategoriesForParent(parent)

    suspend fun addCustomSubcategory(parent: String, subCategory: String) {
        val trimmedSubcategory = subCategory.trim()
        if (trimmedSubcategory.isBlank()) return
        if (categoryDao.countSubcategory(parent, trimmedSubcategory) > 0) return

        categoryDao.insertCategory(
            CategoryEntity(
                parentName = parent,
                subCategoryName = trimmedSubcategory,
                isCustom = true
            )
        )
    }
}
