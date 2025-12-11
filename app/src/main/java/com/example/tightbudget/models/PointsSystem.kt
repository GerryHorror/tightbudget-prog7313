package com.example.tightbudget.models

/**
 * Centralised configuration for all point values within the gamification system.
 *
 * This object defines the point rewards for various user actions and achievements,
 * providing a single source of truth for point allocation throughout the application.
 * All values are immutable constants to ensure consistency across the system.
 *
 * Points serve as the primary currency within the gamification framework, driving
 * user progression through levels and unlocking achievements.
 *
 * @author TightBudget Development Team
 */
object PointsSystem {

    // ═══════════════════════════════════════════════════════════════════════════════
    // BASIC ACTIONS - Core daily activities that encourage regular app usage
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Points awarded when a user adds a single transaction to their financial record.
     * This is the most fundamental action and encourages consistent expense tracking.
     */
    const val ADD_TRANSACTION = 10

    /**
     * Bonus points awarded when a transaction includes a photographic receipt.
     * Encourages users to maintain comprehensive financial documentation,
     * which improves expense verification and record-keeping quality.
     */
    const val ADD_RECEIPT = 15

    /**
     * Points awarded for the first transaction logged on any given day.
     * Incentivises daily engagement and helps establish a consistent tracking habit.
     * Only awarded once per calendar day, regardless of transaction count.
     */
    const val FIRST_TRANSACTION_OF_DAY = 20

    // ═══════════════════════════════════════════════════════════════════════════════
    // DAILY GOALS - Time-bound challenges that promote specific behaviours
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Points awarded upon successful completion of any daily challenge.
     * Daily challenges reset at midnight and provide varied objectives to
     * maintain user engagement through diverse activities.
     */
    const val COMPLETE_DAILY_CHALLENGE = 50

    /**
     * Points awarded when the user remains within their allocated daily budget.
     * Calculated by comparing daily spending against the pro-rated monthly budget.
     * This reward encourages fiscal discipline and mindful spending habits.
     */
    const val STAY_WITHIN_BUDGET_DAILY = 25

    /**
     * Points awarded when transactions are logged across all spending categories
     * within a single day. Encourages comprehensive financial tracking and helps
     * users maintain awareness of spending patterns across different areas.
     */
    const val LOG_ALL_CATEGORIES_IN_DAY = 30

    // ═══════════════════════════════════════════════════════════════════════════════
    // PERIODIC ACHIEVEMENTS - Longer-term goals requiring sustained effort
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Points awarded when the user maintains budget compliance over an entire week.
     * Requires staying within daily budget allocations for seven consecutive days.
     * Promotes sustained financial discipline over a meaningful time period.
     */
    const val WEEKLY_BUDGET_COMPLIANCE = 150

    /**
     * Points awarded for maintaining budget compliance throughout a full calendar month.
     * This is a significant achievement requiring consistent financial management.
     * Represents the highest tier of budget-based rewards.
     */
    const val MONTHLY_BUDGET_COMPLIANCE = 500

    /**
     * Points awarded when a user achieves a predefined savings goal.
     * Savings goals can be customised by the user and represent specific financial targets.
     * This reward encourages long-term financial planning and goal-oriented saving.
     */
    const val ACHIEVE_SAVINGS_GOAL = 100
}