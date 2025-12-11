package com.example.tightbudget.models

/**
 * Enumeration of daily challenge categories that define measurable user objectives.
 *
 * Each challenge belongs to exactly one type, which determines how progress is tracked
 * and what constitutes successful completion. The type defines the semantic meaning
 * of both the challenge's targetValue and currentProgress properties.
 *
 * Challenge types enable diverse daily objectives that encourage different aspects
 * of financial management and application engagement.
 *
 * When adding new challenge types, corresponding progress tracking logic must be
 * implemented in GamificationManager.getCurrentChallengeProgress() to ensure
 * accurate real-time progress updates.
 *
 * @author TightBudget Development Team
 */
enum class ChallengeType {
    /**
     * Challenges requiring the user to add a specific number of transactions.
     * Target value: Count of transactions to record.
     * Progress: Increments with each transaction added during the challenge period.
     * Example: "Add 3 transactions today" (targetValue = 3).
     */
    TRANSACTION,

    /**
     * Challenges requiring the user to upload a specific number of receipt images.
     * Target value: Count of receipts to upload.
     * Progress: Increments with each transaction that includes a receipt photo.
     * Example: "Upload 2 receipts today" (targetValue = 2).
     */
    RECEIPT,

    /**
     * Challenges requiring the user to stay within their allocated budget.
     * Target value: Binary (1 = stayed within budget, 0 = exceeded budget).
     * Progress: Calculated by comparing daily spending to pro-rated monthly budget.
     * Example: "Keep expenses under £50 today" (targetValue = 1).
     */
    BUDGET_COMPLIANCE,

    /**
     * Challenges based on spending less than a previous time period.
     * Target value: Percentage or absolute amount saved compared to yesterday.
     * Progress: Calculated by comparing today's spending to yesterday's total.
     * Example: "Spend 20% less than yesterday" (targetValue = 20).
     * Note: Currently defined but implementation is simplified in the manager.
     */
    SAVINGS,

    /**
     * Challenges based on maintaining consecutive days of application usage.
     * Target value: Number of consecutive days required.
     * Progress: Current streak count from user progress data.
     * Example: "Maintain your 7-day streak" (targetValue = 7).
     */
    STREAK,

    /**
     * Challenges requiring the user to not exceed spending limits in specific categories.
     * Target value: Number of categories to remain compliant in.
     * Progress: Count of categories where spending is within predefined limits.
     * Example: "Stay within limits in 3 categories" (targetValue = 3).
     * Note: Currently defined but implementation is simplified (random compliance).
     */
    CATEGORY_LIMIT
}