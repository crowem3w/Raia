package org.example.test

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * Draws the "physical sheet" illusion that lives behind bottomNavBar's tab row:
 *
 *  1. A thin lit top edge + shaded bottom edge along the whole pill, selling the idea that the
 *     pill background (bg_bottom_nav_pill) is a thin sheet of material rather than a flat color.
 *  2. At the currently active tab, a soft raised bump in that sheet - as if the material were
 *     pushed up towards the viewer from underneath - rendered with a light-catching gradient on
 *     the bump itself and a soft grounding shadow ring where the bump meets the flat sheet.
 *  3. A rounded-square "card" drawn on top of the bump, offset with its own drop shadow and a
 *     bright top edge, so it reads as a separate rigid piece lying down on top of the bulge
 *     rather than being printed flat onto the sheet.
 *
 * The activity drives this purely by telling it where the active tab's pill currently is
 * (center X + half width/height, in this view's own coordinate space, which is kept aligned with
 * navTabsRow by giving both views the same padding box inside bottomNavBar). See
 * SketchActivity#syncBendOverlayToActiveTab.
 */
class SheetBendIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    // Current (possibly mid-animation) geometry of the active tab pill, in local coordinates.
    // NaN centerX means "nothing to draw yet" (before the first layout pass has happened).
    private var centerX: Float = Float.NaN
    private var halfWidth: Float = 0f
    private var halfHeight: Float = 0f

    // Animation target, so a second move request before the first finishes retargets smoothly.
    private var targetCenterX: Float = Float.NaN
    private var targetHalfWidth: Float = 0f
    private var targetHalfHeight: Float = 0f

    private var moveAnimator: ValueAnimator? = null

    /** True while an activity-triggered move animation is in flight. */
    var isAnimating: Boolean = false
        private set

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    private val bumpExtraX = dp(16f)
    private val bumpExtraY = dp(10f)
    private val buttonCornerRadius = dp(14f)
    private val bumpCornerExtra = dp(10f)

    private val edgeHighlightHeight = dp(2f)
    private val edgeShadeHeight = dp(2f)

    private val bumpFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bumpRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.5f)
    }
    private val buttonShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(90, 0, 0, 0)
    }
    private val buttonFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edgeHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(40, 255, 255, 255)
    }
    private val edgeShadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(60, 0, 0, 0)
    }

    private val bumpRect = RectF()
    private val buttonRect = RectF()
    private val scratchPath = Path()

    init {
        // BlurMaskFilter (used for the soft bump ring + card drop-shadow) only rasterizes on a
        // software layer.
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        bumpRingPaint.maskFilter = android.graphics.BlurMaskFilter(dp(5f), android.graphics.BlurMaskFilter.Blur.NORMAL)
        buttonShadowPaint.maskFilter = android.graphics.BlurMaskFilter(dp(6f), android.graphics.BlurMaskFilter.Blur.NORMAL)
    }

    /**
     * Move (or jump) the bend + card to sit under a pill at [newCenterX] with the given half
     * extents, all in this view's local coordinate space. When [animate] is false the change is
     * applied immediately (used for the very first layout, and to re-sync after a real relayout
     * such as a rotation).
     */
    fun setActiveTab(newCenterX: Float, newHalfWidth: Float, newHalfHeight: Float, animate: Boolean) {
        if (newHalfWidth <= 0f || newHalfHeight <= 0f) return

        moveAnimator?.cancel()

        if (!animate || centerX.isNaN()) {
            centerX = newCenterX
            halfWidth = newHalfWidth
            halfHeight = newHalfHeight
            targetCenterX = newCenterX
            targetHalfWidth = newHalfWidth
            targetHalfHeight = newHalfHeight
            isAnimating = false
            invalidate()
            return
        }

        val startCenterX = centerX
        val startHalfWidth = halfWidth
        val startHalfHeight = halfHeight
        targetCenterX = newCenterX
        targetHalfWidth = newHalfWidth
        targetHalfHeight = newHalfHeight

        moveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 260L
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float
                centerX = startCenterX + (targetCenterX - startCenterX) * t
                halfWidth = startHalfWidth + (targetHalfWidth - startHalfWidth) * t
                halfHeight = startHalfHeight + (targetHalfHeight - startHalfHeight) * t
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: android.animation.Animator) {
                    isAnimating = true
                }

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    isAnimating = false
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        // 1) The sheet's own top-lit edge and underside shading, running the full width.
        scratchPath.rewind()
        scratchPath.addRoundRect(0f, 0f, width.toFloat(), edgeHighlightHeight * 6, edgeHighlightHeight * 3, edgeHighlightHeight * 3, Path.Direction.CW)
        canvas.save()
        canvas.clipRect(0f, 0f, width.toFloat(), edgeHighlightHeight)
        canvas.drawPath(scratchPath, edgeHighlightPaint)
        canvas.restore()

        scratchPath.rewind()
        scratchPath.addRoundRect(0f, height - edgeShadeHeight * 6, width.toFloat(), height.toFloat(), edgeShadeHeight * 3, edgeShadeHeight * 3, Path.Direction.CW)
        canvas.save()
        canvas.clipRect(0f, height - edgeShadeHeight, width.toFloat(), height.toFloat())
        canvas.drawPath(scratchPath, edgeShadePaint)
        canvas.restore()

        if (centerX.isNaN()) return

        val cy = height / 2f

        // 2) The bump: the sheet pushed towards the viewer under the active tab.
        bumpRect.set(
            centerX - halfWidth - bumpExtraX,
            (cy - halfHeight - bumpExtraY).coerceAtLeast(1f),
            centerX + halfWidth + bumpExtraX,
            (cy + halfHeight + bumpExtraY).coerceAtMost(height - 1f),
        )
        val bumpRadius = buttonCornerRadius + bumpCornerExtra
        val bumpSpan = maxOf(bumpRect.width(), bumpRect.height())

        bumpFillPaint.shader = RadialGradient(
            centerX, cy, bumpSpan / 1.4f,
            intArrayOf(Color.argb(70, 255, 255, 255), Color.argb(0, 255, 255, 255)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRoundRect(bumpRect, bumpRadius, bumpRadius, bumpFillPaint)

        // Grounding shadow where the raised bump curves back down to the flat sheet.
        bumpRingPaint.color = Color.argb(80, 0, 0, 0)
        canvas.drawRoundRect(bumpRect, bumpRadius, bumpRadius, bumpRingPaint)

        // 3) The active-tab card, lying on top of the bump with its own lift and light.
        buttonRect.set(centerX - halfWidth, cy - halfHeight, centerX + halfWidth, cy + halfHeight)

        canvas.save()
        canvas.translate(0f, dp(2.5f))
        canvas.drawRoundRect(buttonRect, buttonCornerRadius, buttonCornerRadius, buttonShadowPaint)
        canvas.restore()

        buttonFillPaint.shader = LinearGradient(
            0f, buttonRect.top, 0f, buttonRect.bottom,
            Color.parseColor("#5590FF"), Color.parseColor("#3D7EFF"),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRoundRect(buttonRect, buttonCornerRadius, buttonCornerRadius, buttonFillPaint)

        // Bright top rim of the card, clipped to its own rounded shape.
        scratchPath.rewind()
        scratchPath.addRoundRect(buttonRect, buttonCornerRadius, buttonCornerRadius, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(scratchPath)
        buttonHighlightPaint.shader = LinearGradient(
            0f, buttonRect.top, 0f, buttonRect.top + buttonRect.height() * 0.55f,
            Color.argb(75, 255, 255, 255), Color.argb(0, 255, 255, 255),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(buttonRect, buttonHighlightPaint)
        canvas.restore()
    }
}
