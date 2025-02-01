package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EnterOtp : AppCompatActivity() {
    private lateinit var otpInput: EditText
    private lateinit var verifyButton: Button
    private lateinit var resendText: TextView
    private lateinit var progressCircular: ProgressBar
    private lateinit var apiService: ApiService
    private var phone: String? = null
    private var resendEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_enter_otp)

        // Initialize UI components
        otpInput = findViewById(R.id.box)
        verifyButton = findViewById(R.id.click)
        resendText = findViewById(R.id.resend)
        progressCircular = findViewById(R.id.progress_circular)
        progressCircular.visibility = View.GONE

        // Initialize Retrofit service
        apiService = ApiClient.getApiService()

        // Get phone number from intent
        phone = intent.getStringExtra("phone")

        // Handle OTP verification
        verifyButton.setOnClickListener {
            val otp = otpInput.text.toString().trim()
            if (otp.isEmpty()) {
                otpInput.error = "Enter OTP"
            } else if (phone != null) {
                verifyOtp(phone!!, otp)
            } else {
                Toast.makeText(this, "Phone number is missing", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Resend OTP
        resendText.setOnClickListener {
            if (!resendEnabled) {
                Toast.makeText(this, "Please wait before resending", Toast.LENGTH_SHORT).show()
            } else if (phone != null) {
                resendOtp(phone!!)
            }
        }

        startResendCountdown()
    }

    private fun verifyOtp(phone: String, otp: String) {
        progressCircular.visibility = View.VISIBLE
        verifyButton.isEnabled = false

        // Prepare the request body
        val request = ApiService.OtpRequest(phone, otp)
        val call = apiService.verifyOtp(request)

        // Make the network call
        call.enqueue(object : Callback<ApiService.ApiResponse> {
            override fun onResponse(
                call: Call<ApiService.ApiResponse>,
                response: Response<ApiService.ApiResponse>
            ) {
                progressCircular.visibility = View.GONE
                verifyButton.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val message = response.body()?.message ?: "Verification successful"
                    Toast.makeText(this@EnterOtp, message, Toast.LENGTH_SHORT).show()

                    val patientId = response.body()?.patientId
                    println("patient id received from the server: $patientId")
                    if (patientId != null) {
                        // Navigate to the homepage on successful verification
                        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
                        sharedPreferences.edit().putString("PATIENT_ID", patientId).apply()
                        println("patientId: $patientId")
                    } else {
                        Toast.makeText(this@EnterOtp, "Patient ID not found", Toast.LENGTH_SHORT).show()
                    }

                    // Navigate to the homepage on successful verification
                    val intent = Intent(this@EnterOtp, homepage_patient::class.java)
                    intent.putExtra("phone", phone)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@EnterOtp,
                        "Invalid OTP or verification failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiService.ApiResponse>, t: Throwable) {
                progressCircular.visibility = View.GONE
                verifyButton.isEnabled = true
                Toast.makeText(this@EnterOtp, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resendOtp(phone: String) {
        progressCircular.visibility = View.VISIBLE

        val request = ApiService.PhoneRequest(phone)
        val call = apiService.sendOtp(request)

        call.enqueue(object : Callback<ApiService.ApiResponse> {
            override fun onResponse(
                call: Call<ApiService.ApiResponse>,
                response: Response<ApiService.ApiResponse>
            ) {
                progressCircular.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@EnterOtp, "OTP resent successfully", Toast.LENGTH_SHORT).show()
                    startResendCountdown()
                } else {
                    Toast.makeText(this@EnterOtp, "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiService.ApiResponse>, t: Throwable) {
                progressCircular.visibility = View.GONE
                Toast.makeText(this@EnterOtp, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startResendCountdown() {
        resendEnabled = false
        resendText.text = "Resend OTP in 60 sec"

        // Use Handler to manage countdown
        Handler(Looper.getMainLooper()).postDelayed({
            resendEnabled = true
            resendText.text = "Resend OTP"
        }, 60000)
    }
}
