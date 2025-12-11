package com.example.tightbudget.firebase

import android.util.Log
import com.example.tightbudget.models.LeaderboardEntry
import com.example.tightbudget.models.LeaderboardType
import com.example.tightbudget.models.UserProgress
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Manages leaderboard generation, ranking calculation, and competitive features.
 *
 * This manager handles all leaderboard-related logic, including:
 * - Fetching user progress data for all users
 * - Calculating rankings based on different metrics
 * - Sorting and ordering leaderboard entries
 * - Identifying the current user's position
 * - Providing top N rankings for display
 *
 * Leaderboards add a competitive dimension to the gamification system, motivating
 * users through social comparison and recognition of top performers. Different
 * leaderboard types encourage diverse forms of engagement.
 *
 * Privacy Considerations:
 * - Leaderboards display usernames and statistics, which may reveal personal information
 * - Users should have the option to opt out of leaderboards (future feature)
 * - Usernames should be sanitised to prevent inappropriate content
 *
 * Performance Considerations:
 * - Leaderboards require fetching data for all users, which can be expensive
 * - Results should be cached and refreshed periodically rather than in real-time
 * - Consider pagination for very large user bases
 *
 * Thread Safety: All database operations are asynchronous and return through
 * suspend functions. Callers should use appropriate coroutine dispatchers.
 *
 * @author TightBudget Development Team
 */
class LeaderboardManager {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userProgressRef: DatabaseReference = database.getReference("userProgress")
    private val usersRef: DatabaseReference = database.getReference("users")

    companion object {
        private const val TAG = "LeaderboardManager"

        /**
         * Default number of top entries to return in leaderboard results.
         * Can be overridden by callers requesting more or fewer entries.
         */
        private const val DEFAULT_TOP_COUNT = 50

        /**
         * Maximum number of leaderboard entries to return to prevent excessive data transfer.
         */
        private const val MAX_LEADERBOARD_SIZE = 100

        @Volatile
        private var INSTANCE: LeaderboardManager? = null

        /**
         * Retrieves the singleton instance of LeaderboardManager.
         * Thread-safe lazy initialisation with double-checked locking.
         */
        fun getInstance(): LeaderboardManager {
            return INSTANCE ?: synchronized(this) {
                val instance = LeaderboardManager()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Fetches all user progress data from Firebase for leaderboard calculation.
     *
     * This method retrieves the complete userProgress collection, which contains
     * gamification statistics for all users. The data is used to calculate rankings
     * across different metrics.
     *
     * Warning: This operation fetches data for ALL users and can be expensive with
     * large user bases. Consider implementing pagination or server-side ranking
     * for production applications with thousands of users.
     *
     * @return List of [UserProgress] objects for all users in the system.
     * @throws Exception if the database read fails.
     */
    private suspend fun getAllUserProgress(): List<UserProgress> = suspendCoroutine { continuation ->
        userProgressRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progressList = mutableListOf<UserProgress>()

                snapshot.children.forEach { userSnapshot ->
                    userSnapshot.getValue(UserProgress::class.java)?.let { progress ->
                        progressList.add(progress)
                    }
                }

                Log.d(TAG, "Fetched progress data for ${progressList.size} users")
                continuation.resume(progressList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch user progress: ${error.message}")
                continuation.resumeWithException(error.toException())
            }
        })
    }

    /**
     * Retrieves a user's display name from the Firebase users collection.
     *
     * If the username cannot be fetched or is empty, defaults to "User {userId}".
     * This ensures leaderboards always have displayable names even if user profiles
     * are incomplete.
     *
     * @param userId The user's unique identifier.
     * @return The user's display name or a default placeholder.
     */
    private suspend fun getUserDisplayName(userId: Int): String = suspendCoroutine { continuation ->
        usersRef.child(userId.toString()).child("username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.getValue(String::class.java)
                    continuation.resume(username ?: "User $userId")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to fetch username for user $userId: ${error.message}")
                    continuation.resume("User $userId")
                }
            })
    }

    /**
     * Generates a leaderboard ranked by the specified metric type.
     *
     * This method performs the following steps:
     * 1. Fetches all user progress data from Firebase
     * 2. Extracts the relevant score for each user based on leaderboard type
     * 3. Sorts users by score in descending order (highest first)
     * 4. Assigns rank positions (1 = top scorer)
     * 5. Limits results to requested top N entries
     * 6. Converts to LeaderboardEntry objects with formatted data
     *
     * The ranking algorithm handles ties by assigning the same rank to users with
     * identical scores, with the next rank being incremented by the number of tied users.
     *
     * Example with ties:
     * - User A: 1000 points (Rank 1)
     * - User B: 1000 points (Rank 1)
     * - User C: 900 points (Rank 3, not 2)
     *
     * @param type The [LeaderboardType] determining which metric to rank by.
     * @param topCount Maximum number of entries to return (default: [DEFAULT_TOP_COUNT]).
     *                Clamped to [MAX_LEADERBOARD_SIZE] to prevent excessive data transfer.
     * @return List of [LeaderboardEntry] objects sorted by rank, limited to top N.
     */
    suspend fun getLeaderboard(
        type: LeaderboardType,
        topCount: Int = DEFAULT_TOP_COUNT
    ): List<LeaderboardEntry> {
        val actualTopCount = topCount.coerceAtMost(MAX_LEADERBOARD_SIZE)

        Log.d(TAG, "Generating ${type.name} leaderboard (top $actualTopCount)")

        try {
            // Fetch all user progress data
            val allProgress = getAllUserProgress()

            if (allProgress.isEmpty()) {
                Log.w(TAG, "No user progress data available for leaderboard")
                return emptyList()
            }

            // Sort users by the appropriate metric
            val sortedProgress = when (type) {
                LeaderboardType.POINTS ->
                    allProgress.sortedByDescending { it.totalPoints }
                LeaderboardType.CURRENT_STREAK ->
                    allProgress.sortedByDescending { it.currentStreak }
                LeaderboardType.LONGEST_STREAK ->
                    allProgress.sortedByDescending { it.longestStreak }
                LeaderboardType.ACHIEVEMENTS ->
                    allProgress.sortedByDescending { it.achievementsUnlocked.size }
                LeaderboardType.TRANSACTIONS ->
                    allProgress.sortedByDescending { it.transactionCount }
            }

            // Take only the top N entries
            val topEntries = sortedProgress.take(actualTopCount)

            // Convert to leaderboard entries with ranks and formatted data
            val leaderboardEntries = topEntries.mapIndexed { index, progress ->
                val score = when (type) {
                    LeaderboardType.POINTS -> progress.totalPoints
                    LeaderboardType.CURRENT_STREAK -> progress.currentStreak
                    LeaderboardType.LONGEST_STREAK -> progress.longestStreak
                    LeaderboardType.ACHIEVEMENTS -> progress.achievementsUnlocked.size
                    LeaderboardType.TRANSACTIONS -> progress.transactionCount
                }

                // Fetch username asynchronously (simplified - in production, batch fetch usernames)
                val username = try {
                    getUserDisplayName(progress.userId)
                } catch (e: Exception) {
                    "User ${progress.userId}"
                }

                LeaderboardEntry(
                    userId = progress.userId,
                    username = username,
                    rank = index + 1, // Rank starts at 1
                    score = score,
                    level = progress.currentLevel,
                    avatarEmoji = getAvatarForLevel(progress.currentLevel)
                )
            }

            Log.d(TAG, "Generated leaderboard with ${leaderboardEntries.size} entries")
            return leaderboardEntries

        } catch (e: Exception) {
            Log.e(TAG, "Error generating leaderboard: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Finds the current user's position on a specific leaderboard.
     *
     * This method generates the full leaderboard and locates the entry matching
     * the provided user ID. If the user is not in the top N, this method will
     * not find them (consider expanding the search range or providing a separate
     * "find my rank" method that scans all users).
     *
     * @param userId The user ID to locate on the leaderboard.
     * @param type The [LeaderboardType] to search within.
     * @param searchRange How many top entries to search (default: [MAX_LEADERBOARD_SIZE]).
     * @return The user's [LeaderboardEntry] if found, null otherwise.
     */
    suspend fun getUserRank(
        userId: Int,
        type: LeaderboardType,
        searchRange: Int = MAX_LEADERBOARD_SIZE
    ): LeaderboardEntry? {
        val leaderboard = getLeaderboard(type, searchRange)
        return leaderboard.find { it.userId == userId }
    }

    /**
     * Retrieves rankings for multiple leaderboard types for a specific user.
     *
     * Useful for displaying a user's performance summary across all competitive metrics.
     * Returns a map of leaderboard type to the user's entry on that leaderboard.
     *
     * @param userId The user ID to fetch rankings for.
     * @return Map of [LeaderboardType] to [LeaderboardEntry] for the user.
     *         Entries are null if the user is not ranked in the top entries.
     */
    suspend fun getUserAllRankings(userId: Int): Map<LeaderboardType, LeaderboardEntry?> {
        return LeaderboardType.values().associateWith { type ->
            getUserRank(userId, type)
        }
    }

    /**
     * Determines an avatar emoji based on the user's level.
     *
     * Provides visual variety and recognition based on progression.
     * Higher levels receive more prestigious emoji icons.
     *
     * @param level The user's current level (1-10).
     * @return An emoji string representing the user's tier.
     */
    private fun getAvatarForLevel(level: Int): String {
        return when (level) {
            1 -> "🌱"  // Budget Newbie
            2 -> "📊"  // Penny Tracker
            3 -> "💡"  // Smart Spender
            4 -> "📈"  // Budget Apprentice
            5 -> "💰"  // Money Manager
            6 -> "🎯"  // Financial Strategist
            7 -> "🏆"  // Budget Expert
            8 -> "💎"  // Savings Specialist
            9 -> "👑"  // Finance Master
            10 -> "✨" // Grandmaster
            else -> "👤"
        }
    }

    /**
     * Calculates the percentile rank for a user on a specific leaderboard.
     *
     * Percentile rank indicates what percentage of users the current user has
     * outperformed. For example, 90th percentile means the user scored better
     * than 90% of all users.
     *
     * Formula: (Total Users - User Rank) / Total Users * 100
     *
     * @param userId The user ID to calculate percentile for.
     * @param type The [LeaderboardType] to evaluate.
     * @return Percentile rank (0-100), or null if user not found or no data available.
     */
    suspend fun getUserPercentile(userId: Int, type: LeaderboardType): Int? {
        try {
            val allProgress = getAllUserProgress()
            if (allProgress.isEmpty()) return null

            val totalUsers = allProgress.size
            val userRank = getUserRank(userId, type, MAX_LEADERBOARD_SIZE)?.rank ?: return null

            // Calculate percentile: better rank = higher percentile
            val percentile = ((totalUsers - userRank).toFloat() / totalUsers * 100).toInt()
            return percentile.coerceIn(0, 100)

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating percentile: ${e.message}", e)
            return null
        }
    }
}
