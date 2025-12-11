package com.example.tightbudget.firebase

import android.util.Log
import com.example.tightbudget.models.UserProgress

/**
 * Manages point allocation, level calculations, and progression within the gamification system.
 *
 * This manager centralises all logic related to:
 * - Awarding points for user actions
 * - Calculating user levels based on total points accumulated
 * - Determining points required for next level progression
 * - Providing level progression data for UI display
 *
 * The points system forms the foundation of user progression, with points serving as
 * the universal currency that drives level advancement and unlocks achievements.
 *
 * Thread Safety: This class is stateless and thread-safe. All calculations operate
 * on immutable data and return new instances rather than mutating state.
 *
 * @author TightBudget Development Team
 */
class PointsManager {

    companion object {
        private const val TAG = "PointsManager"

        /**
         * Total number of levels available in the progression system.
         * Currently ranges from 1 (Budget Newbie) to 10 (Grandmaster).
         */
        private const val MAX_LEVEL = 10

        /**
         * Point thresholds required to reach each level.
         * Index corresponds to level number (index 0 = level 1, index 9 = level 10).
         * These values define the progression curve, with exponentially increasing
         * requirements to maintain challenge and long-term engagement.
         */
        private val LEVEL_THRESHOLDS = intArrayOf(
            0,      // Level 1: Budget Newbie (starting level)
            400,    // Level 2: Penny Tracker
            800,    // Level 3: Smart Spender
            1200,   // Level 4: Budget Apprentice
            1800,   // Level 5: Money Manager
            2500,   // Level 6: Financial Strategist
            3500,   // Level 7: Budget Expert
            5000,   // Level 8: Savings Specialist
            7500,   // Level 9: Finance Master
            10000   // Level 10: Grandmaster
        )

        /**
         * Descriptive titles for each level, providing prestige and recognition.
         * Index corresponds to level number (index 0 = level 1).
         * These titles are displayed throughout the UI to reinforce progression.
         */
        private val LEVEL_TITLES = arrayOf(
            "Budget Newbie",
            "Penny Tracker",
            "Smart Spender",
            "Budget Apprentice",
            "Money Manager",
            "Financial Strategist",
            "Budget Expert",
            "Savings Specialist",
            "Finance Master",
            "Grandmaster"
        )
    }

    /**
     * Represents comprehensive level progression data for a user.
     *
     * @property currentLevel The user's current level (1 to [MAX_LEVEL]).
     * @property currentLevelTitle The descriptive title for the current level.
     * @property totalPoints The user's cumulative point total.
     * @property pointsForCurrentLevel Points required to reach the current level.
     * @property pointsForNextLevel Points required to reach the next level.
     *                             Equals [pointsForCurrentLevel] if already at max level.
     * @property pointsTowardsNextLevel Progress within the current level range (points earned beyond current level threshold).
     * @property pointsRemainingForNextLevel Points still needed to reach the next level.
     *                                      Zero if already at max level.
     * @property progressPercentage Percentage progress towards next level (0-100).
     *                             100 if at max level.
     * @property isMaxLevel Whether the user has reached the highest available level.
     */
    data class LevelInfo(
        val currentLevel: Int,
        val currentLevelTitle: String,
        val totalPoints: Int,
        val pointsForCurrentLevel: Int,
        val pointsForNextLevel: Int,
        val pointsTowardsNextLevel: Int,
        val pointsRemainingForNextLevel: Int,
        val progressPercentage: Int,
        val isMaxLevel: Boolean
    )

    /**
     * Calculates the user's current level based on their total points accumulated.
     *
     * The level is determined by finding the highest threshold the user has exceeded.
     * This method implements a simple linear search through the thresholds array,
     * which is acceptable given the small number of levels (10).
     *
     * Progression Curve: The thresholds increase exponentially to create a satisfying
     * progression that remains challenging throughout the user's journey. Early levels
     * are achievable quickly to provide immediate gratification, whilst later levels
     * require sustained engagement.
     *
     * @param totalPoints The user's cumulative point total.
     * @return Level number from 1 to [MAX_LEVEL].
     *
     * Examples:
     * - 0 points -> Level 1 (Budget Newbie)
     * - 500 points -> Level 2 (Penny Tracker)
     * - 10000 points -> Level 10 (Grandmaster)
     */
    fun calculateLevel(totalPoints: Int): Int {
        return when {
            totalPoints >= LEVEL_THRESHOLDS[9] -> 10  // Grandmaster
            totalPoints >= LEVEL_THRESHOLDS[8] -> 9   // Finance Master
            totalPoints >= LEVEL_THRESHOLDS[7] -> 8   // Savings Specialist
            totalPoints >= LEVEL_THRESHOLDS[6] -> 7   // Budget Expert
            totalPoints >= LEVEL_THRESHOLDS[5] -> 6   // Financial Strategist
            totalPoints >= LEVEL_THRESHOLDS[4] -> 5   // Money Manager
            totalPoints >= LEVEL_THRESHOLDS[3] -> 4   // Budget Apprentice
            totalPoints >= LEVEL_THRESHOLDS[2] -> 3   // Smart Spender
            totalPoints >= LEVEL_THRESHOLDS[1] -> 2   // Penny Tracker
            else -> 1                                 // Budget Newbie
        }
    }

    /**
     * Retrieves the descriptive title for a given level number.
     *
     * Level titles provide meaningful recognition and help users understand their
     * progression status. They're displayed in profile headers, achievement notifications,
     * and progression UI elements.
     *
     * @param level The level number (1 to [MAX_LEVEL]).
     * @return The human-readable title for the level (e.g., "Budget Apprentice").
     *         Returns "Unknown Level" if the level number is invalid.
     */
    fun getLevelTitle(level: Int): String {
        return if (level in 1..MAX_LEVEL) {
            LEVEL_TITLES[level - 1]
        } else {
            Log.w(TAG, "Invalid level requested: $level")
            "Unknown Level"
        }
    }

    /**
     * Calculates comprehensive level progression information for display purposes.
     *
     * This method provides all data needed to render progression UI elements such as:
     * - Level badges and titles
     * - Progress bars showing advancement towards next level
     * - Point counters and remaining requirements
     * - Percentage-based circular progress indicators
     *
     * The calculations handle the edge case of max level appropriately, preventing
     * division by zero and providing sensible values for UI rendering.
     *
     * @param totalPoints The user's cumulative point total.
     * @return [LevelInfo] containing comprehensive progression data.
     */
    fun getLevelInfo(totalPoints: Int): LevelInfo {
        val currentLevel = calculateLevel(totalPoints)
        val currentLevelTitle = getLevelTitle(currentLevel)
        val isMaxLevel = currentLevel >= MAX_LEVEL

        // Determine point thresholds for current and next levels
        val pointsForCurrentLevel = LEVEL_THRESHOLDS[currentLevel - 1]
        val pointsForNextLevel = if (isMaxLevel) {
            pointsForCurrentLevel // At max level, next level threshold equals current
        } else {
            LEVEL_THRESHOLDS[currentLevel]
        }

        // Calculate progress within current level range
        val pointsTowardsNextLevel = totalPoints - pointsForCurrentLevel
        val pointsRemainingForNextLevel = if (isMaxLevel) {
            0
        } else {
            pointsForNextLevel - totalPoints
        }

        // Calculate percentage progress (0-100)
        val progressPercentage = if (isMaxLevel) {
            100
        } else {
            val levelPointRange = pointsForNextLevel - pointsForCurrentLevel
            if (levelPointRange > 0) {
                ((pointsTowardsNextLevel.toFloat() / levelPointRange) * 100).toInt()
            } else {
                0
            }
        }

        return LevelInfo(
            currentLevel = currentLevel,
            currentLevelTitle = currentLevelTitle,
            totalPoints = totalPoints,
            pointsForCurrentLevel = pointsForCurrentLevel,
            pointsForNextLevel = pointsForNextLevel,
            pointsTowardsNextLevel = pointsTowardsNextLevel,
            pointsRemainingForNextLevel = pointsRemainingForNextLevel,
            progressPercentage = progressPercentage,
            isMaxLevel = isMaxLevel
        )
    }

    /**
     * Applies a point award to user progress and recalculates their level.
     *
     * This method returns a new UserProgress instance with updated point total and level.
     * It does not mutate the original progress object, following functional programming
     * principles for safer concurrent access.
     *
     * Level-up Detection: The caller is responsible for detecting level changes by
     * comparing the original and updated progress objects. This allows for triggering
     * level-up animations, notifications, and bonus rewards.
     *
     * @param currentProgress The user's existing progress data.
     * @param pointsToAward The number of points to add (must be positive).
     * @param reason Human-readable description of why points were awarded.
     *               Used for logging and audit trails.
     * @return A new UserProgress instance with updated points and level.
     */
    fun awardPoints(
        currentProgress: UserProgress,
        pointsToAward: Int,
        reason: String
    ): UserProgress {
        require(pointsToAward >= 0) { "Points to award must be non-negative" }

        val newTotalPoints = currentProgress.totalPoints + pointsToAward
        val newLevel = calculateLevel(newTotalPoints)

        Log.d(TAG, "Awarding $pointsToAward points for: $reason")
        Log.d(TAG, "Points: ${currentProgress.totalPoints} -> $newTotalPoints")
        Log.d(TAG, "Level: ${currentProgress.currentLevel} -> $newLevel")

        return currentProgress.copy(
            totalPoints = newTotalPoints,
            currentLevel = newLevel
        )
    }

    /**
     * Determines if awarding points would result in a level increase.
     *
     * This utility method allows preemptive detection of level-ups before actually
     * applying the point award. Useful for triggering animations or special effects
     * that should coincide with the level-up moment.
     *
     * @param currentProgress The user's existing progress data.
     * @param pointsToAward The number of points that would be awarded.
     * @return True if the point award would increase the user's level, false otherwise.
     */
    fun wouldLevelUp(currentProgress: UserProgress, pointsToAward: Int): Boolean {
        val newTotalPoints = currentProgress.totalPoints + pointsToAward
        val newLevel = calculateLevel(newTotalPoints)
        return newLevel > currentProgress.currentLevel
    }
}
