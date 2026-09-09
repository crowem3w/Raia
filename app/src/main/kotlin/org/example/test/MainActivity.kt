package org.example.test

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

/**
 * Home screen: hero copy on the left, product mockup on the right, both
 * centered vertically against each other. Tapping "Create app" starts a
 * new sketch in [SketchActivity].
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val d = resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()

        val mutedColor = 0xFFA8A29E.toInt()
        val onColor = 0xFFF5F5F4.toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
            )
            setPadding(dp(20), dp(20), dp(12), dp(20))
        }

        // Left: copy + CTA
        val leftColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f,
            )
        }

        leftColumn.addView(TextView(this).apply {
            text = "/ Design / Prompt / Build"
            textSize = 11f
            setTextColor(mutedColor)
        })

        leftColumn.addView(TextView(this).apply {
            text = "Turn your ideas\ninto prompt."
            textSize = 24f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(onColor)
            setPadding(0, dp(12), 0, 0)
        })

        leftColumn.addView(TextView(this).apply {
            text = "A visual builder for age of AI. Design, costumize, and generate prompts apps."
            textSize = 13f
            setTextColor(mutedColor)
            setPadding(0, dp(12), 0, 0)
        })

        leftColumn.addView(MaterialButton(this).apply {
            text = "Create app  \u2192"
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(20) }
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SketchActivity::class.java))
            }
        })

        // Right: hero mockup image
        val heroImage = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.home_hero))
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f,
            )
        }

        root.addView(leftColumn)
        root.addView(heroImage)
        setContentView(root)
    }
}
