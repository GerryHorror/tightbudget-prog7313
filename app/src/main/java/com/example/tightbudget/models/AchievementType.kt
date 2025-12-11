package com.example.tightbudget.models

/**
 * Enumeration of achievement categories used to classify and validate unlock criteria.
 *
 * Each achievement belongs to exactly one type, which determines how progress is measured
 * and what user statistics are evaluated during unlock validation. The type defines the
 * semantic meaning of the achievement's targetValue property.
 *
 * When adding new achievement types, corresponding validation logic must be implemented
 * in GamificationManager.meetsAchievementCriteria() to ensure proper unlock behaviour.
 *
 * @author TightBudget Development Team
 */
enum class AchievementType {
    /**
     * Achievements based on cumulative points earned across all activities.
     * Target value represents the total point threshold required.
     * Example: "Point Collector" unlocks at 500 total points.
     */
    POINTS,

    /**
     * Achievements based on consecutive days of application usage.
     * Target value represents the number of consecutive days required.
     * Example: "Week Warrior" unlocks after logging activity for 7 days straight.
     */
    STREAK,

    /**
     * Achievements based on the total number of transactions recorded.
     * Target value represents the count of transactions required.
     * Example: "Transaction Pro" unlocks after adding 25 transactions.
     */
    TRANSACTIONS,

    /**
     * Achievements based on successfully meeting budget goals.
     * Target value represents the number of budget goals achieved.
     * Example: "Budget Champion" unlocks after meeting 5 monthly budgets.
     */
    BUDGET_GOALS,

    /**
     * Achievements based on the number of receipt images uploaded.
     * Target value represents the count of receipts required.
     * Example: "Receipt Collector" unlocks after uploading 10 receipts.
     */
    RECEIPTS,

    /**
     * Achievements based on cumulative savings or spending reductions.
     * Target value represents the monetary amount saved.
     * Example: "Savings Master" unlocks after saving £100 total.
     * Note: Currently defined but not fully implemented in validation logic.
     */
    SAVINGS,

    /**
     * Achievements based on diversity of spending categories used.
     * Target value represents the number of unique categories required.
     * Example: "Category Explorer" unlocks after using 10 different categories.
     */
    CATEGORIES
}