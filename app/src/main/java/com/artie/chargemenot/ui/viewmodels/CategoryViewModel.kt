package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.repository.CategoryRepository
import com.artie.chargemenot.domain.model.MeadowCategories
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val parentCategories: Flow<List<String>> = categoryRepository
        .getDistinctParentNames()
        .map { parents -> orderParentCategories(parents) }

    fun getSubcategoriesForParent(parent: String): Flow<List<String>> =
        categoryRepository.getSubcategoriesForParent(parent).map { subcategories ->
            if (subcategories.isNotEmpty()) {
                subcategories
            } else {
                defaultSubcategoriesFor(parent)
            }
        }

    fun addCustomSubcategory(parent: String, subCategory: String) {
        coroutineScope.launch(ioDispatcher) {
            categoryRepository.addCustomSubcategory(parent, subCategory)
        }
    }

    private fun orderParentCategories(parents: List<String>): List<String> {
        val resolvedParents = if (parents.isEmpty()) {
            MeadowCategories.parentNames
        } else {
            parents
        }

        val canonical = MeadowCategories.parentNames.filter { parent -> parent in resolvedParents }
        val extras = resolvedParents.filter { parent -> parent !in MeadowCategories.parentNames }
        return canonical + extras
    }

    private fun defaultSubcategoriesFor(parent: String): List<String> {
        return MeadowCategories.defaultSeedCategories
            .filter { category -> category.parentName == parent }
            .map { category -> category.subCategoryName }
    }
}
