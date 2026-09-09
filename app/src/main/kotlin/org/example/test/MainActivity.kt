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






class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val d = resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()
        val appFont = Fonts.dreamAvenue(this)

        val mutedColor = 0xFFA8A29E.toInt()
        val onColor = 0xFFF5F5F4.toInt()

        
        
        
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
            )
        }

        
        val contentArea = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                2f,
            )
            setPadding(dp(20), dp(20), dp(12), dp(20))
        }

        
        val spacer = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f,
            )
        }

        
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
            textSize = 12f
            setTextColor(mutedColor)
            typeface = appFont
        })

        leftColumn.addView(TextView(this).apply {
            text = "Turn your ideas\ninto prompt."
            textSize = 28f
            setTypeface(appFont, Typeface.BOLD)
            setTextColor(onColor)
            setPadding(0, dp(12), 0, 0)
        })

        leftColumn.addView(TextView(this).apply {
            text = "A visual builder for age of AI. Design, costumize, and generate prompts apps."
            textSize = 17f
            setTextColor(mutedColor)
            setPadding(0, dp(12), 0, 0)
            typeface = appFont
        })

        leftColumn.addView(MaterialButton(this).apply {
            text = "Create app  \u2192"
            isAllCaps = false
            textSize = 16f
            typeface = appFont
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(20) }
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SketchActivity::class.java))
            }
        })

        
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

        contentArea.addView(leftColumn)
        contentArea.addView(heroImage)
        root.addView(contentArea)
        root.addView(spacer)
        setContentView(root)
    }
}