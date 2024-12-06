package com.example.blinkit.presentation.fragments.receiverbattery

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.blinkit.R
import com.example.blinkit.databinding.FragmentBatteryBinding

class BatteryFragment : Fragment() {

    private var _binding: FragmentBatteryBinding? = null
    private val binding get() = _binding!!

    private val batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            val batteryLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
            val batteryIsCharging = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            val batteryTemperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.div(10) ?: 0
            val batteryVoltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)?.div(1000) ?: 0
            val batteryTechnology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

            binding.apply {
                val batteryTemperatureF = (batteryTemperature * 9 / 5) + 32
                batteryProgress.setProgress(100 - batteryLevel)
                tvBatteryLevel.text = "$batteryLevel%"
                tvPlugInValue.text = if (batteryIsCharging == 0) getString(R.string.plug_out) else getString(R.string.plug_in)
                tvVoltageValue.text = "$batteryVoltage V"
                tvTemperatureValue.text = "$batteryTemperature °C"
                tvTechnologyValue.text = batteryTechnology
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBatteryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(batteryInfoReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}