package com.artie.chargemenot.domain.model

import com.artie.chargemenot.data.local.CategoryEntity

object MeadowCategories {
    const val CANOPY = "The Canopy"
    const val ROOT_SYSTEM = "The Root System"
    const val VINES = "The Vines"
    const val FERTILIZER = "The Fertilizer"
    const val POLLINATORS = "The Pollinators"
    const val WILDFLOWERS = "The Wildflowers"

    val parentNames: List<String> = listOf(
        CANOPY,
        ROOT_SYSTEM,
        VINES,
        FERTILIZER,
        POLLINATORS,
        WILDFLOWERS
    )

    val defaultSeedCategories: List<CategoryEntity> = listOf(
        CategoryEntity(parentName = CANOPY, subCategoryName = "Rent", isCustom = false),
        CategoryEntity(parentName = CANOPY, subCategoryName = "Mortgage", isCustom = false),
        CategoryEntity(parentName = CANOPY, subCategoryName = "Home Insurance", isCustom = false),
        CategoryEntity(parentName = CANOPY, subCategoryName = "Property Tax", isCustom = false),

        CategoryEntity(parentName = ROOT_SYSTEM, subCategoryName = "Utilities", isCustom = false),
        CategoryEntity(parentName = ROOT_SYSTEM, subCategoryName = "Transportation", isCustom = false),
        CategoryEntity(parentName = ROOT_SYSTEM, subCategoryName = "Internet & Phone", isCustom = false),
        CategoryEntity(parentName = ROOT_SYSTEM, subCategoryName = "Insurance", isCustom = false),

        CategoryEntity(parentName = VINES, subCategoryName = "Subscriptions", isCustom = false),
        CategoryEntity(parentName = VINES, subCategoryName = "Streaming", isCustom = false),
        CategoryEntity(parentName = VINES, subCategoryName = "Software", isCustom = false),
        CategoryEntity(parentName = VINES, subCategoryName = "Memberships", isCustom = false),

        CategoryEntity(parentName = FERTILIZER, subCategoryName = "Groceries", isCustom = false),
        CategoryEntity(parentName = FERTILIZER, subCategoryName = "Dining Out", isCustom = false),
        CategoryEntity(parentName = FERTILIZER, subCategoryName = "Meal Delivery", isCustom = false),

        CategoryEntity(parentName = POLLINATORS, subCategoryName = "Healthcare", isCustom = false),
        CategoryEntity(parentName = POLLINATORS, subCategoryName = "Dental", isCustom = false),
        CategoryEntity(parentName = POLLINATORS, subCategoryName = "Pharmacy", isCustom = false),
        CategoryEntity(parentName = POLLINATORS, subCategoryName = "Wellness", isCustom = false),

        CategoryEntity(parentName = WILDFLOWERS, subCategoryName = "Entertainment", isCustom = false),
        CategoryEntity(parentName = WILDFLOWERS, subCategoryName = "Hobbies", isCustom = false),
        CategoryEntity(parentName = WILDFLOWERS, subCategoryName = "Other", isCustom = false)
    )

    val defaultSubcategoryByParent: Map<String, String> = mapOf(
        CANOPY to "Rent",
        ROOT_SYSTEM to "Utilities",
        VINES to "Subscriptions",
        FERTILIZER to "Groceries",
        POLLINATORS to "Healthcare",
        WILDFLOWERS to "Other"
    )

    fun shortDisplayName(parentName: String): String = when (parentName) {
        CANOPY -> "Canopy"
        ROOT_SYSTEM -> "Roots"
        VINES -> "Vines"
        FERTILIZER -> "Fertilizer"
        POLLINATORS -> "Pollinators"
        WILDFLOWERS -> "Wildflowers"
        else -> parentName
    }

    fun legacyBillCategoryToTaxonomy(category: BillCategory): Pair<String, String> = when (category) {
        BillCategory.RENT -> CANOPY to "Rent"
        BillCategory.FOOD -> FERTILIZER to "Groceries"
        BillCategory.UTILITIES -> ROOT_SYSTEM to "Utilities"
        BillCategory.SUBSCRIPTIONS -> VINES to "Subscriptions"
        BillCategory.TRANSPORTATION -> ROOT_SYSTEM to "Transportation"
        BillCategory.HEALTHCARE -> POLLINATORS to "Healthcare"
        BillCategory.ENTERTAINMENT -> WILDFLOWERS to "Entertainment"
        BillCategory.OTHER -> WILDFLOWERS to "Other"
    }

    fun taxonomyToLegacyBillCategory(parentCategory: String, subCategory: String): BillCategory? {
        return when (parentCategory) {
            CANOPY -> BillCategory.RENT
            ROOT_SYSTEM -> when (subCategory) {
                "Transportation" -> BillCategory.TRANSPORTATION
                else -> BillCategory.UTILITIES
            }
            VINES -> BillCategory.SUBSCRIPTIONS
            FERTILIZER -> BillCategory.FOOD
            POLLINATORS -> BillCategory.HEALTHCARE
            WILDFLOWERS -> when (subCategory) {
                "Entertainment" -> BillCategory.ENTERTAINMENT
                else -> BillCategory.OTHER
            }
            else -> null
        }
    }
}
