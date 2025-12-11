package com.example.tightbudget.models

/**
 * Represents a user achievement or badge within the gamification system.
 *
 * Achievements provide long-term goals that encourage specific behaviours and
 * reward sustained engagement with the application. Each achievement has unique
 * unlock criteria based on its [type] and [targetValue].
 *
 * When unlocked, achievements award bonus points and provide visual recognition
 * through badges displayed in the user's profile.
 *
 * @property id Unique identifier for this achievement. Used as the primary key
 *              in Firebase and for tracking unlock status. Must remain constant
 *              across app versions to maintain user progress.
 *
 * @property title Short, user-facing name displayed in the achievements list.
 *                 Should be concise (2-4 words) and descriptive of the goal.
 *
 * @property description Detailed explanation of what the user must do to unlock
 *                       this achievement. Displayed in achievement details and
 *                       tooltips to guide user behaviour.
 *
 * @property emoji Visual icon representing this achievement. Uses Unicode emoji
 *                 for consistency across platforms. Should relate thematically
 *                 to the achievement's purpose.
 *
 * @property pointsRequired Bonus points awarded when this achievement is unlocked.
 *                          Higher values indicate more challenging or prestigious
 *                          achievements. Points are awarded exactly once upon unlock.
 *
 * @property type The [AchievementType] category determining how progress is measured.
 *                Types include POINTS, STREAK, TRANSACTIONS, RECEIPTS, etc.
 *                See [AchievementType] for all available categories.
 *
 * @property targetValue The threshold value required to unlock this achievement.
 *                       Interpretation depends on [type]:
 *                       - POINTS: Total points accumulated
 *                       - TRANSACTIONS: Number of transactions logged
 *                       - STREAK: Consecutive days of activity
 *                       - RECEIPTS: Number of receipts uploaded
 *
 * @property isUnlocked Whether this achievement has been unlocked by the user.
 *                      Once true, this flag prevents duplicate point awards and
 *                      enables distinct visual styling in the UI.
 *
 * @property unlockedDate Timestamp (milliseconds since epoch) when this achievement
 *                        was unlocked. Null if not yet unlocked. Used for displaying
 *                        achievement history and calculating time-based statistics.
 *
 * @author TightBudget Development Team
 */
data class Achievement(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var emoji: String = "",
    var pointsRequired: Int = 0,
    var type: AchievementType = AchievementType.POINTS,
    var targetValue: Int = 0,
    var isUnlocked: Boolean = false,
    var unlockedDate: Long? = null
) {
    /**
     * No-argument constructor required by Firebase Realtime Database for
     * automatic object deserialisation. Delegates to primary constructor
     * with default values for all properties.
     */
    constructor() : this("", "", "", "", 0, AchievementType.POINTS, 0, false, null)
}