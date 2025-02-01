package com.xyz.healthease.otp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hbb20.CountryCodePicker
import com.xyz.healthease.ApiService
import com.xyz.healthease.R
import com.xyz.healthease.api.ApiClient
import com.xyz.healthease.EnterOtp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GenerateOTP : AppCompatActivity() {

    private lateinit var generateButton: Button
    private lateinit var countryCodePicker: CountryCodePicker
    private lateinit var phoneEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_generate_otp)

        // Initialize UI components
        generateButton = findViewById(R.id.generate)
        countryCodePicker = findViewById(R.id.codePicker)
        phoneEditText = findViewById(R.id.ph_txt)
        progressBar = findViewById(R.id.progress_circular)

        progressBar.visibility = View.GONE
        countryCodePicker.registerCarrierNumberEditText(phoneEditText)

        // Initialize the API service
        apiService = ApiClient.getApiService()

        // Set button click listener
        generateButton.setOnClickListener {
            validateAndSendOtp()
        }
    }

    private fun validateAndSendOtp() {
        if (!countryCodePicker.isValidFullNumber) {
            phoneEditText.error = "Invalid phone number"
            return
        }
        val phoneNumber = countryCodePicker.fullNumberWithPlus
        sendOtp(phoneNumber)
    }

    private fun sendOtp(phone: String) {
        // Show loading spinner
        progressBar.visibility = View.VISIBLE
        generateButton.isEnabled = false

        val request = ApiService.PhoneRequest(phone)
        val call = apiService.sendOtp(request)

        call.enqueue(object : Callback<ApiService.ApiResponse> {
            override fun onResponse(
                call: Call<ApiService.ApiResponse>,
                response: Response<ApiService.ApiResponse>
            ) {
                progressBar.visibility = View.GONE
                generateButton.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val message = response.body()?.message ?: "OTP Sent Successfully"
                    Toast.makeText(this@GenerateOTP, message, Toast.LENGTH_SHORT).show()

                    // Navigate to EnterOTP activity
                    val intent = Intent(this@GenerateOTP, EnterOtp::class.java)
                    intent.putExtra("phone", phone)
                    startActivity(intent)
                } else {
                    showToast("Failed to send OTP. Please try again.")
                }
            }

            override fun onFailure(call: Call<ApiService.ApiResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                generateButton.isEnabled = true
                showToast("Error: ${t.localizedMessage}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this@GenerateOTP, message, Toast.LENGTH_SHORT).show()
    }
}
