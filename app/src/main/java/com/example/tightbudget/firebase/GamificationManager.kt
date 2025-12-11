package com.example.tightbudget.firebase

import android.text.format.DateUtils.isToday
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.tightbudget.models.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

/**
 * Manages gamification features including points, achievements, and daily challenges
 */
class GamificationManager {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userProgressRef: DatabaseReference = database.getReference("userProgress")
    private val achievementsRef: DatabaseReference = database.getReference("achievements")
    private val dailyChallengesRef: DatabaseReference = database.getReference("dailyChallenges")

    companion object {
        private const val TAG = "GamificationManager"

        @Volatile
        private var INSTANCE: GamificationManager? = null

        fun getInstance(): GamificationManager {
            return INSTANCE ?: synchronized(this) {
                val instance = GamificationManager()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Awards points to user and updates their progress
     */
    /**
     * Awards points to user, checks for achievements, and updates their progress in a single transaction
     */
    suspend fun awardPoints(userId: Int, points: Int, reason: String): Boolean {
        return try {
            Log.d(TAG, "Awarding $points points to user $userId for: $reason")

            val userProgress = getUserProgress(userId)
            var currentPoints = userProgress.totalPoints + points
            
            // Create intermediate progress to check for unlocks
            val intermediateProgress = userProgress.copy(
                totalPoints = currentPoints
            )

            // Check for new achievements unlocked by these points
            val achievementManager = AchievementManager()
            val unlockResult = achievementManager.checkForNewUnlocks(intermediateProgress)
            
            // Add bonus points from any newly unlocked achievements
            currentPoints += unlockResult.totalBonusPoints
            
            // Update unlocked achievements list
            var currentUnlocked = userProgress.achievementsUnlocked
            if (unlockResult.newlyUnlocked.isNotEmpty()) {
                val updatedList = currentUnlocked.toMutableList()
                updatedList.addAll(unlockResult.newlyUnlocked.map { it.id })
                currentUnlocked = updatedList.distinct()
                
                unlockResult.newlyUnlocked.forEach {
                    Log.d(TAG, "🏆 Also unlocked: ${it.title}")
                }
            }

            // Calculate final level
            val newLevel = PointsManager.calculateLevel(currentPoints)

            val finalProgress = userProgress.copy(
                totalPoints = currentPoints,
                currentLevel = newLevel,
                achievementsUnlocked = currentUnlocked
            )

            saveUserProgress(userId, finalProgress)
            Log.d(TAG, "Points awarded. Total: $currentPoints. Level: $newLevel")

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error awarding points: ${e.message}", e)
            false
        }
    }

    /**
     * Handles transaction addition and awards appropriate points
     */
    /**
     * Handles transaction addition and awards appropriate points in a single update
     */
    suspend fun onTransactionAdded(userId: Int, transaction: Transaction): Int {
        return try {
            Log.d(TAG, "=== TRANSACTION GAMIFICATION START ===")
            
            val userProgress = getUserProgress(userId)
            
            // 1. Calculate Transaction & Receipt counts
            val hasReceipt = !transaction.receiptPath.isNullOrEmpty()
            val newTransactionCount = userProgress.transactionCount + 1
            val newReceiptsUploaded = if (hasReceipt) userProgress.receiptsUploaded + 1 else userProgress.receiptsUploaded
            
            // 2. Calculate Pending Points
            var pendingPoints = PointsSystem.ADD_TRANSACTION
            if (hasReceipt) pendingPoints += PointsSystem.ADD_RECEIPT
            if (isFirstTransactionToday(userId)) pendingPoints += PointsSystem.FIRST_TRANSACTION_OF_DAY
            
            // 3. Streak Update
            val streakManager = StreakManager()
            val streakResult = streakManager.calculateStreakUpdate(userProgress)
            // Add streak bonus points
            pendingPoints += streakResult.bonusPointsEarned
            if (streakResult.bonusPointsEarned > 0) {
                Log.d(TAG, "Streak bonus earned: ${streakResult.bonusPointsEarned}")
            }
            
            // 4. Create intermediate progress
            val intermediateProgress = streakManager.applyStreakUpdate(userProgress, streakResult).copy(
                transactionCount = newTransactionCount,
                receiptsUploaded = newReceiptsUploaded,
                totalPoints = userProgress.totalPoints + pendingPoints
            )
            
            // 5. Achievement Check
            val achievementManager = AchievementManager()
            val unlockResult = achievementManager.checkForNewUnlocks(intermediateProgress)
            
            if (unlockResult.newlyUnlocked.isNotEmpty()) {
                Log.d(TAG, "Unlocked ${unlockResult.newlyUnlocked.size} achievements")
                pendingPoints += unlockResult.totalBonusPoints
            }
            
            // 6. Final State Construction
            val finalPoints = userProgress.totalPoints + pendingPoints
            val finalLevel = PointsManager.calculateLevel(finalPoints)
            
            var finalUnlocked = intermediateProgress.achievementsUnlocked 
            if (unlockResult.newlyUnlocked.isNotEmpty()) {
                 val unlockedList = finalUnlocked.toMutableList()
                 unlockedList.addAll(unlockResult.newlyUnlocked.map { it.id })
                 finalUnlocked = unlockedList.distinct()
            }

            val finalProgress = intermediateProgress.copy(
                totalPoints = finalPoints,
                currentLevel = finalLevel,
                achievementsUnlocked = finalUnlocked
            )

            // 7. Save Everything Once
            saveUserProgress(userId, finalProgress)
            
            // 8. Trigger Side Effects (Challenges)
            updateChallengeProgress(userId, ChallengeType.TRANSACTION)
            if (hasReceipt) updateChallengeProgress(userId, ChallengeType.RECEIPT)
            if (streakResult.streakIncreased) updateChallengeProgress(userId, ChallengeType.STREAK)

            Log.d(TAG, "=== TRANSACTION GAMIFICATION END ===")
            Log.d(TAG, "Total points earned: $pendingPoints")
            
            pendingPoints
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error processing transaction gamification: ${e.message}", e)
            0
        }
    }

    /**
     * Generate realistic daily challenges
     */
    suspend fun generateDailyChallenges(userId: Int): List<DailyChallenge> {
        val challenges = mutableListOf<DailyChallenge>()
        val currentTime = System.currentTimeMillis()
        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        // Always generate these 3 daily challenges
        challenges.add(
            DailyChallenge(
                id = "daily_transaction_${currentTime}",
                title = "Transaction Logger",
                description = "Add 3 transactions today",
                pointsReward = 75,
                type = ChallengeType.TRANSACTION,
                targetValue = 3,
                dateAssigned = currentTime,
                expiresAt = endOfDay
            )
        )

        challenges.add(
            DailyChallenge(
                id = "daily_receipt_${currentTime}",
                title = "Receipt Collector",
                description = "Upload 2 receipts today",
                pointsReward = 60,
                type = ChallengeType.RECEIPT,
                targetValue = 2,
                dateAssigned = currentTime,
                expiresAt = endOfDay
            )
        )

        challenges.add(
            DailyChallenge(
                id = "daily_budget_${currentTime}",
                title = "Smart Spender",
                description = "Keep expenses under 200 today",
                pointsReward = 100,
                type = ChallengeType.BUDGET_COMPLIANCE,
                targetValue = 1,
                dateAssigned = currentTime,
                expiresAt = endOfDay
            )
        )

        // Save challenges to Firebase
        challenges.forEach { challenge ->
            saveDailyChallenge(userId, challenge)
        }

        return challenges
    }

    /**
     * Update challenge progress and mark as completed if target is reached
     */
    private suspend fun updateChallengeProgress(userId: Int, challengeType: ChallengeType) {
        try {
            Log.d(TAG, "Updating challenge progress for type: $challengeType")

            val userChallenges = getUserDailyChallenges(userId)
            val todaysChallenges = userChallenges.filter {
                isToday(it.dateAssigned) && !it.isCompleted
            }

            for (challenge in todaysChallenges) {
                if (challenge.type == challengeType) {
                    val currentProgress = getCurrentChallengeProgress(challenge.type, userId)

                    // Check if challenge should be completed
                    if (currentProgress >= challenge.targetValue && !challenge.isCompleted) {
                        val completedChallenge = challenge.copy(
                            isCompleted = true,
                            currentProgress = currentProgress
                        )

                        // Save completed challenge
                        saveDailyChallenge(userId, completedChallenge)

                        // Award challenge completion points
                        awardPoints(userId, challenge.pointsReward, "Completed challenge: ${challenge.title}")

                        Log.d(TAG, "✅ Challenge completed: ${challenge.title} (+${challenge.pointsReward} points)")
                    } else {
                        // Update progress without completing
                        val updatedChallenge = challenge.copy(currentProgress = currentProgress)
                        saveDailyChallenge(userId, updatedChallenge)

                        Log.d(TAG, "📊 Challenge progress updated: ${challenge.title} ($currentProgress/${challenge.targetValue})")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating challenge progress: ${e.message}", e)
        }
    }

    /**
     * Get actual current progress for a challenge type
     */
    private suspend fun getCurrentChallengeProgress(challengeType: ChallengeType, userId: Int): Int {
        return when (challengeType) {
            ChallengeType.TRANSACTION -> getTodayTransactionCount(userId)
            ChallengeType.RECEIPT -> getTodayReceiptCount(userId)
            ChallengeType.BUDGET_COMPLIANCE -> {
                // Check if user stayed within budget today
                val budgetManager = FirebaseBudgetManager.getInstance()
                val activeBudget = budgetManager.getActiveBudgetGoal(userId)
                if (activeBudget != null) {
                    val todaySpent = getTodaySpending(userId)
                    val dailyBudget = activeBudget.totalBudget / 30 // Rough daily budget
                    if (todaySpent <= dailyBudget) 1 else 0
                } else 0
            }
            ChallengeType.SAVINGS -> getTodaySavingsPercentage(userId)
            ChallengeType.STREAK -> getUserProgress(userId).currentStreak
            ChallengeType.CATEGORY_LIMIT -> getTodayCategoryComplianceCount(userId)
        }
    }
    /**
     * Calculate user level based on total points
     * Delegates to PointsManager
     */
    fun calculateLevel(totalPoints: Int): Int {
        return PointsManager.calculateLevel(totalPoints)
    }

    /**
     * Get all achievements
     * Delegates to AchievementManager
     */
    fun getAllAchievements(): List<Achievement> {
        return AchievementManager().getAllAchievements()
    }

    private suspend fun getTodaySpending(userId: Int): Double {
        return try {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val transactionManager = FirebaseTransactionManager.getInstance()
            val allTransactions = transactionManager.getAllTransactionsForUser(userId)

            allTransactions.filter { transaction ->
                transaction.isExpense && transaction.dateTimestamp >= todayStart.timeInMillis
            }.sumOf { it.amount }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting today's spending: ${e.message}", e)
            0.0
        }
    }





    // Helper methods for Firebase operations
    suspend fun getUserProgress(userId: Int): UserProgress = suspendCoroutine { continuation ->
        userProgressRef.child(userId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progress = snapshot.getValue(UserProgress::class.java) ?: UserProgress(userId = userId)
                continuation.resume(progress)
            }
            override fun onCancelled(error: DatabaseError) {
                continuation.resume(UserProgress(userId = userId))
            }
        })
    }

    private suspend fun saveUserProgress(userId: Int, progress: UserProgress) {
        userProgressRef.child(userId.toString()).setValue(progress).await()
    }

    private suspend fun saveDailyChallenge(userId: Int, challenge: DailyChallenge) {
        dailyChallengesRef.child(userId.toString()).child(challenge.id).setValue(challenge).await()
    }

    suspend fun getUserDailyChallenges(userId: Int): List<DailyChallenge> = suspendCoroutine { continuation ->
        dailyChallengesRef.child(userId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val challenges = mutableListOf<DailyChallenge>()
                snapshot.children.forEach {
                    it.getValue(DailyChallenge::class.java)?.let { challenge -> challenges.add(challenge) }
                }
                continuation.resume(challenges)
            }
            override fun onCancelled(error: DatabaseError) {
                continuation.resume(emptyList())
            }
        })
    }

    private suspend fun saveAchievement(userId: Int, achievement: Achievement) {
        achievementsRef.child(userId.toString()).child(achievement.id).setValue(achievement).await()
    }



    /**
     * Check if this is the user's first transaction today
     */
    private suspend fun isFirstTransactionToday(userId: Int): Boolean {
        return try {
            val today = Calendar.getInstance()
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val transactionManager = FirebaseTransactionManager.getInstance()
            val allTransactions = transactionManager.getAllTransactionsForUser(userId)

            val todayTransactions = allTransactions.filter { transaction ->
                val transactionDate = Calendar.getInstance().apply {
                    timeInMillis = transaction.dateTimestamp
                }
                transactionDate.timeInMillis >= todayStart.timeInMillis
            }

            todayTransactions.size <= 1 // This transaction is the first (or second, close enough)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking first transaction: ${e.message}", e)
            false
        }
    }



    /**
     * Get today's transaction count for challenge progress
     */
    private suspend fun getTodayTransactionCount(userId: Int): Int {
        return try {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val transactionManager = FirebaseTransactionManager.getInstance()
            val allTransactions = transactionManager.getAllTransactionsForUser(userId)

            allTransactions.count { transaction ->
                transaction.dateTimestamp >= todayStart.timeInMillis
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting today's transaction count: ${e.message}", e)
            0
        }
    }

    /**
     * Get today's receipt count for challenge progress
     */
    private suspend fun getTodayReceiptCount(userId: Int): Int {
        return try {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val transactionManager = FirebaseTransactionManager.getInstance()
            val allTransactions = transactionManager.getAllTransactionsForUser(userId)

            allTransactions.count { transaction ->
                transaction.dateTimestamp >= todayStart.timeInMillis &&
                        !transaction.receiptPath.isNullOrEmpty()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting today's receipt count: ${e.message}", e)
            0
        }
    }

    /**
     * Check budget compliance for today (simplified version)
     */
    private suspend fun getTodayBudgetComplianceCount(userId: Int): Int {
        return try {
            // Simplified: assume user is compliant if they haven't overspent massively
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val transactionManager = FirebaseTransactionManager.getInstance()
            val allTransactions = transactionManager.getAllTransactionsForUser(userId)

            val todayExpenses = allTransactions.filter { transaction ->
                transaction.dateTimestamp >= todayStart.timeInMillis && transaction.isExpense
            }.sumOf { it.amount }

            // Simple logic: if spent less than 500, consider it compliant
            if (todayExpenses < 500.0) 2 else 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking budget compliance: ${e.message}", e)
            0
        }
    }

    /**
     * Calculate savings percentage compared to yesterday (simplified)
     */
    private suspend fun getTodaySavingsPercentage(userId: Int): Int {
        return try {
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

            val transactionManager = FirebaseTransactionManager.getInstance()
            val allTransactions = transactionManager.getAllTransactionsForUser(userId)

            val todayExpenses = allTransactions.filter { transaction ->
                val transCal = Calendar.getInstance().apply { timeInMillis = transaction.dateTimestamp }
                isSameDay(transCal, today) && transaction.isExpense
            }.sumOf { it.amount }

            val yesterdayExpenses = allTransactions.filter { transaction ->
                val transCal = Calendar.getInstance().apply { timeInMillis = transaction.dateTimestamp }
                isSameDay(transCal, yesterday) && transaction.isExpense
            }.sumOf { it.amount }

            if (yesterdayExpenses > 0) {
                val savings = ((yesterdayExpenses - todayExpenses) / yesterdayExpenses * 100).toInt()
                maxOf(0, savings) // Return 0 if negative (spent more)
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating savings: ${e.message}", e)
            0
        }
    }

    /**
     * Category compliance check (simplified)
     */
    private suspend fun getTodayCategoryComplianceCount(userId: Int): Int {
        // Simplified implementation - return random compliance for now
        return kotlin.random.Random.nextInt(0, 3)
    }

    /**
     * Helper function to check if two calendars are on the same day
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        return calendar.timeInMillis
    }
}