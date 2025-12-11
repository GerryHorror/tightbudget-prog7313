package com.example.tightbudget.utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.random.Random

/**
 * Custom view that renders animated confetti particles for celebration effects.
 *
 * This view creates a visually appealing confetti animation by rendering multiple
 * coloured shapes (rectangles, circles, triangles) that fall from the top of the screen
 * with physics-based motion including rotation, oscillation, and varying speeds.
 *
 * The confetti animation is triggered programmatically and runs for a configurable
 * duration before automatically stopping. Particles are recycled for performance.
 *
 * Usage:
 * ```kotlin
 * val confettiView = ConfettiView(context)
 * container.addView(confettiView)
 * confettiView.startConfetti(durationMs = 3000)
 * ```
 *
 * @author TightBudget Development Team
 */
class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Represents a single confetti particle with physics properties.
     *
     * @property x Horizontal position on the canvas.
     * @property y Vertical position on the canvas.
     * @property velocityY Vertical falling speed (pixels per frame).
     * @property velocityX Horizontal drift speed for oscillation.
     * @property rotation Current rotation angle in degrees.
     * @property rotationSpeed Rotation increment per frame.
     * @property size Particle size in pixels.
     * @property color Particle colour (ARGB integer).
     * @property shape Particle shape type (RECTANGLE, CIRCLE, TRIANGLE).
     */
    private data class ConfettiParticle(
        var x: Float,
        var y: Float,
        var velocityY: Float,
        var velocityX: Float,
        var rotation: Float,
        var rotationSpeed: Float,
        var size: Float,
        var color: Int,
        var shape: ParticleShape
    )

    /**
     * Available particle shapes for visual variety.
     */
    private enum class ParticleShape {
        RECTANGLE,
        CIRCLE,
        TRIANGLE
    }

    private val particles = mutableListOf<ConfettiParticle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trianglePath = Path() // Reusable path for triangle drawing
    private var animator: ValueAnimator? = null

    /**
     * Predefined confetti colours creating a vibrant, celebratory palette.
     * Includes gold, teal, orange, pink, purple, and green.
     */
    private val confettiColors = intArrayOf(
        0xFFFFD700.toInt(), // Gold
        0xFF26A69A.toInt(), // Teal
        0xFFFF9800.toInt(), // Orange
        0xFFE91E63.toInt(), // Pink
        0xFF9C27B0.toInt(), // Purple
        0xFF4CAF50.toInt(), // Green
        0xFF2196F3.toInt(), // Blue
        0xFFFFC107.toInt()  // Amber
    )

    /**
     * Number of confetti particles to generate.
     * Higher counts create denser effects but may impact performance.
     */
    private val particleCount = 80

    /**
     * Initiates the confetti animation for a specified duration.
     *
     * This method generates particles at randomised positions across the top of the view
     * and starts an animator that continuously updates particle positions and triggers
     * redraws. The animation automatically stops after the specified duration.
     *
     * Particles are initialised with randomised properties:
     * - Position: Random X coordinate, starting above the view
     * - Velocity: Random falling speed and horizontal drift
     * - Rotation: Random initial angle and rotation speed
     * - Size: Random size within a reasonable range
     * - Colour: Randomly selected from the predefined palette
     * - Shape: Randomly chosen from available shapes
     *
     * @param durationMs Total animation duration in milliseconds (default: 3000ms).
     */
    fun startConfetti(durationMs: Long = 3000) {
        // Clear any existing particles
        particles.clear()

        // Generate particles with randomised properties
        repeat(particleCount) {
            particles.add(
                ConfettiParticle(
                    x = Random.nextFloat() * width,
                    y = -Random.nextFloat() * 200 - 50, // Start above the view
                    velocityY = Random.nextFloat() * 3 + 2, // Fall speed: 2-5 px/frame
                    velocityX = Random.nextFloat() * 2 - 1, // Horizontal drift: -1 to +1 px/frame
                    rotation = Random.nextFloat() * 360, // Random initial rotation
                    rotationSpeed = Random.nextFloat() * 10 - 5, // Rotation speed: -5 to +5 degrees/frame
                    size = Random.nextFloat() * 12 + 8, // Size: 8-20 pixels
                    color = confettiColors[Random.nextInt(confettiColors.size)],
                    shape = ParticleShape.entries[Random.nextInt(ParticleShape.entries.size)]
                )
            )
        }

        // Create and start the animation
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            addUpdateListener {
                updateParticles()
                invalidate() // Trigger redraw
            }
            start()
        }
    }

    /**
     * Updates the position and rotation of all particles based on physics simulation.
     *
     * This method is called on every animation frame to move particles downward,
     * apply horizontal oscillation, and rotate them. Particles that fall below the
     * view bounds are recycled to the top to create a continuous effect.
     */
    private fun updateParticles() {
        particles.forEach { particle ->
            // Update position
            particle.y += particle.velocityY
            particle.x += particle.velocityX

            // Add slight oscillation to horizontal movement
            particle.velocityX += (Random.nextFloat() * 0.2f - 0.1f)
            particle.velocityX = particle.velocityX.coerceIn(-2f, 2f)

            // Update rotation
            particle.rotation += particle.rotationSpeed

            // Recycle particles that fall off the bottom
            if (particle.y > height + 50) {
                particle.y = -50f
                particle.x = Random.nextFloat() * width
            }

            // Wrap particles that drift off the sides
            if (particle.x < -50) particle.x = width + 50f
            if (particle.x > width + 50) particle.x = -50f
        }
    }

    /**
     * Renders all confetti particles to the canvas.
     *
     * This method is called automatically by the Android framework whenever the view
     * needs to be redrawn. It iterates through all particles and draws them with
     * their current position, rotation, size, and colour.
     *
     * Different drawing logic is used for each particle shape:
     * - RECTANGLE: Rotated rectangle
     * - CIRCLE: Simple circle (rotation doesn't affect appearance)
     * - TRIANGLE: Rotated equilateral triangle
     *
     * @param canvas The canvas to draw on.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        particles.forEach { particle ->
            paint.color = particle.color

            canvas.save()
            canvas.translate(particle.x, particle.y)
            canvas.rotate(particle.rotation)

            when (particle.shape) {
                ParticleShape.RECTANGLE -> {
                    canvas.drawRect(
                        -particle.size / 2,
                        -particle.size / 2,
                        particle.size / 2,
                        particle.size / 2,
                        paint
                    )
                }
                ParticleShape.CIRCLE -> {
                    canvas.drawCircle(0f, 0f, particle.size / 2, paint)
                }
                ParticleShape.TRIANGLE -> {
                    trianglePath.reset()
                    trianglePath.moveTo(0f, -particle.size / 2)
                    trianglePath.lineTo(particle.size / 2, particle.size / 2)
                    trianglePath.lineTo(-particle.size / 2, particle.size / 2)
                    trianglePath.close()
                    canvas.drawPath(trianglePath, paint)
                }
            }

            canvas.restore()
        }
    }

    /**
     * Stops the confetti animation and clears all particles.
     *
     * This method should be called when the celebration effect is no longer needed
     * to free resources and stop unnecessary rendering.
     */
    fun stopConfetti() {
        animator?.cancel()
        animator = null
        particles.clear()
        invalidate()
    }

    /**
     * Cleanup method to ensure animations are cancelled when the view is detached.
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopConfetti()
    }
}
