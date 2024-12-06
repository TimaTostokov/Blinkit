package com.example.blinkit.presentation.fragments.home

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.core.common.Constants
import com.example.blinkit.core.common.viewBinding
import com.example.blinkit.data.remote.model.category.Category
import com.example.blinkit.databinding.FragmentHomeBinding
import com.example.blinkit.presentation.fragments.home.adapter.CategoryAdapter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.min

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding(FragmentHomeBinding::bind)

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crashlytics.log("HomeFragment started")

        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_flashLightFragment)
        }
        binding.tvSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_batteryFragment)
        }

        binding.crashlitycs.setOnClickListener {
            crashlytics.log("Crashlytics button clicked")
            throw Exception("My first Exception")
        }

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.orange)
        requireActivity().window.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.orange)

        setAllCategories()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }

    }

    private fun setAllCategories() {
        val category = ArrayList<Category>()
        val size = min(Constants.allProductsCategory.size, Constants.allProductsCategoryIcon.size)
        for (i in 0 until size) {
            category.add(
                Category(
                    Constants.allProductsCategory[i],
                    Constants.allProductsCategoryIcon[i]
                )
            )
        }
        binding.rvCategories.adapter = CategoryAdapter(category)
    }

}