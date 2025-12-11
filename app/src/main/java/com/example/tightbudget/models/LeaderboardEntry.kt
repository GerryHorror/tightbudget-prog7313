package com.example.tightbudget.models

/**
 * Represents a single user's position and statistics on a leaderboard.
 *
 * Leaderboards provide competitive motivation by allowing users to compare their
 * progress against others. Each entry contains the user's ranking, identification,
 * and relevant performance metrics for the leaderboard type.
 *
 * Leaderboards can be filtered by time period (daily, weekly, monthly, all-time)
 * and metric (points, streak, achievements, transactions).
 *
 * @property userId Unique identifier for the user this entry represents.
 * @property username Display name shown on the leaderboard. Should be sanitised
 *                   to prevent inappropriate content or personally identifiable information.
 * @property rank The user's position on this leaderboard (1 = first place).
 *               Lower numbers indicate better performance.
 * @property score The numeric value determining rank on this leaderboard.
 *                Interpretation depends on leaderboard type:
 *                - Points leaderboard: Total points earned
 *                - Streak leaderboard: Current or longest streak
 *                - Achievement leaderboard: Number of achievements unlocked
 *                - Transaction leaderboard: Number of transactions logged
 * @property level The user's current level for display purposes.
 * @property avatarEmoji Optional emoji representing the user's avatar.
 *                      Provides visual personality whilst maintaining privacy.
 *
 * @author TightBudget Development Team
 */
data class LeaderboardEntry(
    val userId: Int = 0,
    val username: String = "",
    val rank: Int = 0,
    val score: Int = 0,
    val level: Int = 1,
    val avatarEmoji: String = "👤"
) {
    /**
     * No-argument constructor required by Firebase Realtime Database for
     * automatic object deserialisation.
     */
    constructor() : this(0, "", 0, 0, 1, "👤")

    /**
     * Determines if this entry represents the current user's ranking.
     *
     * Used for highlighting the user's own position in the leaderboard UI,
     * making it easier to locate themselves amongst other competitors.
     *
     * @param currentUserId The ID of the currently logged-in user.
     * @return True if this entry belongs to the current user, false otherwise.
     */
    fun isCurrentUser(currentUserId: Int): Boolean {
        return userId == currentUserId
    }

    /**
     * Generates a formatted rank string with ordinal suffix.
     *
     * Provides user-friendly rank display with appropriate suffixes:
     * - 1 -> "1st"
     * - 2 -> "2nd"
     * - 3 -> "3rd"
     * - 4 -> "4th"
     * - etc.
     *
     * Handles special cases for 11th, 12th, 13th which use "th" instead of
     * following the last-digit rule.
     *
     * @return Formatted rank string with ordinal suffix (e.g., "1st", "42nd").
     */
    fun getFormattedRank(): String {
        val suffix = when {
            rank % 100 in 11..13 -> "th" // Special case for 11th, 12th, 13th
            rank % 10 == 1 -> "st"
            rank % 10 == 2 -> "nd"
            rank % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$rank$suffix"
    }

    /**
     * Formats the score value for display with appropriate suffix.
     *
     * Simplifies large numbers for readability:
     * - 1,234 -> "1.2K"
     * - 45,678 -> "45.7K"
     * - 1,234,567 -> "1.2M"
     *
     * Numbers under 1,000 are displayed without modification.
     *
     * @return Formatted score string (e.g., "1,234", "45.7K", "1.2M").
     */
    fun getFormattedScore(): String {
        return when {
            score >= 1_000_000 -> String.format("%.1fM", score / 1_000_000.0)
            score >= 1_000 -> String.format("%.1fK", score / 1_000.0)
            else -> score.toString()
        }
    }
}
