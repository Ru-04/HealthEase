package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit




class Enter_otp : AppCompatActivity() {

    private lateinit var phoneNumber: String
    private var timeoutSeconds: Long = 60L
    private  lateinit var resendtoken : PhoneAuthProvider.ForceResendingToken
    private lateinit var storedverificationId :String
    private lateinit var box :EditText
    private lateinit var click :Button
    private lateinit var resend :TextView
    val db = Firebase.firestore
    private val mAuth: FirebaseAuth= FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_enter_otp)

        click = findViewById(R.id.click)
        box = findViewById(R.id.box)
        resend = findViewById(R.id.resend)

        phoneNumber = intent.extras?.getString("phone") ?: ""
        Log.d("PhoneNumber", "Phone number from Intent: $phoneNumber") // Print phone number
        if (phoneNumber.isBlank()) {
            Log.e("PhoneNumber", "Phone number is empty or missing")
        }else{
        sendOtp(phoneNumber, false)
        }

        click.setOnClickListener {
            val enterOtp = box.text.toString()
            val credential = PhoneAuthProvider.getCredential(storedverificationId, enterOtp)
            signIn(credential)
        }
        resend.setOnClickListener {
            sendOtp(phoneNumber, true)
        }
    }

        private fun sendOtp(phoneNumber: String, isResend: Boolean) {
            startResendTimer()
            val builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        signIn(phoneAuthCredential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(applicationContext, "OTP verification failed",Toast.LENGTH_LONG).show()
                    }

                    override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(s, forceResendingToken)
                        storedverificationId = s
                        resendtoken = forceResendingToken
                        Toast.makeText(applicationContext, "OTP sent successfully",Toast.LENGTH_LONG).show()
                    }
                })

            if(isResend)
            {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendtoken).build())
            }
            else
            {
                PhoneAuthProvider.verifyPhoneNumber(builder.build())
            }

        }
        fun signIn(phoneAuthCredential: PhoneAuthCredential) {
            mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@Enter_otp, homepage::class.java)
                    intent.putExtra("phone", phoneNumber)
                    startActivity(intent)
                } else {
                    Toast.makeText(applicationContext, "OTP verification failed",Toast.LENGTH_LONG).show()
                }
            }
        }


    private fun startResendTimer() {
        resend.isEnabled = false

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                timeoutSeconds--
                resend.text = "Resend OTP in $timeoutSeconds seconds"
                if (timeoutSeconds <= 0) {
                    timeoutSeconds = 60L
                    handler.removeCallbacks(this) // Stop the timer
                    resend.isEnabled = true
                    resend.text = "Resend OTP" // Reset button text
                } else {
                    handler.postDelayed(this, 1000) // Schedule next execution
                }
            }
        }
        handler.post(runnable) // Start the timer
    }

}