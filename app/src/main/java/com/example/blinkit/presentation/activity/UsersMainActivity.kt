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
import androidx.lifecycle.lifecycleScope
import com.aghajari.zoomhelper.ZoomHelper
import com.example.blinkit.Blinkit
import com.example.blinkit.R
import com.example.blinkit.databinding.ActivityMainBinding
import com.example.blinkit.databinding.ActivityUsersMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsersMainActivity : AppCompatActivity() {

    private val binding: ActivityUsersMainBinding by lazy { ActivityUsersMainBinding.inflate(layoutInflater) }
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
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

    override fun onResume() {
        super.onResume()
        Log.d("UserAppTimers", "onResume called. Updating time spent...")
        startTimer()
    }

    override fun onPause() {
        super.onPause()
        Log.d("UserAppTimers", "onPause called. Stopping timer...")
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

        Log.d("UserAppTimers", "Current Timer: $formattedTime, Date: $currentDate")
    }

    @SuppressLint("SwitchIntDef")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {}
            Configuration.ORIENTATION_PORTRAIT -> {}
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ZoomHelper.getInstance().dispatchTouchEvent(ev!!, this) || super.dispatchTouchEvent(
            ev
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}