package com.example.tightbudget.models

/**
 * Enumeration of leaderboard categories based on different performance metrics.
 *
 * Each leaderboard type ranks users according to a specific statistic, encouraging
 * diverse forms of engagement and achievement. Users can view multiple leaderboards
 * to find areas where they excel or need improvement.
 *
 * Different leaderboard types appeal to different user motivations:
 * - POINTS: Overall progression and activity
 * - CURRENT_STREAK: Daily consistency and habit formation
 * - LONGEST_STREAK: Peak performance and dedication
 * - ACHIEVEMENTS: Goal completion and mastery
 * - TRANSACTIONS: Activity volume and engagement
 *
 * @author TightBudget Development Team
 */
enum class LeaderboardType {
    /**
     * Ranks users by total points accumulated across all activities.
     * Represents overall progression and is the primary competitive metric.
     * Higher scores indicate more comprehensive engagement with the app.
     */
    POINTS,

    /**
     * Ranks users by their current consecutive day streak.
     * Emphasises ongoing consistency and daily habit maintenance.
     * Resets when users miss a day, making it a dynamic, real-time leaderboard.
     */
    CURRENT_STREAK,

    /**
     * Ranks users by their all-time longest consecutive day streak.
     * Represents peak consistency achievement and never decreases.
     * Provides recognition for users' best historical performance.
     */
    LONGEST_STREAK,

    /**
     * Ranks users by the number of achievements unlocked.
     * Emphasises goal completion and diverse accomplishments.
     * Maximum possible value is 21 (total number of achievements available).
     */
    ACHIEVEMENTS,

    /**
     * Ranks users by total number of transactions logged.
     * Represents activity volume and engagement frequency.
     * Rewards users who consistently track their expenses.
     */
    TRANSACTIONS
}
