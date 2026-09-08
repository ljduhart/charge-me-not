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
        .map { parents ->
            if (parents.isEmpty()) {
                MeadowCategories.parentNames
            } else {
                parents
            }
        }

    fun getSubcategoriesForParent(parent: String): Flow<List<String>> =
        categoryRepository.getSubcategoriesForParent(parent)

    fun addCustomSubcategory(parent: String, subCategory: String) {
        coroutineScope.launch(ioDispatcher) {
            categoryRepository.addCustomSubcategory(parent, subCategory)
        }
    }
}
