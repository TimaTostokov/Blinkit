package com.example.blinkit.core.unavailable

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class Connection(private val connectivityManager: ConnectivityManager) {

    private val _connectionState = MutableStateFlow(isConnected())
    val connectionState: StateFlow<Boolean> = _connectionState

    private var isNetworkCallbackRegistered = false

    constructor(application: Application) : this(
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    )

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _connectionState.value = true
        }

        override fun onLost(network: Network) {
            _connectionState.value = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun isConnected(): Boolean {
        return connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } ?: false
    }

    suspend fun start() {
        withContext(Dispatchers.Main) {
            _connectionState.value = isConnected()
        }

        if (!isNetworkCallbackRegistered) {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                networkCallback
            )
            isNetworkCallbackRegistered = true
        }
    }

    fun stop() {
        if (isNetworkCallbackRegistered) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isNetworkCallbackRegistered = false
        }
    }

}