package com.example.blinkit.presentation.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blinkit.R
import com.example.blinkit.core.common.Constants.APP_ACTIVITY
import com.example.blinkit.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        APP_ACTIVITY = this

        if (resources.configuration.smallestScreenWidthDp < 600) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun hideBottomNavigationView(view: BottomNavigationView) {
        if (view.visibility == View.GONE) return
        view.clearAnimation()
        view.animate().translationY(view.height.toFloat()).setDuration(300)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                view.visibility = View.GONE
            }
    }

    private fun showBottomNavigationView(view: BottomNavigationView) {
        if (view.visibility == View.VISIBLE) return
        view.visibility = View.VISIBLE
        view.clearAnimation()
        view.animate().translationY(0f).setDuration(300)
            .setInterpolator(DecelerateInterpolator())
    }

    @SuppressLint("SwitchIntDef")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {}
            Configuration.ORIENTATION_PORTRAIT -> {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}