package com.example.blinkit.presentation.fragments.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.core.common.viewBinding
import com.example.blinkit.databinding.FragmentSplashBinding
import com.example.blinkit.presentation.activity.UsersMainActivity
import com.example.blinkit.presentation.fragments.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
@Suppress("DEPRECATION")
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val binding by viewBinding(FragmentSplashBinding::bind)

    private val viewModel by viewModels<AuthViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setStatusBarColor()

        val animTop =
            android.view.animation.AnimationUtils.loadAnimation(view.context, R.anim.from_top)
        val animBottom =
            android.view.animation.AnimationUtils.loadAnimation(view.context, R.anim.from_bottom)

        val tvSplash = view.findViewById<TextView>(R.id.tvBlinkit)
        val tvSplashc = view.findViewById<TextView>(R.id.tvSplashc)

        tvSplash.animation = animBottom
        tvSplashc.animation = animTop

        lifecycleScope.launch {
            delay(3000)

            val isUserLoggedIn = viewModel.isACurrentUser.value

            if (isUserLoggedIn) {
                startActivity(Intent(requireActivity(), UsersMainActivity::class.java))
                requireActivity().finish()
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_singInFragment)
            }
        }

        activity?.window?.let { window ->
            window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.yellow)
        }

    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.let { window ->
            window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.yellow)
        }
    }

}