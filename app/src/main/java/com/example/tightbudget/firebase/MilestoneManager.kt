package com.example.tightbudget.firebase

import android.util.Log
import com.example.tightbudget.models.MilestoneCategory
import com.example.tightbudget.models.MilestoneReward
import com.example.tightbudget.models.UserProgress

/**
 * Manages milestone reward definitions, unlock validation, and bonus distribution.
 *
 * This manager handles special milestone rewards that recognise major accomplishments
 * beyond regular achievements. Milestones often combine multiple criteria or represent
 * meta-goals such as "Complete all beginner achievements" or "Reach level 5".
 *
 * Milestones provide extra motivation for long-term engagement and celebrate major
 * progression milestones with significant bonus point rewards.
 *
 * Thread Safety: This class is stateless and thread-safe.
 *
 * @author TightBudget Development Team
 */
class MilestoneManager(
    private val achievementManager: AchievementManager
) {

    companion object {
        private const val TAG = "MilestoneManager"
    }

    /**
     * Result of checking for newly unlocked milestone rewards.
     *
     * @property newlyUnlocked List of milestones that have just been achieved.
     * @property totalBonusPoints Sum of all bonus points from newly unlocked milestones.
     */
    data class MilestoneUnlockResult(
        val newlyUnlocked: List<MilestoneReward>,
        val totalBonusPoints: Int
    )

    /**
     * Provides the complete catalogue of all milestone rewards.
     *
     * Milestones are organized into categories:
     * - LEVEL: Reaching specific user levels
     * - ACHIEVEMENT_TIER: Unlocking all achievements in a tier
     * - STREAK: Extraordinary streak accomplishments
     * - POINTS: Major point thresholds
     * - COMPLETIONIST: 100% completion goals
     * - SPECIAL: Unique one-time feats
     *
     * @return List of all available milestone rewards.
     */
    fun getAllMilestones(): List<MilestoneReward> {
        return listOf(
            // ═══════════════════════════════════════════════════════════════════════════
            // LEVEL MILESTONES - Celebrating progression through the level system
            // ═══════════════════════════════════════════════════════════════════════════
            MilestoneReward(
                id = "milestone_level_5",
                title = "Halfway Hero",
                description = "Reach Level 5 (Money Manager)",
                emoji = "🎖️",
                bonusPoints = 250,
                category = MilestoneCategory.LEVEL
            ),
            MilestoneReward(
                id = "milestone_level_10",
                title = "Ultimate Grandmaster",
                description = "Reach the maximum level (Level 10)",
                emoji = "👑",
                bonusPoints = 1000,
                category = MilestoneCategory.LEVEL
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // ACHIEVEMENT TIER MILESTONES - Complete all achievements in a tier
            // ═══════════════════════════════════════════════════════════════════════════
            MilestoneReward(
                id = "milestone_beginner_complete",
                title = "Beginner Graduate",
                description = "Unlock all Beginner achievements",
                emoji = "🎓",
                bonusPoints = 150,
                category = MilestoneCategory.ACHIEVEMENT_TIER
            ),
            MilestoneReward(
                id = "milestone_intermediate_complete",
                title = "Intermediate Master",
                description = "Unlock all Intermediate achievements",
                emoji = "📜",
                bonusPoints = 300,
                category = MilestoneCategory.ACHIEVEMENT_TIER
            ),
            MilestoneReward(
                id = "milestone_advanced_complete",
                title = "Advanced Champion",
                description = "Unlock all Advanced achievements",
                emoji = "🏅",
                bonusPoints = 500,
                category = MilestoneCategory.ACHIEVEMENT_TIER
            ),
            MilestoneReward(
                id = "milestone_expert_complete",
                title = "Expert Elite",
                description = "Unlock all Expert achievements",
                emoji = "💫",
                bonusPoints = 750,
                category = MilestoneCategory.ACHIEVEMENT_TIER
            ),
            MilestoneReward(
                id = "milestone_legendary_complete",
                title = "Legendary Collector",
                description = "Unlock all Legendary achievements",
                emoji = "🌟",
                bonusPoints = 1500,
                category = MilestoneCategory.ACHIEVEMENT_TIER
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // STREAK MILESTONES - Exceptional consistency achievements
            // ═══════════════════════════════════════════════════════════════════════════
            MilestoneReward(
                id = "milestone_streak_100",
                title = "Centurion",
                description = "Maintain a 100-day streak",
                emoji = "💯",
                bonusPoints = 2000,
                category = MilestoneCategory.STREAK
            ),
            MilestoneReward(
                id = "milestone_streak_365",
                title = "Year-Long Warrior",
                description = "Maintain a 365-day streak",
                emoji = "🗓️",
                bonusPoints = 5000,
                category = MilestoneCategory.STREAK
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // POINTS MILESTONES - Major point accumulation thresholds
            // ═══════════════════════════════════════════════════════════════════════════
            MilestoneReward(
                id = "milestone_points_25k",
                title = "Quarter Century",
                description = "Earn 25,000 total points",
                emoji = "🔥",
                bonusPoints = 1000,
                category = MilestoneCategory.POINTS
            ),
            MilestoneReward(
                id = "milestone_points_50k",
                title = "Half Century Hero",
                description = "Earn 50,000 total points",
                emoji = "💰",
                bonusPoints = 2500,
                category = MilestoneCategory.POINTS
            ),
            MilestoneReward(
                id = "milestone_points_100k",
                title = "Points Overlord",
                description = "Earn 100,000 total points",
                emoji = "👑",
                bonusPoints = 5000,
                category = MilestoneCategory.POINTS
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // COMPLETIONIST MILESTONES - 100% completion goals
            // ═══════════════════════════════════════════════════════════════════════════
            MilestoneReward(
                id = "milestone_all_achievements",
                title = "Achievement Hunter",
                description = "Unlock all 21 achievements",
                emoji = "🏆",
                bonusPoints = 3000,
                category = MilestoneCategory.COMPLETIONIST
            ),
            MilestoneReward(
                id = "milestone_perfect_week",
                title = "Perfect Week",
                description = "Complete all daily challenges for 7 consecutive days",
                emoji = "✨",
                bonusPoints = 500,
                category = MilestoneCategory.COMPLETIONIST
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // SPECIAL MILESTONES - Unique one-time accomplishments
            // ═══════════════════════════════════════════════════════════════════════════
            MilestoneReward(
                id = "milestone_first_day",
                title = "Welcome Bonus",
                description = "Complete your first day of tracking",
                emoji = "🎉",
                bonusPoints = 50,
                category = MilestoneCategory.SPECIAL
            ),
            MilestoneReward(
                id = "milestone_transaction_500",
                title = "Transaction Titan",
                description = "Log 500 transactions",
                emoji = "📊",
                bonusPoints = 800,
                category = MilestoneCategory.SPECIAL
            ),
            MilestoneReward(
                id = "milestone_transaction_1000",
                title = "Transaction Legend",
                description = "Log 1,000 transactions",
                emoji = "📈",
                bonusPoints = 1500,
                category = MilestoneCategory.SPECIAL
            )
        )
    }

    /**
     * Validates whether a user meets the unlock criteria for a specific milestone.
     *
     * This method examines the milestone category and validates against appropriate
     * user statistics. Some milestones require complex validation involving multiple
     * data sources (e.g., checking if all achievements in a tier are unlocked).
     *
     * @param userProgress The user's current progress data.
     * @param milestone The milestone to validate.
     * @return True if the user has achieved this milestone, false otherwise.
     */
    fun meetsUnlockCriteria(userProgress: UserProgress, milestone: MilestoneReward): Boolean {
        return when (milestone.category) {
            MilestoneCategory.LEVEL -> {
                when (milestone.id) {
                    "milestone_level_5" -> userProgress.currentLevel >= 5
                    "milestone_level_10" -> userProgress.currentLevel >= 10
                    else -> false
                }
            }

            MilestoneCategory.ACHIEVEMENT_TIER -> {
                // Check if all achievements in a specific tier are unlocked
                val allAchievements = achievementManager.getAllAchievements()
                val unlockedSet = userProgress.achievementsUnlocked.toSet()

                when (milestone.id) {
                    "milestone_beginner_complete" -> {
                        val beginnerAchievements = achievementManager
                            .getAchievementsByTier(AchievementManager.AchievementTier.BEGINNER)
                        beginnerAchievements.all { unlockedSet.contains(it.id) }
                    }
                    "milestone_intermediate_complete" -> {
                        val intermediateAchievements = achievementManager
                            .getAchievementsByTier(AchievementManager.AchievementTier.INTERMEDIATE)
                        intermediateAchievements.all { unlockedSet.contains(it.id) }
                    }
                    "milestone_advanced_complete" -> {
                        val advancedAchievements = achievementManager
                            .getAchievementsByTier(AchievementManager.AchievementTier.ADVANCED)
                        advancedAchievements.all { unlockedSet.contains(it.id) }
                    }
                    "milestone_expert_complete" -> {
                        val expertAchievements = achievementManager
                            .getAchievementsByTier(AchievementManager.AchievementTier.EXPERT)
                        expertAchievements.all { unlockedSet.contains(it.id) }
                    }
                    "milestone_legendary_complete" -> {
                        val legendaryAchievements = achievementManager
                            .getAchievementsByTier(AchievementManager.AchievementTier.LEGENDARY)
                        legendaryAchievements.all { unlockedSet.contains(it.id) }
                    }
                    else -> false
                }
            }

            MilestoneCategory.STREAK -> {
                when (milestone.id) {
                    "milestone_streak_100" -> userProgress.longestStreak >= 100
                    "milestone_streak_365" -> userProgress.longestStreak >= 365
                    else -> false
                }
            }

            MilestoneCategory.POINTS -> {
                when (milestone.id) {
                    "milestone_points_25k" -> userProgress.totalPoints >= 25000
                    "milestone_points_50k" -> userProgress.totalPoints >= 50000
                    "milestone_points_100k" -> userProgress.totalPoints >= 100000
                    else -> false
                }
            }

            MilestoneCategory.COMPLETIONIST -> {
                when (milestone.id) {
                    "milestone_all_achievements" -> {
                        val totalAchievements = achievementManager.getAllAchievements().size
                        userProgress.achievementsUnlocked.size >= totalAchievements
                    }
                    // Note: "milestone_perfect_week" requires tracking daily challenge completion
                    // which is not yet implemented in UserProgress
                    "milestone_perfect_week" -> false
                    else -> false
                }
            }

            MilestoneCategory.SPECIAL -> {
                when (milestone.id) {
                    "milestone_first_day" -> userProgress.transactionCount >= 1
                    "milestone_transaction_500" -> userProgress.transactionCount >= 500
                    "milestone_transaction_1000" -> userProgress.transactionCount >= 1000
                    else -> false
                }
            }
        }
    }

    /**
     * Identifies all milestones that should be newly unlocked based on current progress.
     *
     * This method scans all milestones and checks each against the user's progress,
     * filtering out already-unlocked milestones to ensure bonuses are only awarded once.
     *
     * Note: Milestone unlock status is not yet persisted in UserProgress. This should
     * be added as a List<String> property similar to achievementsUnlocked.
     *
     * @param userProgress The user's current progress data.
     * @param alreadyUnlockedMilestones Set of milestone IDs already unlocked.
     * @return [MilestoneUnlockResult] containing newly unlocked milestones and total bonuses.
     */
    fun checkForNewUnlocks(
        userProgress: UserProgress,
        alreadyUnlockedMilestones: Set<String>
    ): MilestoneUnlockResult {
        val allMilestones = getAllMilestones()

        Log.d(TAG, "Checking for new milestone unlocks")
        Log.d(TAG, "Total milestones: ${allMilestones.size}")
        Log.d(TAG, "Already unlocked: ${alreadyUnlockedMilestones.size}")

        // Find milestones that meet criteria but haven't been unlocked yet
        val newlyUnlocked = allMilestones.filter { milestone ->
            !alreadyUnlockedMilestones.contains(milestone.id) &&
                    meetsUnlockCriteria(userProgress, milestone)
        }

        // Calculate total bonus points
        val totalBonusPoints = newlyUnlocked.sumOf { it.bonusPoints }

        if (newlyUnlocked.isNotEmpty()) {
            Log.d(TAG, "🎉 ${newlyUnlocked.size} new milestone(s) achieved!")
            newlyUnlocked.forEach { milestone ->
                Log.d(TAG, "  - ${milestone.title} (+${milestone.bonusPoints} points)")
            }
        } else {
            Log.d(TAG, "No new milestones unlocked")
        }

        return MilestoneUnlockResult(
            newlyUnlocked = newlyUnlocked,
            totalBonusPoints = totalBonusPoints
        )
    }

    /**
     * Filters milestones by category for organised display.
     *
     * @param category The [MilestoneCategory] to filter by.
     * @return List of milestones in the specified category.
     */
    fun getMilestonesByCategory(category: MilestoneCategory): List<MilestoneReward> {
        return getAllMilestones().filter { it.category == category }
    }

    /**
     * Calculates the user's overall milestone completion percentage.
     *
     * @param unlockedMilestones Set of unlocked milestone IDs.
     * @return Percentage (0-100) of milestones unlocked.
     */
    fun getCompletionPercentage(unlockedMilestones: Set<String>): Int {
        val totalMilestones = getAllMilestones().size
        if (totalMilestones == 0) return 0

        val percentage = (unlockedMilestones.size.toFloat() / totalMilestones * 100).toInt()
        return percentage.coerceIn(0, 100)
    }
}
