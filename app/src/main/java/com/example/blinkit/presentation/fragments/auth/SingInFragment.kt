package com.example.blinkit.presentation.fragments.auth

import android.animation.ValueAnimator
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.core.common.Extensions.showToast
import com.example.blinkit.databinding.FragmentSingInBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingInFragment : Fragment() {

    private var _binding: FragmentSingInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        startStableInfiniteScroll()
        getUserNumber()
        onContinueButtonClick()
    }

    private fun onContinueButtonClick() {
        binding.btnContinue.setOnClickListener {
            val number = binding.etUserNumber.text.toString().trim()

            if (number.isEmpty() || number.length < 9) {
                showToast("Please enter valid phone number")
            } else {
                val bundle = Bundle().also {
                    it.putString("number", number)
                }
                findNavController().navigate(R.id.action_singInFragment_to_OTPFragment, bundle)
                binding.etUserNumber.text?.clear()
            }
        }
    }

    private fun getUserNumber() {
        binding.etUserNumber.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(number: CharSequence?, start: Int, before: Int, count: Int) {
                val len = number?.length

                if (len == 9) {
                    binding.btnContinue.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green
                        )
                    )
                } else {
                    binding.btnContinue.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.grayish_blue
                        )
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun startStableInfiniteScroll() {
        val imageView = binding.ivLoginImage
        val drawable = imageView.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        imageView.scaleType = ImageView.ScaleType.MATRIX
        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        val paint = Paint().apply {
            isAntiAlias = true
            this.shader = shader
        }

        val customView = object : View(imageView.context) {
            private val matrix = Matrix()
            private var offset = 0f

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                matrix.reset()
                matrix.postTranslate(-offset, 0f)
                paint.shader.setLocalMatrix(matrix)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }

            fun updateOffset(newOffset: Float) {
                offset = newOffset % bitmap.width
                invalidate()
            }
        }

        val parent = imageView.parent as ViewGroup
        parent.addView(customView, imageView.layoutParams)
        parent.removeView(imageView)

        val animator = ValueAnimator.ofFloat(0f, bitmap.width.toFloat())
        animator.duration = 5000L
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = ValueAnimator.INFINITE

        animator.addUpdateListener { animation ->
            val offset = animation.animatedValue as Float
            customView.updateOffset(offset)
        }

        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.etUserNumber.text?.clear()
        _binding = null
    }

}