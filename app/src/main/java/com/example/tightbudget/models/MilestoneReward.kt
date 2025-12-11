package com.example.tightbudget.models

/**
 * Represents a special reward granted upon reaching significant gamification milestones.
 *
 * Milestone rewards provide extra recognition and bonus points for major achievements
 * such as reaching specific level thresholds, unlocking all achievements in a tier,
 * or completing extraordinary feats.
 *
 * These rewards differ from regular achievements in that they often combine multiple
 * criteria or represent meta-accomplishments (e.g., "Unlock all beginner achievements").
 *
 * @property id Unique identifier for this milestone reward.
 * @property title Short, celebratory name for the milestone.
 * @property description Detailed explanation of what was accomplished.
 * @property emoji Visual icon representing the milestone's significance.
 * @property bonusPoints Extra points awarded for reaching this milestone.
 *                      Typically larger than regular achievement bonuses.
 * @property category The [MilestoneCategory] this reward belongs to.
 * @property isUnlocked Whether the user has achieved this milestone.
 * @property unlockedDate Timestamp when the milestone was reached, or null if not yet unlocked.
 *
 * @author TightBudget Development Team
 */
data class MilestoneReward(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val emoji: String = "",
    val bonusPoints: Int = 0,
    val category: MilestoneCategory = MilestoneCategory.LEVEL,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long? = null
) {
    /**
     * No-argument constructor required by Firebase Realtime Database.
     */
    constructor() : this("", "", "", "", 0, MilestoneCategory.LEVEL, false, null)
}

/**
 * Categories of milestone rewards based on the type of accomplishment.
 *
 * @property LEVEL Milestones for reaching specific user levels.
 * @property ACHIEVEMENT_TIER Milestones for unlocking all achievements in a difficulty tier.
 * @property STREAK Milestones for extraordinary streak accomplishments.
 * @property POINTS Milestones for reaching major point thresholds.
 * @property COMPLETIONIST Milestones for 100% completion goals.
 * @property SPECIAL Unique, one-time milestones for exceptional feats.
 */
enum class MilestoneCategory {
    LEVEL,
    ACHIEVEMENT_TIER,
    STREAK,
    POINTS,
    COMPLETIONIST,
    SPECIAL
}
