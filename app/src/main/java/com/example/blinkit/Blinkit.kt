package com.example.blinkit

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Blinkit : Application() {

    var startTime: Long = 0
    var totalTimeSpent: Long = 0
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        loadTotalTimeSpent()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var foregroundActivityCount = 0

            override fun onActivityStarted(activity: android.app.Activity) {
                if (foregroundActivityCount == 0) {
                    onAppForegrounded()
                }
                foregroundActivityCount++
            }

            override fun onActivityStopped(activity: android.app.Activity) {
                foregroundActivityCount--
                if (foregroundActivityCount == 0) {
                    onAppBackgrounded()
                }
            }

            override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {}
            override fun onActivityResumed(activity: android.app.Activity) {}
            override fun onActivityPaused(activity: android.app.Activity) {}
            override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(activity: android.app.Activity) {}
        })
    }

    private fun saveTotalTimeSpent() {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("TOTAL_TIME_SPENT", totalTimeSpent)
            apply()
        }
    }

    private fun loadTotalTimeSpent() {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        totalTimeSpent = sharedPref.getLong("TOTAL_TIME_SPENT", 0)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun onAppForegrounded() {
        startTime = System.currentTimeMillis()
    }

    fun onAppBackgrounded() {
        val endTime = System.currentTimeMillis()
        totalTimeSpent += (endTime - startTime)
        saveTotalTimeSpent()
    }

}