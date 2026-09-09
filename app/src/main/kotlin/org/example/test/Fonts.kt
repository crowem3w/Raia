package org.example.test

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

/** Central place to load the app's custom typeface (Dream Avenue). */
object Fonts {

    @Volatile
    private var cached: Typeface? = null

    fun dreamAvenue(context: Context): Typeface {
        return cached ?: synchronized(this) {
            cached ?: ResourcesCompat.getFont(context.applicationContext, R.font.dream_avenue)!!
                .also { cached = it }
        }
    }
}
