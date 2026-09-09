package org.example.test

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * The app's home / landing screen: a hero section with marketing copy and a
 * "Create app" call to action on the left, and a product screenshot on the
 * right, both vertically centered against each other.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        // --- Left column: copy + CTA ---------------------------------------

        val eyebrow = TextView(this).apply {
            text = "/ Design / Prompt / Build"
            textSize = 12f
            setTextColor(Color.parseColor("#6750A4"))
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.05f
        }

        val headline = TextView(this).apply {
            text = "Turn your ideas\ninto prompt."
            textSize = 26f
            setTextColor(Color.parseColor("#1B1B1F"))
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(12), 0, dp(12))
        }

        val subtext = TextView(this).apply {
            text = "A visual builder for the age of AI. Design, customize, " +
                "and generate prompts for apps."
            textSize = 14f
            setTextColor(Color.parseColor("#5F5F66"))
            setLineSpacing(dp(3).toFloat(), 1f)
        }

        val createButton = MaterialButton(this).apply {
            text = "Create app  →"
            isAllCaps = false
            textSize = 15f
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1B1B1F"))
            setTextColor(Color.WHITE)
            cornerRadius = dp(24)
            setPadding(dp(24), dp(12), dp(24), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(20) }
            setOnClickListener {
                startActivity(Intent(this@MainActivity, EditorActivity::class.java))
            }
        }

        val leftColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            addView(eyebrow)
            addView(headline)
            addView(subtext)
            addView(createButton)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f,
            )
        }

        // --- Right column: hero screenshot ----------------------------------

        val heroImage = ImageView(this).apply {
            setImageResource(R.drawable.home_hero)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f,
            ).apply { gravity = Gravity.CENTER_VERTICAL }
        }

        // --- Combine into a centered hero row --------------------------------

        val heroRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(24), dp(12), dp(24))
            addView(leftColumn)
            addView(heroImage)
        }

        val root = ScrollView(this).apply {
            isFillViewport = true
            addView(
                heroRow.apply {
                    layoutParams = ScrollView.LayoutParams(
                        ScrollView.LayoutParams.MATCH_PARENT,
                        ScrollView.LayoutParams.MATCH_PARENT,
                    )
                },
            )
        }

        setContentView(root)
    }
}
