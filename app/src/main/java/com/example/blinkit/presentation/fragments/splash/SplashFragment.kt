package com.example.blinkit.presentation.fragments.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.core.common.viewBinding
import com.example.blinkit.databinding.FragmentSplashBinding

@Suppress("DEPRECATION")
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val binding by viewBinding(FragmentSplashBinding::bind)

    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        findNavController().navigate(R.id.action_splashFragment_to_singInFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler.postDelayed(navigateRunnable, 3000)

        val animTop =
            android.view.animation.AnimationUtils.loadAnimation(view.context, R.anim.from_top)
        val animBottom =
            android.view.animation.AnimationUtils.loadAnimation(view.context, R.anim.from_bottom)

        val tvSplash = view.findViewById<TextView>(R.id.tvBlinkit)
        val tvSplashc = view.findViewById<TextView>(R.id.tvSplashc)

        tvSplash.animation = animBottom
        tvSplashc.animation = animTop

        activity?.window?.let { window ->
            window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.yellow)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(navigateRunnable)

        activity?.window?.let { window ->
            window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.yellow)
        }
    }

}