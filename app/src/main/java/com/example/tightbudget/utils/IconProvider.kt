package com.example.tightbudget.utils

import com.example.tightbudget.models.ChallengeType

/**
 * Centralised icon provider for the TightBudget application.
 * This utility class serves as the single source of truth for all emoji icons
 * used throughout the application, including categories, achievements, challenges,
 * and actions. By consolidating icon definitions here, we ensure consistency
 * and eliminate duplication across the codebase.
 *
 * Design principles:
 * - Single Responsibility: Manages all icon/emoji definitions
 * - DRY (Don't Repeat Yourself): Eliminates duplicate emoji mappings
 * - Centralisation: One place to update icons for the entire application
 * - Type Safety: Uses enums where possible to prevent invalid icon requests
 */
object IconProvider {

    /**
     * Category emoji definitions.
     * These are the primary icons used throughout the application for
     * representing spending and income categories.
     */
    object CategoryIcons {
        const val HOUSING = "🏠"
        const val FOOD = "🍔"
        const val TRANSPORT = "🚗"
        const val ENTERTAINMENT = "🎬"
        const val SHOPPING = "🛍️"
        const val UTILITIES = "💡"
        const val HEALTH = "💊"
        const val OTHER = "📋"
        const val INCOME = "💰"
        const val UNKNOWN = "📁"
        const val EDUCATION = "📚"
        const val FITNESS = "💪"
        const val GROCERIES = "🛒"
    }

    /**
     * Achievement emoji definitions.
     * These icons are used in the gamification system to represent
     * various achievement badges that users can unlock.
     */
    object AchievementIcons {
        const val SAVER = "💰"
        const val STREAK_KEEPER = "🔥"
        const val TRANSPORT_PRO = "🚗"
        const val FOOD_MANAGER = "🍽️"
        const val CONSISTENT = "📅"
        const val PHOTOGRAPHER = "📸"
        const val HOUSING_PRO = "🏠"
        const val CHALLENGE_MASTER = "🏆"
        const val FUN_MANAGER = "🎥"
        const val INVESTOR = "📈"
        const val TECH_WIZARD = "💻"
        const val BUDGET_GURU = "👑"
        const val LOCKED = "🔒"
    }

    /**
     * Challenge type emoji definitions.
     * These icons represent different types of daily challenges
     * available to users in the gamification system.
     */
    object ChallengeIcons {
        const val TRANSACTION = "📝"
        const val RECEIPT = "📄"
        const val BUDGET_COMPLIANCE = "💰"
        const val SAVINGS = "🏦"
        const val STREAK = "🔥"
        const val CATEGORY_LIMIT = "🛍️"
    }

    /**
     * Action emoji definitions.
     * These icons are used for common user interface actions
     * such as adding, editing, or deleting items.
     */
    object ActionIcons {
        const val ADD = "➕"
        const val EDIT = "✏️"
        const val DELETE = "🗑️"
        const val SETTINGS = "⚙️"
    }

    /**
     * Extended category emoji mappings for fuzzy matching and subcategories.
     * This map allows the system to intelligently match user-created category
     * names to appropriate icons, even when the exact category name doesn't
     * match our predefined list.
     */
    private val extendedCategoryMap = mapOf(
        // Housing related
        "housing" to CategoryIcons.HOUSING,
        "home" to CategoryIcons.HOUSING,
        "rent" to CategoryIcons.HOUSING,
        "mortgage" to CategoryIcons.HOUSING,
        "apartment" to CategoryIcons.HOUSING,

        // Utilities related
        "utilities" to CategoryIcons.UTILITIES,
        "electricity" to CategoryIcons.UTILITIES,
        "water" to "💧",
        "gas" to "🔥",
        "internet" to "🌐",
        "wifi" to "📶",

        // Food related
        "food" to CategoryIcons.FOOD,
        "groceries" to CategoryIcons.GROCERIES,
        "grocery" to CategoryIcons.GROCERIES,
        "restaurant" to "🍽️",
        "dining" to "🍽️",
        "takeout" to "🥡",
        "coffee" to "☕",

        // Transport related
        "transport" to CategoryIcons.TRANSPORT,
        "transportation" to CategoryIcons.TRANSPORT,
        "travel" to "✈️",
        "fuel" to "⛽",
        "car" to "🚗",
        "petrol" to "⛽",
        "bus" to "🚌",
        "train" to "🚆",
        "uber" to "🚕",
        "taxi" to "🚕",

        // Entertainment related
        "entertainment" to CategoryIcons.ENTERTAINMENT,
        "recreation" to "🎮",
        "movies" to CategoryIcons.ENTERTAINMENT,
        "games" to "🎮",
        "fun" to "🎉",
        "hobby" to "🎨",
        "music" to "🎵",
        "concert" to "🎤",
        "spotify" to "🎵",
        "streaming" to "📺",
        "netflix" to "📺",

        // Shopping related
        "shopping" to CategoryIcons.SHOPPING,
        "clothes" to "👚",
        "clothing" to "👚",
        "shoes" to "👟",
        "accessories" to "👜",

        // Health related
        "health" to CategoryIcons.HEALTH,
        "healthcare" to CategoryIcons.HEALTH,
        "medical" to "🏥",
        "doctor" to "👨‍⚕️",
        "pharmacy" to CategoryIcons.HEALTH,
        "medicine" to CategoryIcons.HEALTH,
        "fitness" to CategoryIcons.FITNESS,
        "gym" to CategoryIcons.FITNESS,

        // Education related
        "education" to CategoryIcons.EDUCATION,
        "school" to "🏫",
        "college" to "🎓",
        "university" to "🎓",
        "books" to "📚",
        "courses" to "📝",
        "tuition" to "🎓",

        // Income related
        "income" to CategoryIcons.INCOME,
        "salary" to CategoryIcons.INCOME,
        "paycheck" to CategoryIcons.INCOME,
        "earnings" to CategoryIcons.INCOME,
        "wages" to CategoryIcons.INCOME,
        "dividends" to CategoryIcons.INCOME,
        "interest" to CategoryIcons.INCOME,
        "interest income" to CategoryIcons.INCOME,
        "bonus" to CategoryIcons.INCOME,

        // Miscellaneous
        "pets" to "🐶",
        "subscriptions" to "📱",
        "insurance" to "🛡️",
        "personal care" to "💅",
        "savings" to "💵",
        "childcare" to "🧸",
        "donations" to "🙏",
        "gifts" to "🎁",
        "other" to CategoryIcons.OTHER,
        "miscellaneous" to CategoryIcons.OTHER,
        "misc" to CategoryIcons.OTHER
    )

    /**
     * Retrieves the appropriate emoji icon for a given category name.
     * This method performs intelligent fuzzy matching to find the best icon
     * for user-created categories that may not exactly match predefined names.
     *
     * Matching strategy:
     * 1. Normalise the input (trim, lowercase)
     * 2. Try direct mapping lookup
     * 3. Try exact match against predefined constants
     * 4. Perform partial string matching (contains check)
     * 5. Fallback to unknown icon
     *
     * @param categoryName The name of the category to find an icon for
     * @return The emoji string representing the category
     */
    fun getCategoryIcon(categoryName: String): String {
        // Handle null or empty input
        if (categoryName.isBlank()) {
            return CategoryIcons.UNKNOWN
        }

        // Normalise the category name for matching
        val normalised = categoryName.trim().lowercase()

        // Try direct map lookup first (most efficient)
        extendedCategoryMap[normalised]?.let { return it }

        // Try specific category constants for exact matches
        when (normalised) {
            "food", "food & drink" -> return CategoryIcons.FOOD
            "transport", "transportation" -> return CategoryIcons.TRANSPORT
            "housing", "rent", "home" -> return CategoryIcons.HOUSING
            "entertainment" -> return CategoryIcons.ENTERTAINMENT
            "shopping" -> return CategoryIcons.SHOPPING
            "health", "healthcare" -> return CategoryIcons.HEALTH
            "utilities" -> return CategoryIcons.UTILITIES
            "income" -> return CategoryIcons.INCOME
            "education" -> return CategoryIcons.EDUCATION
            "fitness" -> return CategoryIcons.FITNESS
            "groceries" -> return CategoryIcons.GROCERIES
        }

        // Perform fuzzy matching (partial string match)
        for ((key, icon) in extendedCategoryMap) {
            if (normalised.contains(key) || key.contains(normalised)) {
                return icon
            }
        }

        // Fallback to unknown icon
        return CategoryIcons.UNKNOWN
    }

    /**
     * Retrieves the appropriate emoji icon for an achievement type.
     * Used in the gamification system to display achievement badges.
     *
     * @param achievementName The name or identifier of the achievement
     * @return The emoji string representing the achievement
     */
    fun getAchievementIcon(achievementName: String): String {
        return when (achievementName.lowercase()) {
            "saver", "budget master", "super saver" -> AchievementIcons.SAVER
            "streak keeper" -> AchievementIcons.STREAK_KEEPER
            "transport", "transport pro" -> AchievementIcons.TRANSPORT_PRO
            "food manager" -> AchievementIcons.FOOD_MANAGER
            "consistent", "daily logger" -> AchievementIcons.CONSISTENT
            "photographer" -> AchievementIcons.PHOTOGRAPHER
            "housing pro" -> AchievementIcons.HOUSING_PRO
            "challenge master" -> AchievementIcons.CHALLENGE_MASTER
            "fun manager" -> AchievementIcons.FUN_MANAGER
            "investor" -> AchievementIcons.INVESTOR
            "tech wizard" -> AchievementIcons.TECH_WIZARD
            "budget guru" -> AchievementIcons.BUDGET_GURU
            else -> AchievementIcons.LOCKED
        }
    }

    /**
     * Retrieves the appropriate emoji icon for a challenge type.
     * Used in the daily challenges feature to visually distinguish
     * between different types of challenges.
     *
     * @param challengeType The type of challenge
     * @return The emoji string representing the challenge type
     */
    fun getChallengeIcon(challengeType: ChallengeType): String {
        return when (challengeType) {
            ChallengeType.TRANSACTION -> ChallengeIcons.TRANSACTION
            ChallengeType.RECEIPT -> ChallengeIcons.RECEIPT
            ChallengeType.BUDGET_COMPLIANCE -> ChallengeIcons.BUDGET_COMPLIANCE
            ChallengeType.SAVINGS -> ChallengeIcons.SAVINGS
            ChallengeType.STREAK -> ChallengeIcons.STREAK
            ChallengeType.CATEGORY_LIMIT -> ChallengeIcons.CATEGORY_LIMIT
        }
    }

    /**
     * Retrieves the appropriate emoji icon for a user action.
     * Used throughout the UI for action buttons and menu items.
     *
     * @param actionName The name of the action
     * @return The emoji string representing the action
     */
    fun getActionIcon(actionName: String): String {
        return when (actionName.lowercase()) {
            "add" -> ActionIcons.ADD
            "edit" -> ActionIcons.EDIT
            "delete" -> ActionIcons.DELETE
            else -> ActionIcons.SETTINGS
        }
    }

    /**
     * Creates a default category list with predefined icons and colours.
     * This method provides the standard set of categories that are created
     * for new users when they first use the application.
     *
     * @return List of Category objects with predefined values
     */
    fun getDefaultCategories(): List<Triple<String, String, String>> {
        // Returns list of (name, emoji, colour) triples
        return listOf(
            Triple("Food", CategoryIcons.FOOD, "#FF9800"),
            Triple("Housing", CategoryIcons.HOUSING, "#4CAF50"),
            Triple("Transport", CategoryIcons.TRANSPORT, "#2196F3"),
            Triple("Entertainment", CategoryIcons.ENTERTAINMENT, "#9C27B0"),
            Triple("Utilities", "⚡", "#FFC107"),
            Triple("Health", "⚕️", "#E91E63"),
            Triple("Shopping", CategoryIcons.SHOPPING, "#00BCD4"),
            Triple("Education", CategoryIcons.EDUCATION, "#3F51B5"),
            Triple("Groceries", CategoryIcons.GROCERIES, "#8BC34A"),
            Triple("Fitness", CategoryIcons.FITNESS, "#FF5722")
        )
    }
}
