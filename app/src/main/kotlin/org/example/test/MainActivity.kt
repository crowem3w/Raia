package org.example.test

import android.os.Bundle
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Single-Activity host: swaps a full-screen Fragment per screen into fragment_container.
 * Launches to Splash, which calls advanceFromSplash() after a short delay.
 * Onboarding is shown once per install; completion is persisted in SharedPreferences.
 */
class MainActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            showFragment(SplashFragment())
        }
    }

    /** Called by SplashFragment once its delay elapses. */
    fun advanceFromSplash() {
        showFragment(resolvePostSplashFragment())
    }

    private fun resolvePostSplashFragment(): Fragment =
        if (prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)) HomeFragment() else OnboardingFragment()

    /** Called by OnboardingFragment once its completing action is tapped. */
    fun finishOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
        showFragment(HomeFragment())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private companion object {
        const val PREFS_NAME = "app_prefs"
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
