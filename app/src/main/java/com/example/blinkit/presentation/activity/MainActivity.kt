package com.example.blinkit.presentation.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.aghajari.zoomhelper.ZoomHelper
import com.example.blinkit.Blinkit
import com.example.blinkit.core.common.Constants.APP_ACTIVITY
import com.example.blinkit.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        APP_ACTIVITY = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        if (resources.configuration.smallestScreenWidthDp < 600) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d("MainAppTimers", "onResume called. Updating time spent...")
        startTimer()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainAppTimers", "onPause called. Stopping timer...")
        stopTimer()
    }

    private fun startTimer() {
        isTimerRunning = true
        lifecycleScope.launch {
            while (isTimerRunning) {
                updateTimeSpent()
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        isTimerRunning = false
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateTimeSpent() {
        val app = application as Blinkit
        val timeSpentInMillis = app.totalTimeSpent + (System.currentTimeMillis() - app.startTime)

        val timeSpentInSeconds = timeSpentInMillis / 1000
        val timeSpentInMinutes = timeSpentInSeconds / 60
        val timeSpentInHours = timeSpentInMinutes / 60

        val formattedTime = String.format(
            "%02d:%02d:%02d",
            timeSpentInHours,
            timeSpentInMinutes % 60,
            timeSpentInSeconds % 60
        )

        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        binding.timeSpentTextView.text = """
        Время в приложении: $formattedTime
        Дата: $currentDate
    """.trimIndent()

        Log.d("MainAppTimers", "Current Timer: $formattedTime, Date: $currentDate")
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