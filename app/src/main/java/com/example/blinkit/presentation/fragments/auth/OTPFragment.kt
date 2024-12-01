package com.example.blinkit.presentation.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkit.core.common.Extensions
import com.example.blinkit.core.common.Extensions.hideDialog
import com.example.blinkit.core.common.Extensions.showToast
import com.example.blinkit.core.common.Extensions.snackbar
import com.example.blinkit.data.remote.model.users.User
import com.example.blinkit.databinding.FragmentOTPBinding
import com.example.blinkit.presentation.activity.UsersMainActivity
import com.example.blinkit.presentation.fragments.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OTPFragment : Fragment() {

    private var _binding: FragmentOTPBinding? = null
    private val binding get() = _binding!!

    private var userNumber: String? = null

    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOTPBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tbOtpFragment.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        getUserNumber()
        customizingEnteringOTP()
        sendOTP()
        onLoginButtonClicked()
    }

    private fun onLoginButtonClicked() {
        binding.btnLogin.setOnClickListener {
            Extensions.showDialog(requireContext(),"Signing you...")
            val editText = arrayOf(
                binding.etOtp1,
                binding.etOtp2,
                binding.etOtp3,
                binding.etOtp4,
                binding.etOtp5,
                binding.etOtp6
            )
            val otp = editText.joinToString("") {
                it.text.toString()
            }
            if (otp.length < editText.size) {
                snackbar("Please enter right OTP")
                hideDialog()
            } else {
                editText.forEach { it.text?.clear(); it.clearFocus() }
                verifyOTP(otp)
            }
        }
    }

    private fun verifyOTP(otp: String) {
        val user = User(
            uid = Extensions.getCurrentUserId(),
            userPhoneNumber = userNumber,
            userAddress = null
        )
        viewModel.signInWithPhoneAuthCredential(otp, userNumber.toString(), user)
        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect {
                if (it) {
                    hideDialog()
                    showToast("Logged In...")
                    startActivity(Intent(requireActivity(), UsersMainActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

    private fun sendOTP() {
        Extensions.showDialog(requireContext(), "Sending OTP...")
        viewModel.apply {
            viewModel.sendOTP(userNumber.toString().trim(), requireActivity())
            lifecycleScope.launch {
                otpSend.collect { otpSend ->
                    if (otpSend) {
                        hideDialog()
                        showToast("OPT send to the number...")
                    }
                }
            }
        }
    }

    private fun customizingEnteringOTP() {
        val editTexts = arrayOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        } else if (s.isEmpty()) {
                            if (i > 0) {
                                editTexts[i - 1].requestFocus()
                            }
                        }
                    }
                }
            })
        }
    }

    private fun getUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("number").toString().trim()
        binding.tvUserNumber.text = userNumber
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}