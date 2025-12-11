package com.example.tightbudget.firebase

import android.util.Log
import com.example.tightbudget.models.ChallengeType
import com.example.tightbudget.models.DailyChallenge
import java.util.Calendar
import kotlin.random.Random

/**
 * Manages daily challenge generation, rotation, and variety within the gamification system.
 *
 * This manager handles all challenge-related logic, including:
 * - Generating varied daily challenges from a diverse pool
 * - Rotating challenges to prevent repetition and maintain freshness
 * - Balancing difficulty and point rewards
 * - Calculating time-based expiration
 * - Providing challenge templates with appropriate target values
 *
 * Daily challenges provide short-term, focused objectives that reset every 24 hours,
 * maintaining engagement through achievable, time-limited goals. The variety in
 * challenge types encourages diverse behaviours and prevents routine staleness.
 *
 * Design Philosophy: Challenges should be:
 * - Achievable within a single day by active users
 * - Varied enough to avoid predictability
 * - Progressively more valuable for more difficult objectives
 * - Thematically aligned with financial responsibility
 *
 * Thread Safety: This class is stateless and thread-safe. All challenge generation
 * uses immutable templates and random selection without shared mutable state.
 *
 * @author TightBudget Development Team
 */
class ChallengeManager {

    companion object {
        private const val TAG = "ChallengeManager"

        /**
         * Number of challenges to generate per day.
         * This provides variety whilst remaining manageable.
         */
        private const val DAILY_CHALLENGE_COUNT = 3
    }

    /**
     * Template for challenge generation containing reusable challenge definitions.
     * Each template includes all properties needed to instantiate a DailyChallenge,
     * except for timestamps which are calculated at generation time.
     */
    private data class ChallengeTemplate(
        val title: String,
        val description: String,
        val pointsReward: Int,
        val type: ChallengeType,
        val targetValue: Int,
        val difficulty: ChallengeDifficulty
    )

    /**
     * Difficulty classification for challenges, used for balancing and selection.
     */
    enum class ChallengeDifficulty {
        EASY,       // Simple, quickly achievable goals (50-75 points)
        MEDIUM,     // Moderate goals requiring some effort (75-100 points)
        HARD        // Challenging goals requiring dedication (100-150 points)
    }

    /**
     * Comprehensive pool of challenge templates providing variety and diversity.
     *
     * This pool contains 18 different challenge types across all difficulty levels
     * and challenge categories. Challenges are randomly selected from this pool daily
     * to ensure users experience fresh objectives regularly.
     *
     * Challenge Design Guidelines:
     * - EASY challenges: Completable with minimal effort by any active user
     * - MEDIUM challenges: Require deliberate focus but achievable in one day
     * - HARD challenges: Stretch goals requiring significant commitment
     *
     * Point Rewards Scale:
     * - Easy: 50-75 points
     * - Medium: 75-100 points
     * - Hard: 100-150 points
     */
    private fun getChallengePool(): List<ChallengeTemplate> {
        return listOf(
            // ═══════════════════════════════════════════════════════════════════════════
            // TRANSACTION CHALLENGES - Encourage regular expense logging
            // ═══════════════════════════════════════════════════════════════════════════
            ChallengeTemplate(
                title = "Transaction Logger",
                description = "Add 3 transactions today",
                pointsReward = 60,
                type = ChallengeType.TRANSACTION,
                targetValue = 3,
                difficulty = ChallengeDifficulty.EASY
            ),
            ChallengeTemplate(
                title = "Diligent Tracker",
                description = "Add 5 transactions today",
                pointsReward = 85,
                type = ChallengeType.TRANSACTION,
                targetValue = 5,
                difficulty = ChallengeDifficulty.MEDIUM
            ),
            ChallengeTemplate(
                title = "Super Tracker",
                description = "Add 7 transactions today",
                pointsReward = 120,
                type = ChallengeType.TRANSACTION,
                targetValue = 7,
                difficulty = ChallengeDifficulty.HARD
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // RECEIPT CHALLENGES - Encourage documentation and record-keeping
            // ═══════════════════════════════════════════════════════════════════════════
            ChallengeTemplate(
                title = "Receipt Collector",
                description = "Upload 2 receipts today",
                pointsReward = 70,
                type = ChallengeType.RECEIPT,
                targetValue = 2,
                difficulty = ChallengeDifficulty.EASY
            ),
            ChallengeTemplate(
                title = "Documentation Master",
                description = "Upload 3 receipts today",
                pointsReward = 95,
                type = ChallengeType.RECEIPT,
                targetValue = 3,
                difficulty = ChallengeDifficulty.MEDIUM
            ),
            ChallengeTemplate(
                title = "Paperwork Champion",
                description = "Upload 5 receipts today",
                pointsReward = 130,
                type = ChallengeType.RECEIPT,
                targetValue = 5,
                difficulty = ChallengeDifficulty.HARD
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // BUDGET COMPLIANCE CHALLENGES - Encourage fiscal discipline
            // ═══════════════════════════════════════════════════════════════════════════
            ChallengeTemplate(
                title = "Smart Spender",
                description = "Keep expenses under budget today",
                pointsReward = 100,
                type = ChallengeType.BUDGET_COMPLIANCE,
                targetValue = 1,
                difficulty = ChallengeDifficulty.MEDIUM
            ),
            ChallengeTemplate(
                title = "Frugal Friday",
                description = "Spend less than £20 today",
                pointsReward = 110,
                type = ChallengeType.BUDGET_COMPLIANCE,
                targetValue = 1,
                difficulty = ChallengeDifficulty.MEDIUM
            ),
            ChallengeTemplate(
                title = "Zero Spend Challenge",
                description = "Log no expenses today (income only)",
                pointsReward = 150,
                type = ChallengeType.BUDGET_COMPLIANCE,
                targetValue = 1,
                difficulty = ChallengeDifficulty.HARD
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // STREAK CHALLENGES - Encourage consistent daily engagement
            // ═══════════════════════════════════════════════════════════════════════════
            ChallengeTemplate(
                title = "Streak Keeper",
                description = "Maintain your current streak",
                pointsReward = 75,
                type = ChallengeType.STREAK,
                targetValue = 1,
                difficulty = ChallengeDifficulty.EASY
            ),
            ChallengeTemplate(
                title = "Consistency Champion",
                description = "Reach a 5-day streak",
                pointsReward = 100,
                type = ChallengeType.STREAK,
                targetValue = 5,
                difficulty = ChallengeDifficulty.MEDIUM
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // SAVINGS CHALLENGES - Encourage spending reduction
            // ═══════════════════════════════════════════════════════════════════════════
            ChallengeTemplate(
                title = "Thrifty Thursday",
                description = "Spend less today than yesterday",
                pointsReward = 90,
                type = ChallengeType.SAVINGS,
                targetValue = 1,
                difficulty = ChallengeDifficulty.MEDIUM
            ),
            ChallengeTemplate(
                title = "Major Saver",
                description = "Spend 50% less than yesterday",
                pointsReward = 140,
                type = ChallengeType.SAVINGS,
                targetValue = 50,
                difficulty = ChallengeDifficulty.HARD
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // CATEGORY CHALLENGES - Encourage comprehensive tracking
            // ═══════════════════════════════════════════════════════════════════════════
            ChallengeTemplate(
                title = "Category Conscious",
                description = "Log expenses in 3 different categories",
                pointsReward = 80,
                type = ChallengeType.CATEGORY_LIMIT,
                targetValue = 3,
                difficulty = ChallengeDifficulty.MEDIUM
            ),
            ChallengeTemplate(
                title = "Diverse Spender",
                description = "Use 5 different spending categories",
                pointsReward = 110,
                type = ChallengeType.CATEGORY_LIMIT,
                targetValue = 5,
                difficulty = ChallengeDifficulty.HARD
            ),

            // ═══════════════════════════════════════════════════════════════════════════
            // COMBINED/SPECIAL CHALLENGES - Multi-faceted objectives
            // ═══════════════════════════════════════════════════════════════════════════
            ChallengeTemplate(
                title = "Perfect Day",
                description = "Add 3 transactions all with receipts",
                pointsReward = 120,
                type = ChallengeType.RECEIPT,
                targetValue = 3,
                difficulty = ChallengeDifficulty.HARD
            ),
            ChallengeTemplate(
                title = "Morning Motivator",
                description = "Log your first transaction before noon",
                pointsReward = 50,
                type = ChallengeType.TRANSACTION,
                targetValue = 1,
                difficulty = ChallengeDifficulty.EASY
            ),
            ChallengeTemplate(
                title = "Complete Tracker",
                description = "Add receipts to all transactions today",
                pointsReward = 100,
                type = ChallengeType.RECEIPT,
                targetValue = 2,
                difficulty = ChallengeDifficulty.MEDIUM
            )
        )
    }

    /**
     * Generates a set of daily challenges for the current day.
     *
     * This method selects [DAILY_CHALLENGE_COUNT] challenges from the template pool,
     * ensuring variety and appropriate difficulty balance. The selection algorithm:
     *
     * 1. Retrieves the full challenge pool
     * 2. Randomly shuffles to prevent predictable patterns
     * 3. Selects the first N challenges (where N = DAILY_CHALLENGE_COUNT)
     * 4. Instantiates each template with current timestamps
     * 5. Returns the list of active challenges
     *
     * Challenges expire at 23:59:59 on the day they are generated, creating urgency
     * and encouraging daily engagement.
     *
     * Future Enhancement: Could implement difficulty balancing to ensure each day
     * has a mix of easy, medium, and hard challenges rather than random selection.
     *
     * @return List of [DAILY_CHALLENGE_COUNT] newly generated daily challenges.
     */
    fun generateDailyChallenges(): List<DailyChallenge> {
        val currentTime = System.currentTimeMillis()
        val endOfDay = getEndOfDayTimestamp()

        // Shuffle the pool to provide randomised variety
        val pool = getChallengePool().shuffled()

        // Select the first N challenges from the shuffled pool
        val selectedTemplates = pool.take(DAILY_CHALLENGE_COUNT)

        Log.d(TAG, "Generating $DAILY_CHALLENGE_COUNT daily challenges")

        // Instantiate each template as a concrete DailyChallenge
        val challenges = selectedTemplates.mapIndexed { index, template ->
            val challenge = DailyChallenge(
                id = "daily_${template.type}_${currentTime}_$index",
                title = template.title,
                description = template.description,
                pointsReward = template.pointsReward,
                type = template.type,
                targetValue = template.targetValue,
                currentProgress = 0,
                isCompleted = false,
                dateAssigned = currentTime,
                expiresAt = endOfDay
            )

            Log.d(TAG, "Generated: ${template.title} (${template.difficulty}, ${template.pointsReward} pts)")
            challenge
        }

        return challenges
    }

    /**
     * Generates a balanced set of daily challenges with one from each difficulty tier.
     *
     * This variant ensures users always receive a mix of:
     * - 1 EASY challenge (quick wins for motivation)
     * - 1 MEDIUM challenge (achievable with focus)
     * - 1 HARD challenge (stretch goal for dedicated users)
     *
     * This approach provides:
     * - Immediate gratification from easy challenges
     * - Meaningful progress from medium challenges
     * - Aspirational goals from hard challenges
     *
     * @return List of 3 balanced daily challenges (one easy, one medium, one hard).
     */
    fun generateBalancedDailyChallenges(): List<DailyChallenge> {
        val currentTime = System.currentTimeMillis()
        val endOfDay = getEndOfDayTimestamp()

        val pool = getChallengePool()

        // Separate challenges by difficulty
        val easyChallenges = pool.filter { it.difficulty == ChallengeDifficulty.EASY }.shuffled()
        val mediumChallenges = pool.filter { it.difficulty == ChallengeDifficulty.MEDIUM }.shuffled()
        val hardChallenges = pool.filter { it.difficulty == ChallengeDifficulty.HARD }.shuffled()

        // Select one from each difficulty tier
        val selectedTemplates = listOfNotNull(
            easyChallenges.firstOrNull(),
            mediumChallenges.firstOrNull(),
            hardChallenges.firstOrNull()
        )

        Log.d(TAG, "Generating balanced daily challenges (Easy, Medium, Hard)")

        // Instantiate the selected templates
        val challenges = selectedTemplates.mapIndexed { index, template ->
            DailyChallenge(
                id = "daily_${template.type}_${currentTime}_$index",
                title = template.title,
                description = template.description,
                pointsReward = template.pointsReward,
                type = template.type,
                targetValue = template.targetValue,
                currentProgress = 0,
                isCompleted = false,
                dateAssigned = currentTime,
                expiresAt = endOfDay
            )
        }

        return challenges
    }

    /**
     * Calculates the timestamp for 23:59:59 on the current day.
     *
     * This represents the expiration time for all challenges generated today.
     * After this time, challenges become invalid and should be replaced.
     *
     * @return Timestamp (milliseconds since epoch) for end of current day.
     */
    private fun getEndOfDayTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Determines if challenges need to be refreshed based on the last refresh timestamp.
     *
     * Challenges should be refreshed if:
     * - Never refreshed before (lastRefresh == 0)
     * - Last refresh was on a different calendar day
     *
     * This method uses calendar day comparison rather than 24-hour periods to ensure
     * challenges refresh at midnight regardless of when they were last generated.
     *
     * @param lastRefreshTimestamp Timestamp when challenges were last generated.
     * @return True if new challenges should be generated, false otherwise.
     */
    fun shouldRefreshChallenges(lastRefreshTimestamp: Long): Boolean {
        if (lastRefreshTimestamp == 0L) {
            return true // Never refreshed
        }

        val today = Calendar.getInstance()
        val lastRefresh = Calendar.getInstance().apply {
            timeInMillis = lastRefreshTimestamp
        }

        // Check if last refresh was on a different day
        return today.get(Calendar.YEAR) != lastRefresh.get(Calendar.YEAR) ||
                today.get(Calendar.DAY_OF_YEAR) != lastRefresh.get(Calendar.DAY_OF_YEAR)
    }
}
