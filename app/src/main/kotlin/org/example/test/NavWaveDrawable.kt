package org.example.test

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * Paints the bottom nav bar not as a flat rectangle but as a single soft, continuous sheet that
 * looks physically bent/folded upward around whichever tab is active: the left/right ends sit
 * low (a wide rounded stadium shape) while the top edge rises into one smooth hump centered on
 * [peakX], so the active icon reads as nesting naturally inside the raised fold rather than as a
 * separate floating chip.
 *
 * [peakX] is in the drawable's own bounds coordinate space (i.e. the same coordinates as the
 * active tab's `left + width / 2f` within the nav bar), and is meant to be driven by a
 * ValueAnimator from the activity for a smooth slide when the active tab changes. A negative
 * value means "no tab positioned yet" and draws a flat-topped resting sheet (used for the first
 * frame, before the tabs have been laid out and we know a real x position).
 */
class NavWaveDrawable(
    private val density: Float,
    private val fillTopColor: Int,
    private val fillBottomColor: Int,
    private val rimColor: Int,
    private val creaseShadowColor: Int,
) : Drawable() {

    private fun dp(v: Float) = v * density

    // How far below the drawable's top bound the resting (non-raised) edge sits, and how close
    // to the very top the raised peak reaches - the difference is the visible "lift" of the fold.
    private val bumpHeightPx = dp(22f)
    private val peakInsetPx = dp(3f)
    private val bumpHalfWidthPx = dp(44f)
    private val cornerRadiusPx = dp(30f)

    var peakX: Float = -1f
        set(value) {
            if (field == value) return
            field = value
            rebuildPath()
            invalidateSelf()
        }

    private val path = Path()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = rimColor
    }
    private val creasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        rebuildShaders(bounds)
        rebuildPath()
    }

    private fun rebuildShaders(bounds: Rect) {
        if (bounds.width() <= 0 || bounds.height() <= 0) return
        val top = bounds.top.toFloat()
        val bottom = bounds.bottom.toFloat()

        // Soft vertical gradient across the whole sheet - brighter where the material catches
        // the light near the top of the fold, settling to a slightly deeper warm-white below.
        fillPaint.shader = LinearGradient(
            0f, top, 0f, bottom,
            intArrayOf(fillTopColor, fillTopColor, fillBottomColor),
            floatArrayOf(0f, 0.3f, 1f),
            Shader.TileMode.CLAMP,
        )

        // A thin rim-light that traces the whole outline but is only really visible along the
        // upper portion (top edge + upper sides of the fold), fading to nothing by mid-height.
        rimPaint.shader = LinearGradient(
            0f, top, 0f, top + bounds.height() * 0.45f,
            intArrayOf(colorWithAlpha(rimColor, 150), colorWithAlpha(rimColor, 0)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
    }

    private fun colorWithAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    private fun rebuildPath() {
        val b = bounds
        if (b.width() <= 0 || b.height() <= 0) return
        path.reset()

        val left = b.left.toFloat()
        val top = b.top.toFloat()
        val right = b.right.toFloat()
        val bottom = b.bottom.toFloat()
        val cr = cornerRadiusPx.coerceAtMost((bottom - top) / 2f).coerceAtMost((right - left) / 2f)
        val baseTopY = top + bumpHeightPx
        val peakY = top + peakInsetPx

        val hasPeak = peakX >= 0f
        val clampedPeak = peakX.coerceIn(left + cr, right - cr)
        val bumpLeftX = (clampedPeak - bumpHalfWidthPx).coerceAtLeast(left + cr)
        val bumpRightX = (clampedPeak + bumpHalfWidthPx).coerceAtMost(right - cr)

        path.moveTo(left + cr, baseTopY)
        if (hasPeak) {
            path.lineTo(bumpLeftX, baseTopY)
            // Rise into the fold - one smooth cubic curve up to the peak.
            path.cubicTo(
                bumpLeftX + (clampedPeak - bumpLeftX) * 0.6f, baseTopY,
                clampedPeak - (clampedPeak - bumpLeftX) * 0.32f, peakY,
                clampedPeak, peakY,
            )
            // Settle back down out of the fold on the other side.
            path.cubicTo(
                clampedPeak + (bumpRightX - clampedPeak) * 0.32f, peakY,
                bumpRightX - (bumpRightX - clampedPeak) * 0.6f, baseTopY,
                bumpRightX, baseTopY,
            )
            path.lineTo(right - cr, baseTopY)
        } else {
            path.lineTo(right - cr, baseTopY)
        }
        // Top-right corner, right edge, bottom-right corner.
        path.quadTo(right, baseTopY, right, baseTopY + cr)
        path.lineTo(right, bottom - cr)
        path.quadTo(right, bottom, right - cr, bottom)
        // Bottom edge, bottom-left corner.
        path.lineTo(left + cr, bottom)
        path.quadTo(left, bottom, left, bottom - cr)
        // Left edge back up to the top-left corner.
        path.lineTo(left, baseTopY + cr)
        path.quadTo(left, baseTopY, left + cr, baseTopY)
        path.close()
    }

    override fun draw(canvas: Canvas) {
        if (bounds.width() <= 0 || bounds.height() <= 0) return

        canvas.save()
        canvas.clipPath(path)
        canvas.drawPath(path, fillPaint)

        // A soft inner crease shadow right where the fold rises out of the resting sheet, so the
        // transition between the low ends and the raised center reads with real depth instead of
        // looking like a flat cutout.
        if (peakX >= 0f) {
            val creaseCx = peakX.coerceIn(bounds.left.toFloat(), bounds.right.toFloat())
            val creaseCy = bounds.top + bumpHeightPx
            val creaseRadius = bumpHalfWidthPx * 1.6f
            creasePaint.shader = RadialGradient(
                creaseCx, creaseCy, creaseRadius,
                intArrayOf(colorWithAlpha(creaseShadowColor, 46), colorWithAlpha(creaseShadowColor, 0)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(creaseCx, creaseCy, creaseRadius, creasePaint)

            // A gentle highlight catching the very top of the raised fold, like light grazing the
            // crest of a bent, translucent sheet.
            val highlightCy = bounds.top + peakInsetPx + dp(5f)
            val highlightRadius = bumpHalfWidthPx * 0.85f
            highlightPaint.shader = RadialGradient(
                creaseCx, highlightCy, highlightRadius,
                intArrayOf(Color.argb(90, 255, 255, 255), Color.argb(0, 255, 255, 255)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(creaseCx, highlightCy, highlightRadius, highlightPaint)
        }
        canvas.restore()

        canvas.drawPath(path, rimPaint)
    }

    override fun getOutline(outline: Outline) {
        // Let the elevation shadow trace the actual folded silhouette (concave outlines are
        // supported for shadow-casting, just not for clipping) instead of a plain rectangle, so
        // the shadow itself follows the curve of the sheet.
        if (path.isEmpty) {
            super.getOutline(outline)
            return
        }
        outline.setPath(path)
    }

    override fun setAlpha(alpha: Int) {
        fillPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        fillPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
