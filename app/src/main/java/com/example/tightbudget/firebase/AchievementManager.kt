package com.example.tightbudget.firebase

import android.util.Log
import com.example.tightbudget.models.Achievement
import com.example.tightbudget.models.AchievementType
import com.example.tightbudget.models.UserProgress

/**
 * Manages achievement definitions, unlock validation, and progression tracking.
 *
 * This manager centralises all achievement-related logic, including:
 * - Providing the master list of all available achievements
 * - Validating unlock criteria against user progress
 * - Identifying newly unlocked achievements
 * - Categorising achievements by difficulty tier
 * - Managing achievement metadata (emojis, descriptions, point rewards)
 *
 * Achievements provide long-term goals that encourage specific behaviours and reward
 * sustained engagement. They are displayed in the UI as locked or unlocked badges,
 * with progress indicators showing advancement towards unlock criteria.
 *
 * Design Pattern: This class uses a repository pattern, maintaining the definitive
 * achievement catalogue whilst delegating persistence to the GamificationManager.
 *
 * Thread Safety: This class is stateless and thread-safe. All achievement lists are
 * generated fresh on each call to prevent shared mutable state.
 *
 * @author TightBudget Development Team
 */
class AchievementManager {

    companion object {
        private const val TAG = "AchievementManager"
    }

    /**
     * Difficulty tier categories for achievement classification.
     * Used for UI filtering and progressive disclosure of goals.
     */
    enum class AchievementTier {
        BEGINNER,       // Early, easily achievable goals for new users
        INTERMEDIATE,   // Mid-range goals requiring regular engagement
        ADVANCED,       // Challenging goals for dedicated users
        EXPERT,         // Highly challenging goals requiring commitment
        LEGENDARY       // Ultimate goals representing mastery
    }

    /**
     * Represents the result of checking for newly unlocked achievements.
     *
     * @property newlyUnlocked List of achievements that have just been unlocked.
     *                        Empty if no new achievements were earned.
     * @property totalBonusPoints Sum of all point rewards from newly unlocked achievements.
     */
    data class UnlockResult(
        val newlyUnlocked: List<Achievement>,
        val totalBonusPoints: Int
    )

    /**
     * Provides the complete master list of all available achievements.
     *
     * This method generates a fresh list on each call to ensure immutability and
     * prevent accidental state mutation. Each achievement is defined with:
     * - Unique persistent ID for tracking
     * - User-facing title and description
     * - Visual emoji icon
     * - Unlock criteria (type and target value)
     * - Bonus points awarded upon unlock
     *
     * Achievement IDs must remain stable across app versions to maintain user progress.
     * Never change an achievement's ID after it has been released to users.
     *
     * The achievements are organized into five difficulty tiers:
     * - BEGINNER: 3 achievements (first transaction, 5 transactions, first receipt)
     * - INTERMEDIATE: 5 achievements (25 transactions, 10 receipts, short streaks, 500 points)
     * - ADVANCED: 5 achievements (50 transactions, 25 receipts, 14-day streak, 1500 points, 10 categories)
     * - EXPERT: 4 achievements (100 transactions, 50 receipts, 30-day streak, 5000 points)
     * - LEGENDARY: 4 achievements (250 transactions, 100 receipts, 60-day streak, 10000 points)
     *
     * @return Immutable list of all 21 achievements available in the system.
     */
    fun getAllAchievements(): List<Achievement> {
        return listOf(
            // ═══════════════════════════════════════════════════════════════════════════
            // BEGINNER TIER - Early goals to encourage initial engagement
            // ═══════════════════════════════════════════════════════════════════════════
            Achievement(
                id = "first_transaction",
                title = "First Steps",
                description = "Add your first transaction",
                emoji = "🎯",
                pointsRequired = 50,
                type = AchievementType.TRANSACTIONS,
                targetValue = 1
            ),
            Achievement(
                id = "transactions_5",
                title = "Getting Started",
                description = "Add 5 transactions",
                emoji = "📝",
                pointsRequired = 75,
                type = AchievementType.TRANSACTIONS,
                targetValue = 5
            ),
            Achievement(
                id = "first_receipt",
                title = "Receipt Rookie",
                description = "Upload your first receipt",
                emoji = "📄",
                pointsRequired = 40,
                type = AchievementType.RECEIPTS,
                targetValue = 1
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // INTERMEDIATE TIER - Goals requiring regular engagement
            // ═══════════════════════════════════════════════════════════════════════════
            Achievement(
                id = "transactions_25",
                title = "Transaction Pro",
                description = "Add 25 transactions",
                emoji = "💼",
                pointsRequired = 150,
                type = AchievementType.TRANSACTIONS,
                targetValue = 25
            ),
            Achievement(
                id = "receipts_10",
                title = "Receipt Collector",
                description = "Upload 10 receipts",
                emoji = "📋",
                pointsRequired = 200,
                type = AchievementType.RECEIPTS,
                targetValue = 10
            ),
            Achievement(
                id = "streak_3",
                title = "Three Day Hero",
                description = "Log transactions for 3 days straight",
                emoji = "🔥",
                pointsRequired = 100,
                type = AchievementType.STREAK,
                targetValue = 3
            ),
            Achievement(
                id = "streak_7",
                title = "Week Warrior",
                description = "Log transactions for 7 days straight",
                emoji = "⚡",
                pointsRequired = 200,
                type = AchievementType.STREAK,
                targetValue = 7
            ),
            Achievement(
                id = "points_500",
                title = "Point Collector",
                description = "Earn 500 total points",
                emoji = "⭐",
                pointsRequired = 100,
                type = AchievementType.POINTS,
                targetValue = 500
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // ADVANCED TIER - Challenging goals for dedicated users
            // ═══════════════════════════════════════════════════════════════════════════
            Achievement(
                id = "transactions_50",
                title = "Budget Master",
                description = "Add 50 transactions",
                emoji = "👑",
                pointsRequired = 300,
                type = AchievementType.TRANSACTIONS,
                targetValue = 50
            ),
            Achievement(
                id = "receipts_25",
                title = "Receipt Master",
                description = "Upload 25 receipts",
                emoji = "🗂️",
                pointsRequired = 400,
                type = AchievementType.RECEIPTS,
                targetValue = 25
            ),
            Achievement(
                id = "streak_14",
                title = "Two Week Champion",
                description = "Log transactions for 14 days straight",
                emoji = "🏆",
                pointsRequired = 500,
                type = AchievementType.STREAK,
                targetValue = 14
            ),
            Achievement(
                id = "points_1500",
                title = "Point Millionaire",
                description = "Earn 1500 total points",
                emoji = "💎",
                pointsRequired = 300,
                type = AchievementType.POINTS,
                targetValue = 1500
            ),
            Achievement(
                id = "categories_10",
                title = "Category Explorer",
                description = "Use 10 different categories",
                emoji = "🗺️",
                pointsRequired = 250,
                type = AchievementType.CATEGORIES,
                targetValue = 10
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // EXPERT TIER - Highly challenging goals requiring commitment
            // ═══════════════════════════════════════════════════════════════════════════
            Achievement(
                id = "transactions_100",
                title = "Transaction Legend",
                description = "Add 100 transactions",
                emoji = "🌟",
                pointsRequired = 600,
                type = AchievementType.TRANSACTIONS,
                targetValue = 100
            ),
            Achievement(
                id = "receipts_50",
                title = "Receipt Royalty",
                description = "Upload 50 receipts",
                emoji = "👑",
                pointsRequired = 800,
                type = AchievementType.RECEIPTS,
                targetValue = 50
            ),
            Achievement(
                id = "streak_30",
                title = "Month Master",
                description = "Log transactions for 30 days straight",
                emoji = "🚀",
                pointsRequired = 1000,
                type = AchievementType.STREAK,
                targetValue = 30
            ),
            Achievement(
                id = "points_5000",
                title = "Point Grandmaster",
                description = "Earn 5000 total points",
                emoji = "💫",
                pointsRequired = 1000,
                type = AchievementType.POINTS,
                targetValue = 5000
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // LEGENDARY TIER - Ultimate goals representing mastery
            // ═══════════════════════════════════════════════════════════════════════════
            Achievement(
                id = "transactions_250",
                title = "Budget Grandmaster",
                description = "Add 250 transactions",
                emoji = "🏅",
                pointsRequired = 1500,
                type = AchievementType.TRANSACTIONS,
                targetValue = 250
            ),
            Achievement(
                id = "receipts_100",
                title = "Receipt Emperor",
                description = "Upload 100 receipts",
                emoji = "🎖️",
                pointsRequired = 2000,
                type = AchievementType.RECEIPTS,
                targetValue = 100
            ),
            Achievement(
                id = "streak_60",
                title = "Two Month Legend",
                description = "Log transactions for 60 days straight",
                emoji = "🔮",
                pointsRequired = 2500,
                type = AchievementType.STREAK,
                targetValue = 60
            ),
            Achievement(
                id = "points_10000",
                title = "Financial Deity",
                description = "Earn 10000 total points",
                emoji = "✨",
                pointsRequired = 2000,
                type = AchievementType.POINTS,
                targetValue = 10000
            )
        )
    }

    /**
     * Validates whether a user meets the unlock criteria for a specific achievement.
     *
     * This method examines the achievement type and compares the user's relevant statistic
     * against the achievement's target value. Different achievement types require different
     * validation logic:
     *
     * - POINTS: Compares totalPoints against targetValue
     * - TRANSACTIONS: Compares transactionCount against targetValue
     * - RECEIPTS: Compares receiptsUploaded against targetValue
     * - STREAK: Compares longestStreak against targetValue
     * - BUDGET_GOALS: Compares budgetGoalsMet against targetValue
     * - CATEGORIES: Would compare unique categories used (not yet fully implemented)
     * - SAVINGS: Would compare cumulative savings (not yet fully implemented)
     *
     * Note: Some achievement types are defined but not fully implemented in the validation
     * logic. These will return false until the necessary tracking is added.
     *
     * @param userProgress The user's current progress and statistics.
     * @param achievement The achievement to validate.
     * @return True if the user meets the unlock criteria, false otherwise.
     */
    fun meetsUnlockCriteria(userProgress: UserProgress, achievement: Achievement): Boolean {
        val meets = when (achievement.type) {
            AchievementType.POINTS -> {
                userProgress.totalPoints >= achievement.targetValue
            }
            AchievementType.TRANSACTIONS -> {
                userProgress.transactionCount >= achievement.targetValue
            }
            AchievementType.RECEIPTS -> {
                userProgress.receiptsUploaded >= achievement.targetValue
            }
            AchievementType.STREAK -> {
                userProgress.longestStreak >= achievement.targetValue
            }
            AchievementType.BUDGET_GOALS -> {
                userProgress.budgetGoalsMet >= achievement.targetValue
            }
            AchievementType.CATEGORIES -> {
                // Category tracking not yet fully implemented
                // Would require counting unique spending categories used
                Log.d(TAG, "Category achievement validation not yet implemented")
                false
            }
            AchievementType.SAVINGS -> {
                // Savings tracking not yet fully implemented
                // Would require tracking cumulative amount saved
                Log.d(TAG, "Savings achievement validation not yet implemented")
                false
            }
        }

        Log.d(TAG, "Achievement ${achievement.id}: meets criteria = $meets")
        return meets
    }

    /**
     * Identifies all achievements that should be newly unlocked based on current progress.
     *
     * This method performs a comprehensive scan of all achievements, checking each one
     * against the user's progress. It filters out already-unlocked achievements to ensure
     * bonuses are only awarded once.
     *
     * The method also calculates the total bonus points that should be awarded from all
     * newly unlocked achievements, allowing the calling code to make a single point update.
     *
     * Algorithm:
     * 1. Retrieve all defined achievements
     * 2. Filter out already-unlocked achievements using user's unlock list
     * 3. Validate remaining achievements against current progress
     * 4. Sum bonus points from newly unlocked achievements
     * 5. Return results for processing
     *
     * @param userProgress The user's current progress and statistics.
     * @return [UnlockResult] containing newly unlocked achievements and total bonus points.
     */
    fun checkForNewUnlocks(userProgress: UserProgress): UnlockResult {
        val allAchievements = getAllAchievements()
        val alreadyUnlocked = userProgress.achievementsUnlocked.toSet()

        Log.d(TAG, "Checking for new achievement unlocks")
        Log.d(TAG, "Total achievements: ${allAchievements.size}")
        Log.d(TAG, "Already unlocked: ${alreadyUnlocked.size}")

        // Find achievements that meet criteria but haven't been unlocked yet
        val newlyUnlocked = allAchievements.filter { achievement ->
            !alreadyUnlocked.contains(achievement.id) &&
                    meetsUnlockCriteria(userProgress, achievement)
        }

        // Calculate total bonus points from all newly unlocked achievements
        val totalBonusPoints = newlyUnlocked.sumOf { it.pointsRequired }

        if (newlyUnlocked.isNotEmpty()) {
            Log.d(TAG, "🏆 ${newlyUnlocked.size} new achievement(s) unlocked!")
            newlyUnlocked.forEach { achievement ->
                Log.d(TAG, "  - ${achievement.title} (+${achievement.pointsRequired} points)")
            }
        } else {
            Log.d(TAG, "No new achievements unlocked")
        }

        return UnlockResult(
            newlyUnlocked = newlyUnlocked,
            totalBonusPoints = totalBonusPoints
        )
    }

    /**
     * Categorises an achievement into its difficulty tier for UI organisation.
     *
     * This method examines the achievement's target value and type to determine its
     * appropriate difficulty classification. This enables:
     * - Progressive disclosure (showing easier achievements first to new users)
     * - Filtering UI by difficulty level
     * - Achievement progression paths (complete beginner before seeing expert goals)
     *
     * Tier classification is based on target value thresholds for each achievement type.
     *
     * @param achievement The achievement to categorise.
     * @return The [AchievementTier] this achievement belongs to.
     */
    fun getAchievementTier(achievement: Achievement): AchievementTier {
        return when (achievement.type) {
            AchievementType.TRANSACTIONS -> when {
                achievement.targetValue <= 5 -> AchievementTier.BEGINNER
                achievement.targetValue <= 25 -> AchievementTier.INTERMEDIATE
                achievement.targetValue <= 50 -> AchievementTier.ADVANCED
                achievement.targetValue <= 100 -> AchievementTier.EXPERT
                else -> AchievementTier.LEGENDARY
            }
            AchievementType.RECEIPTS -> when {
                achievement.targetValue <= 1 -> AchievementTier.BEGINNER
                achievement.targetValue <= 10 -> AchievementTier.INTERMEDIATE
                achievement.targetValue <= 25 -> AchievementTier.ADVANCED
                achievement.targetValue <= 50 -> AchievementTier.EXPERT
                else -> AchievementTier.LEGENDARY
            }
            AchievementType.STREAK -> when {
                achievement.targetValue <= 3 -> AchievementTier.INTERMEDIATE
                achievement.targetValue <= 14 -> AchievementTier.ADVANCED
                achievement.targetValue <= 30 -> AchievementTier.EXPERT
                else -> AchievementTier.LEGENDARY
            }
            AchievementType.POINTS -> when {
                achievement.targetValue <= 500 -> AchievementTier.INTERMEDIATE
                achievement.targetValue <= 1500 -> AchievementTier.ADVANCED
                achievement.targetValue <= 5000 -> AchievementTier.EXPERT
                else -> AchievementTier.LEGENDARY
            }
            else -> AchievementTier.INTERMEDIATE
        }
    }

    /**
     * Filters the master achievement list to only those belonging to a specific tier.
     *
     * This is useful for progressive UI disclosure and difficulty-based filtering,
     * allowing new users to focus on achievable goals whilst giving experienced users
     * visibility of ultimate challenges.
     *
     * @param tier The [AchievementTier] to filter by.
     * @return List of achievements belonging to the specified tier.
     */
    fun getAchievementsByTier(tier: AchievementTier): List<Achievement> {
        return getAllAchievements().filter { achievement ->
            getAchievementTier(achievement) == tier
        }
    }
}
