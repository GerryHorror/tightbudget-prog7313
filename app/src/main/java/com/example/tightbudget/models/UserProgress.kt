package com.example.tightbudget.models

/**
 * Comprehensive data class tracking a user's complete gamification state and progress.
 *
 * This model serves as the central repository for all gamification-related statistics,
 * achievements, and temporal data. It is persisted to Firebase Realtime Database and
 * updated throughout the user's interaction with the application.
 *
 * The class follows a mutable design pattern to facilitate efficient updates through
 * Firebase's value change listeners whilst maintaining data class benefits.
 *
 * @property userId Unique identifier linking this progress record to a specific user.
 *                  Maps to the user's primary ID in the authentication system.
 *                  Used as the Firebase database key under "userProgress/{userId}".
 *
 * @property totalPoints Cumulative sum of all points earned by the user across all activities.
 *                       This value increases monotonically and never decreases.
 *                       Used to calculate the user's current level and unlock point-based
 *                       achievements. Updated whenever points are awarded.
 *
 * @property currentLevel The user's current level based on total points accumulated.
 *                        Calculated by GamificationManager.calculateLevel() using
 *                        predefined point thresholds. Ranges from 1 (Budget Newbie)
 *                        to 10 (Grandmaster). Provides progression milestones and
 *                        status recognition.
 *
 * @property currentStreak Number of consecutive days the user has logged activity.
 *                         Increments by 1 each day when activity is detected.
 *                         Resets to 1 if a day is missed (fails to log any transaction).
 *                         Used for streak-based achievements and bonus point awards.
 *
 * @property longestStreak Historical record of the user's highest consecutive day count.
 *                         Updated whenever currentStreak exceeds this value.
 *                         Never decreases, preserving the user's best performance.
 *                         Used for "longest streak" achievement unlock criteria.
 *
 * @property lastLoginDate Timestamp (milliseconds since epoch) of the user's most recent
 *                         activity session. Updated each time a transaction is added.
 *                         Critical for streak calculation logic, determining whether
 *                         the user logged in "today" or "yesterday" relative to current time.
 *
 * @property transactionCount Total number of transactions the user has recorded.
 *                            Increments by 1 with each transaction addition.
 *                            Used for transaction-based achievement unlock criteria
 *                            and daily challenge progress tracking.
 *
 * @property receiptsUploaded Total number of receipt images the user has uploaded.
 *                            Increments by 1 when a transaction includes a receipt photo.
 *                            Used for receipt-based achievement unlock criteria and
 *                            receipt challenge progress validation.
 *
 * @property budgetGoalsMet Count of budget goals successfully achieved by the user.
 *                          Increments when daily, weekly, or monthly budget compliance
 *                          is verified. Used for budget-based achievement unlocks.
 *
 * @property achievementsUnlocked List of achievement IDs that have been unlocked.
 *                                Each ID corresponds to an Achievement.id value.
 *                                Used to prevent duplicate unlocks and display locked/
 *                                unlocked status in the UI. Grows monotonically as
 *                                achievements are earned.
 *
 * @property dailyChallenges List of challenge IDs currently assigned to the user.
 *                           Typically contains 3 challenge IDs representing today's
 *                           active challenges. Refreshed daily when challenges expire.
 *                           Used to query and display active challenges from Firebase.
 *
 * @property lastChallengeRefresh Timestamp (milliseconds since epoch) when daily challenges
 *                                were last regenerated. Used to determine if challenges
 *                                need refreshing (typically at midnight). Prevents
 *                                duplicate challenge generation within the same day.
 *
 * @author TightBudget Development Team
 */
data class UserProgress(
    var userId: Int = 0,
    var totalPoints: Int = 0,
    var currentLevel: Int = 1,
    var currentStreak: Int = 0,
    var longestStreak: Int = 0,
    var lastLoginDate: Long = System.currentTimeMillis(),
    var transactionCount: Int = 0,
    var receiptsUploaded: Int = 0,
    var budgetGoalsMet: Int = 0,
    var achievementsUnlocked: List<String> = emptyList(),
    var dailyChallenges: List<String> = emptyList(),
    var lastChallengeRefresh: Long = 0
) {
    /**
     * No-argument constructor required by Firebase Realtime Database for
     * automatic object deserialisation. Delegates to primary constructor
     * with sensible default values for all properties.
     *
     * Default values ensure a new user starts with:
     * - Level 1 (Budget Newbie)
     * - Zero points and achievements
     * - Current timestamp for login tracking
     */
    constructor() : this(0, 0, 1, 0, 0, System.currentTimeMillis(), 0, 0, 0, emptyList(), emptyList(), 0)
}