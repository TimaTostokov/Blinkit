package com.example.blinkit.presentation.fragments.auth.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.blinkit.core.common.Extensions
import com.example.blinkit.data.remote.model.users.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
) : ViewModel() {

    private val _verificationId = MutableStateFlow<String?>(null)

    private val _otpSend = MutableStateFlow(false)
    val otpSend = _otpSend

    private val _isSignedInSuccessfully = MutableStateFlow(false)
    val isSignedInSuccessfully = _isSignedInSuccessfully

    fun sendOTP(userNumber: String, activity: Activity) {
        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {}

            override fun onVerificationFailed(p0: FirebaseException) {}

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _verificationId.value = verificationId
                _otpSend.value = true
            }
        }

        val options = PhoneAuthOptions.newBuilder(Extensions.getAuthInstance())
            .setPhoneNumber("+996$userNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(otp: String, userNumber: String, user: User) {
        val credential = PhoneAuthProvider.getCredential(_verificationId.value.toString(), otp)
        Extensions.getAuthInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(user.uid!!).setValue(user)
                _isSignedInSuccessfully.value = true
                }
                else {

                }
            }
    }

}