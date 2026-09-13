package org.example.test

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import android.view.animation.OvershootInterpolator

/**
 * Paints the bottom nav bar as a single continuous "sculpted" surface (design.txt), rather
 * than a flat rounded-rect with separate button backgrounds glued on top.
 *
 * The whole shape is one closed [Path]: a wide pill that normally runs flat along its top
 * edge, except around the active tab, where that edge dips into a shallow concave valley on
 * both sides, then rises up and wraps around into a raised rounded-square island. Every
 * transition is a "zero-slope" cubic (see [sCurveTo]) so the top edge reads as one flexible
 * ribbon with no seams or sharp corners, per design.txt.
 *
 * This view only paints the surface - SketchActivity keeps the actual tab icons/labels in a
 * sibling view (navTabRow) layered on top of this one and sharing its bounds, and calls
 * [setActiveCenter] whenever the active tab changes so the fold can travel to meet it.
 */
class NavDockFoldView(context: Context) : View(context) {

    private val d = resources.displayMetrics.density
    private fun dp(v: Float) = v * d

    // --- Shape geometry, matching the "shallow valley / raised island" description ----------
    private val cornerRadius = dp(40f)
    private val valleyDepth = dp(9f)      // how far the flat edge dips before it starts rising
    private val valleyWidth = dp(22f)     // flat-edge -> valley-bottom transition width
    private val valleyGap = dp(10f)       // valley-bottom -> island-side transition width
    private val islandHalfWidth = dp(29f)
    private val islandRise = dp(20f)      // how far the island's top sits above the flat edge

    // Extra headroom this view reserves above its "resting" flat top edge so the raised
    // island has somewhere to rise into. SketchActivity mirrors this value (topReserveDp)
    // when padding/translating the tab row and icon bubble that sit on top of this view.
    private val topInset = islandRise + dp(6f)

    // --- Palette (kept in sync with SketchActivity's warm/minimal nav colors) ---------------
    private val dockFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(224, 247, 245, 240)
    }
    private val islandFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(255, 254, 252, 248)
    }
    private val ambientShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(50, 40, 34, 28)
        maskFilter = BlurMaskFilter(dp(16f), BlurMaskFilter.Blur.NORMAL)
    }
    private val contactShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 40, 34, 28)
        maskFilter = BlurMaskFilter(dp(6f), BlurMaskFilter.Blur.NORMAL)
    }
    private val innerFoldShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(46, 40, 34, 28)
        style = Paint.Style.STROKE
        strokeWidth = dp(9f)
        maskFilter = BlurMaskFilter(dp(6f), BlurMaskFilter.Blur.NORMAL)
    }

    private var activeCenterX = 0f
    private var foldAnimator: ValueAnimator? = null

    private val outlinePath = Path()
    private val islandPatchPath = Path()
    private val valleyStrokePath = Path()

    init {
        // BlurMaskFilter (used for all three shadow layers above) only renders on a software
        // layer - this view is small and static between animation frames, so the cost is fine.
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    /**
     * Moves the fold so it's centered under [centerX] (in this view's own local x coordinate,
     * i.e. the same coordinate space as the sibling tab row). Animates with a gentle spring
     * overshoot per design.txt (~250-450ms) unless [animate] is false, which is used for the
     * very first placement, where there's nothing to animate from yet.
     */
    fun setActiveCenter(centerX: Float, animate: Boolean) {
        foldAnimator?.cancel()
        if (!animate || width == 0) {
            activeCenterX = centerX
            rebuildPaths()
            invalidate()
            return
        }
        foldAnimator = ValueAnimator.ofFloat(activeCenterX, centerX).apply {
            duration = 360L
            interpolator = OvershootInterpolator(1.1f)
            addUpdateListener {
                activeCenterX = it.animatedValue as Float
                rebuildPaths()
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (activeCenterX == 0f) activeCenterX = w / 2f
        rebuildPaths()
    }

    // The one repeated move that makes the whole top edge read as a single flexible ribbon:
    // a cubic with a horizontal tangent at both ends, so every segment blends seamlessly into
    // the next with no visible joins or sharp corners.
    private fun Path.sCurveTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        val midX = x1 + (x2 - x1) / 2f
        cubicTo(midX, y1, midX, y2, x2, y2)
    }

    private fun rebuildPaths() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val flatTopY = topInset
        val bottomY = h
        val valleyY = flatTopY + valleyDepth
        val islandTopY = flatTopY - islandRise

        val minCx = cornerRadius + valleyWidth + valleyGap + islandHalfWidth + dp(2f)
        val maxCx = w - minCx
        val cx = if (maxCx > minCx) activeCenterX.coerceIn(minCx, maxCx) else w / 2f

        val islandLeft = cx - islandHalfWidth
        val islandRight = cx + islandHalfWidth
        val leftValleyStart = islandLeft - valleyGap - valleyWidth
        val leftValleyBottom = islandLeft - valleyGap
        val rightValleyBottom = islandRight + valleyGap
        val rightValleyEnd = islandRight + valleyGap + valleyWidth

        outlinePath.reset()
        outlinePath.moveTo(cornerRadius, flatTopY)
        outlinePath.lineTo(leftValleyStart, flatTopY)
        outlinePath.sCurveTo(leftValleyStart, flatTopY, leftValleyBottom, valleyY)
        outlinePath.sCurveTo(leftValleyBottom, valleyY, islandLeft, islandTopY)
        outlinePath.lineTo(islandRight, islandTopY)
        outlinePath.sCurveTo(islandRight, islandTopY, rightValleyBottom, valleyY)
        outlinePath.sCurveTo(rightValleyBottom, valleyY, rightValleyEnd, flatTopY)
        outlinePath.lineTo(w - cornerRadius, flatTopY)
        outlinePath.arcTo(RectF(w - 2 * cornerRadius, flatTopY, w, flatTopY + 2 * cornerRadius), -90f, 90f)
        outlinePath.lineTo(w, bottomY - cornerRadius)
        outlinePath.arcTo(RectF(w - 2 * cornerRadius, bottomY - 2 * cornerRadius, w, bottomY), 0f, 90f)
        outlinePath.lineTo(cornerRadius, bottomY)
        outlinePath.arcTo(RectF(0f, bottomY - 2 * cornerRadius, 2 * cornerRadius, bottomY), 90f, 90f)
        outlinePath.lineTo(0f, flatTopY + cornerRadius)
        outlinePath.arcTo(RectF(0f, flatTopY, 2 * cornerRadius, flatTopY + 2 * cornerRadius), 180f, 90f)
        outlinePath.close()

        // The raised island itself, as a standalone patch so it can be filled a shade
        // brighter than the rest of the dock (design.txt: "slightly brighter frosted
        // surface") while sharing an exact seam with the outline above.
        islandPatchPath.reset()
        islandPatchPath.moveTo(leftValleyBottom, valleyY)
        islandPatchPath.sCurveTo(leftValleyBottom, valleyY, islandLeft, islandTopY)
        islandPatchPath.lineTo(islandRight, islandTopY)
        islandPatchPath.sCurveTo(islandRight, islandTopY, rightValleyBottom, valleyY)
        islandPatchPath.lineTo(leftValleyBottom, valleyY)
        islandPatchPath.close()

        // Just the two folded curves (not the flat parts), stroked and blurred to read as an
        // inner shadow tracing the bend - drawn separately from the outline/island fills.
        valleyStrokePath.reset()
        valleyStrokePath.moveTo(leftValleyStart, flatTopY)
        valleyStrokePath.sCurveTo(leftValleyStart, flatTopY, leftValleyBottom, valleyY)
        valleyStrokePath.sCurveTo(leftValleyBottom, valleyY, islandLeft, islandTopY)
        valleyStrokePath.moveTo(islandRight, islandTopY)
        valleyStrokePath.sCurveTo(islandRight, islandTopY, rightValleyBottom, valleyY)
        valleyStrokePath.sCurveTo(rightValleyBottom, valleyY, rightValleyEnd, flatTopY)
    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return

        // Broad ambient shadow for the whole dock, so it reads as suspended above the canvas.
        canvas.save()
        canvas.translate(0f, dp(4f))
        canvas.drawPath(outlinePath, ambientShadowPaint)
        canvas.restore()

        canvas.drawPath(outlinePath, dockFill)

        canvas.save()
        canvas.clipPath(outlinePath)
        // Contact shadow beneath the raised island: drawn blurred and slightly larger than
        // the island patch that gets painted on top of it next, so only its soft blurred
        // edge peeks out - exactly the look of a shape resting just above a surface.
        canvas.drawPath(islandPatchPath, contactShadowPaint)
        // Inner shadow tracing the fold, to sell the depth of the bend itself.
        canvas.drawPath(valleyStrokePath, innerFoldShadowPaint)
        canvas.restore()

        canvas.drawPath(islandPatchPath, islandFill)
    }
}
