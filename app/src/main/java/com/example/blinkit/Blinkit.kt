package com.example.blinkit

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.google.firebase.FirebaseApp

class Blinkit : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}