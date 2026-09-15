package org.example.test

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

/**
 * Shown once, the first time the app is opened. Tapping its button (the sketch's own button,
 * if one was drawn - e.g. "Get started") marks onboarding complete and hands off to
 * MainActivity.finishOnboarding(), which won't show this screen again.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val root = view as? ViewGroup ?: return
        findFirstButton(root)?.setOnClickListener {
            (activity as? MainActivity)?.finishOnboarding()
        }
        // TODO: if this screen has no button, wire up whatever should complete onboarding
        // (e.g. a swipeable pager's "done" state) and call MainActivity.finishOnboarding().
    }

    private fun findFirstButton(group: ViewGroup): MaterialButton? {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            if (child is MaterialButton) return child
            if (child is ViewGroup) findFirstButton(child)?.let { return it }
        }
        return null
    }
}
