package com.example.blinkit.presentation.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.blinkit.Blinkit
import com.example.blinkit.core.common.Constants.APP_ACTIVITY
import com.example.blinkit.core.common.ScreenCaptureService
import com.example.blinkit.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var isTimerRunning = false
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val mediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(this, ScreenCaptureService::class.java).apply {
                action = ScreenCaptureService.ACTION_START
                putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, result.data)
            }
            startForegroundService(intent)
        } else {
            Log.e("MainActivity", "Не удалось получить разрешение на захват экрана")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        APP_ACTIVITY = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE)
        } else {
            requestScreenCapturePermission()
        }
        if (resources.configuration.smallestScreenWidthDp < 600) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        binding.startCaptureButton.setOnClickListener {
            startScreenCapture()
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

    private fun hasPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                requestScreenCapturePermission()
            } else {
                // Разрешения не предоставлены
                Toast.makeText(this, "Необходимы разрешения для работы приложения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestScreenCapturePermission() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    private fun startCapture() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            image?.let {
                val planes = it.planes
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride: Int = planes[0].pixelStride
                val rowStride: Int = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * metrics.widthPixels

                val bitmap = Bitmap.createBitmap(
                    metrics.widthPixels + rowPadding / pixelStride,
                    metrics.heightPixels,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)

                saveBitmap(bitmap)

                it.close()
                stopCapture()
            }
        }, null)
    }

    private fun stopCapture() {
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null
        imageReader?.close()
        imageReader = null
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val filename = "Screenshot_${System.currentTimeMillis()}.png"
        val fos: FileOutputStream
        try {
            val imagesDir = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "Screenshots"
            )
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val imageFile = File(imagesDir, filename)
            fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()

            // Добавляем изображение в галерею
            MediaStore.Images.Media.insertImage(
                contentResolver, imageFile.absolutePath, filename, null
            )

            Log.d("MainActivity", "Скриншот сохранен: ${imageFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка при сохранении скриншота", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startScreenCapture() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainAppTimers", "onResume called. Updating time spent...")
        startPeriodicScreenCapture()
        startTimer()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainAppTimers", "onPause called. Stopping timer...")
        stopPeriodicScreenCapture()
        stopTimer()
    }

    private fun startPeriodicScreenCapture() {
        handler = Handler(mainLooper)
        runnable = object : Runnable {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                startScreenCapture()
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(runnable)
    }

    private fun stopPeriodicScreenCapture() {
        handler.removeCallbacks(runnable)
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

        val currentDate =
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
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

    companion object {
        private const val REQUEST_PERMISSION_CODE = 1001
    }

}