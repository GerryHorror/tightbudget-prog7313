package com.example.tightbudget.firebase

import android.util.Log
import com.example.tightbudget.models.UserProgress
import java.util.Calendar

/**
 * Manages user streak tracking and bonus point calculations within the gamification system.
 *
 * This manager handles all streak-related logic, including:
 * - Calculating consecutive day counts based on login timestamps
 * - Determining when streaks should increment, maintain, or reset
 * - Awarding milestone bonus points at significant streak thresholds
 * - Tracking both current and longest streaks for achievement validation
 *
 * Streaks encourage daily engagement by rewarding consistent application usage.
 * The logic is time-zone aware and operates on calendar days rather than 24-hour periods.
 *
 * Thread Safety: This class is stateless and thread-safe. All operations work with
 * immutable calendar instances and return new UserProgress instances rather than
 * mutating existing state.
 *
 * @author TightBudget Development Team
 */
class StreakManager {

    companion object {
        private const val TAG = "StreakManager"

        /**
         * Milestone thresholds where bonus points are awarded.
         * These values represent significant streak achievements.
         */
        private const val STREAK_MILESTONE_WEEK = 7
        private const val STREAK_MILESTONE_TWO_WEEKS = 14
        private const val STREAK_MILESTONE_MONTH = 30

        /**
         * Bonus point values awarded at streak milestones.
         * Higher values for longer streaks incentivise sustained engagement.
         */
        private const val BONUS_WEEK = 100
        private const val BONUS_TWO_WEEKS = 200
        private const val BONUS_MONTH = 500
    }

    /**
     * Represents the result of a streak calculation operation.
     *
     * @property newStreak The calculated streak value after processing the update.
     * @property streakIncreased Whether the streak count increased from the previous value.
     *                          Used to determine if milestone bonuses should be awarded.
     * @property bonusPointsEarned Points awarded for reaching a milestone. Zero if no milestone reached.
     * @property bonusReason Human-readable description of why bonus points were awarded.
     *                      Null if no bonus was earned.
     */
    data class StreakResult(
        val newStreak: Int,
        val streakIncreased: Boolean,
        val bonusPointsEarned: Int,
        val bonusReason: String?
    )

    /**
     * Calculates and updates the user's streak based on their login history.
     *
     * This method implements the core streak logic:
     * 1. If already logged today: maintain current streak (no change)
     * 2. If logged yesterday: increment streak by 1
     * 3. If first login ever: initialise streak to 1
     * 4. Otherwise (gap in logins): reset streak to 1
     *
     * The method also determines if a streak milestone has been reached and calculates
     * any bonus points that should be awarded. Milestones are only triggered when the
     * streak increases, not when it maintains or resets.
     *
     * Calendar Day Logic: "Today" and "yesterday" are determined by calendar dates in
     * the user's local time zone, not 24-hour periods. A login at 23:59 followed by
     * another at 00:01 counts as consecutive days.
     *
     * @param currentProgress The user's existing progress data including lastLoginDate and currentStreak.
     * @return [StreakResult] containing the new streak value and any bonus points earned.
     *
     * @see calculateStreakBonus for milestone bonus calculation details
     * @see isSameDay for calendar day comparison logic
     */
    fun calculateStreakUpdate(currentProgress: UserProgress): StreakResult {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        val lastLoginCal = Calendar.getInstance().apply {
            timeInMillis = currentProgress.lastLoginDate
        }

        // Determine if the user has already logged in today to prevent duplicate streak updates
        val alreadyLoggedToday = isSameDay(lastLoginCal, today)

        // Calculate the new streak value based on login patterns
        val newStreak = when {
            alreadyLoggedToday -> {
                // User already recorded activity today - maintain existing streak
                Log.d(TAG, "User already logged today, maintaining streak: ${currentProgress.currentStreak}")
                currentProgress.currentStreak
            }
            isSameDay(lastLoginCal, yesterday) -> {
                // User logged yesterday - increment streak for consecutive day
                val incrementedStreak = currentProgress.currentStreak + 1
                Log.d(TAG, "Continuing streak: ${currentProgress.currentStreak} -> $incrementedStreak")
                incrementedStreak
            }
            currentProgress.lastLoginDate == 0L -> {
                // First time logging in - initialise streak
                Log.d(TAG, "Starting first streak")
                1
            }
            else -> {
                // Gap in login history - reset streak to starting value
                Log.d(TAG, "Streak broken, resetting to 1")
                1
            }
        }

        // Determine if the streak count increased (rather than maintained or reset)
        val streakIncreased = newStreak > currentProgress.currentStreak

        // Calculate bonus points if a milestone was reached
        val bonusPoints = if (streakIncreased) {
            calculateStreakBonus(newStreak)
        } else {
            0
        }

        // Generate human-readable reason for bonus points
        val bonusReason = if (bonusPoints > 0) {
            "$newStreak-day streak bonus!"
        } else {
            null
        }

        return StreakResult(
            newStreak = newStreak,
            streakIncreased = streakIncreased,
            bonusPointsEarned = bonusPoints,
            bonusReason = bonusReason
        )
    }

    /**
     * Determines bonus points awarded for reaching specific streak milestones.
     *
     * Bonus points are awarded at key thresholds to create meaningful goals:
     * - 7 days (1 week): 100 points
     * - 14 days (2 weeks): 200 points
     * - 30 days (1 month): 500 points
     *
     * Bonuses are only awarded the first time each milestone is reached. If a user's
     * streak increments from 6 to 7, they receive the week bonus. If their streak
     * later resets and they reach 7 again, they receive it again.
     *
     * Note: This method does not track whether bonuses have been previously awarded.
     * The calling code must ensure bonuses are only given when the streak increases.
     *
     * @param streakDays The current streak count in days.
     * @return Bonus points for the milestone, or 0 if no milestone was reached.
     */
    private fun calculateStreakBonus(streakDays: Int): Int {
        return when (streakDays) {
            STREAK_MILESTONE_WEEK -> BONUS_WEEK
            STREAK_MILESTONE_TWO_WEEKS -> BONUS_TWO_WEEKS
            STREAK_MILESTONE_MONTH -> BONUS_MONTH
            else -> 0
        }
    }

    /**
     * Compares two calendar instances to determine if they represent the same calendar day.
     *
     * This comparison is based on year and day-of-year, ignoring time components.
     * Two timestamps on different dates will never match, even if they're less than
     * 24 hours apart. Two timestamps on the same date will always match, even if
     * they're 23 hours and 59 minutes apart.
     *
     * Example:
     * - 2025-01-15 23:59:59 and 2025-01-15 00:00:01 -> true (same day)
     * - 2025-01-15 23:59:59 and 2025-01-16 00:00:01 -> false (different days)
     * - 2025-12-31 12:00:00 and 2026-01-01 12:00:00 -> false (different years)
     *
     * @param cal1 First calendar instance to compare.
     * @param cal2 Second calendar instance to compare.
     * @return True if both calendars represent the same calendar day, false otherwise.
     */
    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Creates an updated UserProgress instance with new streak values applied.
     *
     * This method returns a new copy of the user progress with the following updates:
     * - currentStreak: Updated to the calculated value
     * - longestStreak: Updated to the maximum of current longest and new streak
     * - lastLoginDate: Updated to the current system time
     *
     * The longest streak is automatically maintained, ensuring it always reflects
     * the user's best performance even if their current streak is lower.
     *
     * @param currentProgress The existing user progress data.
     * @param streakResult The calculated streak update result.
     * @return A new UserProgress instance with updated streak values.
     */
    fun applyStreakUpdate(
        currentProgress: UserProgress,
        streakResult: StreakResult
    ): UserProgress {
        return currentProgress.copy(
            currentStreak = streakResult.newStreak,
            longestStreak = maxOf(currentProgress.longestStreak, streakResult.newStreak),
            lastLoginDate = System.currentTimeMillis()
        )
    }
}
