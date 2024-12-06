package com.example.blinkit.presentation.fragments.flishlight

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.core.common.viewBinding
import com.example.blinkit.databinding.FragmentFlashLightBinding

class FlashLightFragment : Fragment(R.layout.fragment_flash_light) {

    private val binding by viewBinding(FragmentFlashLightBinding::bind)

    private var originalImage: Bitmap? = null
    private var newImage: Bitmap? = null
    private var isFonaricOn: Boolean = false

    private var newWidth: Int = 0
    private var newHeight: Int = 0

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    private var imageView: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_flash_light, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
        }

        cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = try {
            cameraManager.cameraIdList.firstOrNull() ?: throw RuntimeException("No camera found")
        } catch (e: CameraAccessException) {
            throw RuntimeException("Error accessing camera", e)
        }

        imageView = view.findViewById(R.id.fonarik)

        setStartPosition()
        uploadFonaricImage()
        viewRightPartOfImage()

        originalImage?.let {
            newWidth = it.width / 2
            newHeight = it.height
        }

        isFonaricOn = false

        imageView?.setOnClickListener {
            toggleFlashlight()
        }
    }

    private fun toggleFlashlight() {
        isFonaricOn = !isFonaricOn
        if (isFonaricOn) {
            originalImage?.let {
                newImage = Bitmap.createBitmap(it, 0, 0, newWidth, newHeight)
                imageView?.setImageBitmap(newImage)
            }
        } else {
            viewRightPartOfImage()
        }
        setTorchMode(isFonaricOn)
    }

    private fun viewRightPartOfImage() {
        originalImage?.let {
            newImage = Bitmap.createBitmap(it, newWidth, 0, newWidth, newHeight)
            imageView?.setImageBitmap(newImage)
        }
    }

    private fun uploadFonaricImage() {
        originalImage = BitmapFactory.decodeResource(resources, R.drawable.switcher)
    }

    private fun setStartPosition() {
        originalImage = BitmapFactory.decodeResource(resources, R.drawable.switcher)
        originalImage?.let {
            newWidth = it.width / 2
            newHeight = it.height
            newImage = Bitmap.createBitmap(it, newWidth, 0, newWidth, newHeight)
            imageView?.setImageBitmap(newImage)
        }
        isFonaricOn = false
    }

    private fun setTorchMode(isOn: Boolean) {
        try {
            cameraManager.setTorchMode(cameraId, isOn)
        } catch (e: CameraAccessException) {
            Log.e("MainFragment", "Failed to set torch mode", e)
        }
    }

}