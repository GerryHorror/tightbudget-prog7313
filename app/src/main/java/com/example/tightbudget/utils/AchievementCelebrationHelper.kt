package com.example.tightbudget.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import com.example.tightbudget.R
import com.example.tightbudget.models.Achievement
import com.google.android.material.button.MaterialButton

/**
 * Utility class for displaying celebratory dialogs when users unlock achievements.
 *
 * This helper manages the complete celebration experience, including:
 * - Full-screen modal dialog with custom animations
 * - Confetti particle effects
 * - Pulsing glow and sparkle animations
 * - Optional sound effects
 * - Customisable congratulations messages
 *
 * The celebration creates an immersive, rewarding experience that reinforces
 * positive user behaviour and encourages continued engagement with the application.
 *
 * Thread Safety: This class should only be used from the main UI thread.
 *
 * Usage:
 * ```kotlin
 * AchievementCelebrationHelper.showCelebration(
 *     context = this,
 *     achievement = unlockedAchievement,
 *     enableSound = true
 * ) {
 *     // Optional callback when user dismisses the dialog
 *     refreshUI()
 * }
 * ```
 *
 * @author TightBudget Development Team
 */
object AchievementCelebrationHelper {

    /**
     * Duration of the confetti animation in milliseconds.
     */
    private const val CONFETTI_DURATION = 4000L

    /**
     * Duration to delay before starting certain animations, allowing the dialog to settle.
     */
    private const val ANIMATION_DELAY = 100L

    /**
     * Displays a full-screen celebration dialog for an unlocked achievement.
     *
     * This method creates a visually striking modal experience with multiple
     * coordinated animations:
     * 1. Dialog slides up from bottom with fade-in
     * 2. Confetti particles rain down from top
     * 3. Achievement icon scales up with bounce effect
     * 4. Glow background pulses continuously
     * 5. Sparkle emojis twinkle
     * 6. Optional celebration sound effect plays
     *
     * The dialog is non-cancellable (user must press Continue button) to ensure
     * they notice the achievement unlock. This prevents accidental dismissal and
     * guarantees the celebratory moment is experienced.
     *
     * @param context The activity or application context for creating the dialog.
     * @param achievement The [Achievement] that was unlocked, containing display data.
     * @param enableSound Whether to play a celebration sound effect (default: true).
     *                   Set to false for silent mode or testing scenarios.
     * @param onDismiss Optional callback invoked when the user dismisses the dialog.
     *                 Useful for triggering UI refreshes or analytics events.
     */
    fun showCelebration(
        context: Context,
        achievement: Achievement,
        enableSound: Boolean = true,
        onDismiss: (() -> Unit)? = null
    ) {
        // Create full-screen dialog
        val dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_achievement_unlock_celebration)
            setCancelable(false) // Prevent accidental dismissal
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        }

        // Bind views
        val confettiContainer = dialog.findViewById<FrameLayout>(R.id.confettiContainer)
        val iconContainer = dialog.findViewById<FrameLayout>(R.id.iconContainer)
        val glowBackground = dialog.findViewById<View>(R.id.glowBackground)
        val achievementEmoji = dialog.findViewById<TextView>(R.id.achievementEmoji)
        val achievementTitle = dialog.findViewById<TextView>(R.id.achievementTitle)
        val achievementDescription = dialog.findViewById<TextView>(R.id.achievementDescription)
        val pointsReward = dialog.findViewById<TextView>(R.id.pointsReward)
        val sparkleLeft = dialog.findViewById<TextView>(R.id.sparkleLeft)
        val sparkleRight = dialog.findViewById<TextView>(R.id.sparkleRight)
        val continueButton = dialog.findViewById<MaterialButton>(R.id.continueButton)

        // Populate achievement data
        achievementEmoji.text = achievement.emoji
        achievementTitle.text = achievement.title
        achievementDescription.text = achievement.description
        pointsReward.text = "+${achievement.pointsRequired} pts"

        // Start confetti animation
        val confettiView = ConfettiView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        confettiContainer.addView(confettiView)
        confettiView.startConfetti(CONFETTI_DURATION)

        // Start animations with slight delay to allow dialog to settle
        iconContainer.postDelayed({
            startCelebrationAnimations(
                context = context,
                iconContainer = iconContainer,
                glowBackground = glowBackground,
                sparkleLeft = sparkleLeft,
                sparkleRight = sparkleRight
            )
        }, ANIMATION_DELAY)

        // Play celebration sound if enabled
        if (enableSound) {
            playCelebrationSound(context)
        }

        // Handle continue button
        continueButton.setOnClickListener {
            confettiView.stopConfetti()
            dialog.dismiss()
            onDismiss?.invoke()
        }

        // Show dialog with slide-up animation
        dialog.show()
        dialog.window?.decorView?.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.achievement_card_slide_up)
        )
    }

    /**
     * Starts all coordinated animations for the celebration UI elements.
     *
     * This method triggers multiple simultaneous animations to create a cohesive
     * celebratory effect:
     * - Icon: Scale-up with bounce
     * - Glow: Continuous pulsing
     * - Sparkles: Twinkling rotation and scale
     *
     * All animations are carefully timed to create visual harmony without
     * overwhelming the user.
     *
     * @param context Context for loading animation resources.
     * @param iconContainer Container view holding the achievement icon.
     * @param glowBackground Background view with pulsing glow effect.
     * @param sparkleLeft Left sparkle emoji view.
     * @param sparkleRight Right sparkle emoji view.
     */
    private fun startCelebrationAnimations(
        context: Context,
        iconContainer: FrameLayout,
        glowBackground: View,
        sparkleLeft: TextView,
        sparkleRight: TextView
    ) {
        // Icon scale-up animation (one-time)
        val iconAnimation = AnimationUtils.loadAnimation(context, R.anim.achievement_icon_scale_pulse)
        iconContainer.startAnimation(iconAnimation)

        // Glow pulse animation (repeating)
        val glowAnimation = AnimationUtils.loadAnimation(context, R.anim.achievement_glow_pulse)
        glowBackground.startAnimation(glowAnimation)

        // Sparkle twinkle animations (repeating, with slight offset for visual variety)
        val sparkleAnimation = AnimationUtils.loadAnimation(context, R.anim.achievement_sparkle_twinkle)
        sparkleLeft.startAnimation(sparkleAnimation)

        // Start right sparkle with slight delay for asynchronous twinkling
        sparkleRight.postDelayed({
            sparkleRight.startAnimation(sparkleAnimation)
        }, 400)
    }

    /**
     * Plays a celebratory sound effect when an achievement is unlocked.
     *
     * The sound is played at a moderate volume level and released immediately
     * after completion to avoid memory leaks. Sound playback is non-blocking
     * and fails gracefully if resources are unavailable.
     *
     * Current Implementation: Uses a default system notification sound as a
     * placeholder. For production, this should be replaced with a custom
     * celebration sound effect (e.g., a short fanfare or success chime).
     *
     * Future Enhancement: Add custom sound resource to res/raw/achievement_unlock.mp3
     * and load it using MediaPlayer.create(context, R.raw.achievement_unlock)
     *
     * @param context Context for accessing sound resources.
     */
    private fun playCelebrationSound(context: Context) {
        try {
            // TODO: Replace with custom achievement sound resource
            // For now, using a default system sound as placeholder
            val mediaPlayer = MediaPlayer.create(
                context,
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            )

            mediaPlayer?.apply {
                setVolume(0.3f, 0.3f) // Moderate volume
                setOnCompletionListener { mp ->
                    mp.release() // Prevent memory leaks
                }
                start()
            }
        } catch (e: Exception) {
            // Fail silently - sound is optional and shouldn't crash the app
            android.util.Log.w("AchievementCelebration", "Failed to play sound: ${e.message}")
        }
    }

    /**
     * Displays a simplified celebration for milestone rewards.
     *
     * This variant is similar to achievement celebrations but with messaging
     * tailored for milestone accomplishments (e.g., "Reach Level 5" or
     * "Complete all beginner achievements").
     *
     * Future Enhancement: Consider creating a separate layout optimised for
     * milestone rewards with different styling and messaging.
     *
     * @param context The activity or application context.
     * @param milestoneTitle The title of the unlocked milestone.
     * @param milestoneDescription Description of what was accomplished.
     * @param milestoneEmoji Emoji representing the milestone.
     * @param bonusPoints Points awarded for this milestone.
     * @param enableSound Whether to play celebration sound.
     * @param onDismiss Optional callback when dismissed.
     */
    fun showMilestoneCelebration(
        context: Context,
        milestoneTitle: String,
        milestoneDescription: String,
        milestoneEmoji: String,
        bonusPoints: Int,
        enableSound: Boolean = true,
        onDismiss: (() -> Unit)? = null
    ) {
        // Create a temporary Achievement object to reuse the same dialog layout
        // In production, consider creating a dedicated milestone dialog layout
        val milestoneAsAchievement = Achievement(
            id = "milestone_temp",
            title = milestoneTitle,
            description = milestoneDescription,
            emoji = milestoneEmoji,
            pointsRequired = bonusPoints,
            type = com.example.tightbudget.models.AchievementType.POINTS,
            targetValue = 0,
            isUnlocked = true,
            unlockedDate = System.currentTimeMillis()
        )

        showCelebration(
            context = context,
            achievement = milestoneAsAchievement,
            enableSound = enableSound,
            onDismiss = onDismiss
        )
    }
}
