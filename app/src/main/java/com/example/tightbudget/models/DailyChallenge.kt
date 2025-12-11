package com.example.tightbudget.models

/**
 * Represents a time-limited daily challenge that encourages specific user behaviours.
 *
 * Daily challenges provide short-term, focused objectives that reset every 24 hours,
 * maintaining user engagement through varied, achievable goals. Each challenge awards
 * points upon completion and tracks real-time progress against the target value.
 *
 * Challenges expire at midnight (23:59:59) on the day they are assigned, creating
 * urgency and encouraging daily application usage.
 *
 * @property id Unique identifier combining the challenge type and assignment timestamp.
 *              Format: "daily_{type}_{timestamp}". Used as the primary key in Firebase
 *              and ensures challenges don't duplicate across multiple generations.
 *
 * @property title Short, action-oriented name displayed prominently in the UI.
 *                 Should clearly indicate the challenge objective (e.g., "Transaction Logger").
 *
 * @property description Detailed explanation of what the user must accomplish to complete
 *                       the challenge. Displayed as supporting text beneath the title.
 *                       Should include specific numeric targets when applicable.
 *
 * @property pointsReward Points awarded immediately upon challenge completion.
 *                        Higher-difficulty challenges offer greater rewards to
 *                        incentivise effort. Points are awarded exactly once per challenge.
 *
 * @property type The [ChallengeType] category determining what user action is being measured.
 *                Types include TRANSACTION, RECEIPT, BUDGET_COMPLIANCE, STREAK, etc.
 *                Defines how progress is calculated and tracked.
 *
 * @property targetValue The numeric goal the user must reach to complete the challenge.
 *                       Interpretation varies by [type]:
 *                       - TRANSACTION: Number of transactions to add
 *                       - RECEIPT: Number of receipts to upload
 *                       - BUDGET_COMPLIANCE: Binary (1 = stayed within budget)
 *                       - SAVINGS: Percentage saved compared to previous day
 *
 * @property currentProgress Real-time counter tracking user progress toward [targetValue].
 *                           Updated automatically as relevant user actions occur.
 *                           Always ranges from 0 to [targetValue] (capped at target).
 *
 * @property isCompleted Flag indicating whether the challenge has been fully completed.
 *                       Set to true when [currentProgress] reaches [targetValue].
 *                       Once true, prevents duplicate point awards and enables
 *                       completion UI styling.
 *
 * @property dateAssigned Timestamp (milliseconds since epoch) when the challenge was
 *                        generated and assigned to the user. Used to filter active
 *                        challenges and verify challenges belong to the current day.
 *
 * @property expiresAt Timestamp (milliseconds since epoch) when the challenge becomes invalid.
 *                     Set to 23:59:59 on the assignment day. After expiration, the
 *                     challenge can no longer be completed and is replaced during the
 *                     next generation cycle.
 *
 * @author TightBudget Development Team
 */
data class DailyChallenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val pointsReward: Int = 0,
    val type: ChallengeType = ChallengeType.TRANSACTION,
    val targetValue: Int = 0,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val dateAssigned: Long = 0L,
    val expiresAt: Long = 0L
) {
    /**
     * No-argument constructor required by Firebase Realtime Database for
     * automatic object deserialisation. Delegates to primary constructor
     * with default values for all properties.
     */
    constructor() : this("", "", "", 0, ChallengeType.TRANSACTION, 0, 0, false, 0L, 0L)

    /**
     * Calculates the completion percentage for visual progress indicators.
     *
     * This method is used extensively throughout the UI to display progress bars,
     * radial progress indicators, and percentage labels. The result is clamped
     * to a maximum of 100% even if progress exceeds the target (edge case handling).
     *
     * @return Percentage value from 0 to 100 representing progress toward completion.
     *         Returns 0 if [targetValue] is invalid (zero or negative).
     */
    fun getProgressPercentage(): Int {
        return if (targetValue > 0) {
            minOf(100, (currentProgress * 100) / targetValue)
        } else 0
    }

    /**
     * Generates a formatted string displaying progress in "current/target" format.
     *
     * Provides a clear, concise representation of challenge progress suitable for
     * UI labels and accessibility descriptions. Example output: "2/5" indicates
     * 2 units completed out of a target of 5.
     *
     * @return Formatted progress string (e.g., "3/10", "1/3", "5/5").
     */
    fun getProgressText(): String {
        return "$currentProgress/$targetValue"
    }
}