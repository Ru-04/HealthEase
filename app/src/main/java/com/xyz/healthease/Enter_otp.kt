package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.api.ApiClient
import com.xyz.healthease.homepage.doctor_homepage
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
    private var role: String? = null
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
        role = intent.getStringExtra("role")
        Log.d("EnterOtp", "Phone: $phone, Role: $role")

        if (role == null) {
            Log.e("EnterOtp", "Role is missing!")
            Toast.makeText(this, "Role is missing!", Toast.LENGTH_SHORT).show()
            return  // Stop execution if role is null
        }

        Log.d("EnterOtp", "Phone: $phone, Role: $role")  // Debugging log

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

        if (role == null) {
            Toast.makeText(this, "Role is missing!", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare the request body
        val request = ApiService.OtpRequest(phone, otp,role!!)
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
                    val apiResponse = response.body()
                    val message = apiResponse?.message ?: "Verification successful"
                    val userId = apiResponse?.userId

                    Toast.makeText(this@EnterOtp, message, Toast.LENGTH_SHORT).show()

                    if (userId == null) {
                        Toast.makeText(this@EnterOtp, "User ID not found", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    // Store user ID in SharedPreferences
                    when (role?.lowercase()) {
                        "patient" -> {
                            editor.putString("PATIENT_ID", userId).apply()
                            Log.d("EnterOtp", "Patient ID stored: $userId")
                        }
                        "doctor" -> {
                            editor.putString("DOCTOR_ID", userId).apply()
                            Log.d("EnterOtp", "Doctor ID stored: $userId")
                        }
                        else -> {
                            Toast.makeText(this@EnterOtp, "Invalid role", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }

                    navigateToHomepage()
                } else {
                    Toast.makeText(this@EnterOtp, "Invalid OTP or verification failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiService.ApiResponse>, t: Throwable) {
                progressCircular.visibility = View.GONE
                verifyButton.isEnabled = true
                Toast.makeText(this@EnterOtp, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun navigateToHomepage() {
        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)

        val intent = when (role) {
            "doctor" -> {
                val doctorId = sharedPreferences.getString("DOCTOR_ID", null)
                Intent(this, doctor_homepage::class.java).apply {
                    putExtra("doctor_id", doctorId)
                }
            }
            "patient" -> {
                val patientId = sharedPreferences.getString("PATIENT_ID", null)
                Intent(this, homepage_patient::class.java).apply {
                    putExtra("patient_id", patientId)
                }
            }
            "hospital" -> Intent(this, homepage_hospital::class.java)
            else -> {
                Toast.makeText(this, "Invalid role! Cannot proceed.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    private fun resendOtp(phone: String) {
        progressCircular.visibility = View.VISIBLE

        val request = ApiService.PhoneRequest(phone, role)
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
        val countdownTime = 60000L // 60 seconds

        object : CountDownTimer(countdownTime, 1000) { // Update every second
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                resendText.text = "Resend OTP in $secondsRemaining sec"
            }

            override fun onFinish() {
                resendEnabled = true
                resendText.text = "Resend OTP"
            }
        }.start()
    }

}