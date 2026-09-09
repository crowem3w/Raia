package org.example.test

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * Home screen. Intentionally blank for now — the only action available is
 * starting a new sketch, which opens [SketchActivity].
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)

        val createButton = MaterialButton(this).apply {
            text = "Create"
            isAllCaps = false
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            )
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SketchActivity::class.java))
            }
        }

        root.addView(createButton)
        setContentView(root)
    }
}
