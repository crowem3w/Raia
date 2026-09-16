package org.example.test

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment

/**
 * Shown briefly on launch, then automatically hands off to MainActivity.advanceFromSplash(),
 * which decides between Onboarding (first run only) and Home.
 */
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val handler = Handler(Looper.getMainLooper())
    private val advance = Runnable { (activity as? MainActivity)?.advanceFromSplash() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler.postDelayed(advance, SPLASH_DELAY_MS)
    }

    override fun onDestroyView() {
        handler.removeCallbacks(advance)
        super.onDestroyView()
    }

    private companion object {
        const val SPLASH_DELAY_MS = 1200L
    }
}
