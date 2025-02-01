package com.xyz.healthease.signinPatient

import com.xyz.healthease.otp.GenerateOTP



import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.R

lateinit var patientBtn: Button
lateinit var hospitalBtn: Button
lateinit var doctorBtn: Button

class SigningAs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_signing_as)

        // Initialize buttons
        patientBtn = findViewById(R.id.patient_btn)
        hospitalBtn = findViewById(R.id.hospital_btn)
        doctorBtn = findViewById(R.id.doctor_btn)

        // Handle role selection
        patientBtn.setOnClickListener {
            navigateToLogin("patient")
        }
        hospitalBtn.setOnClickListener {
            navigateToLogin("hospital")
        }
        doctorBtn.setOnClickListener {
            navigateToLogin("doctor")
        }
    }

    private fun navigateToLogin(role: String) {
        val intent = Intent(this@SigningAs, GenerateOTP::class.java)
        intent.putExtra("role", role) // Pass the role to LoginActivity
        startActivity(intent)
    }
}
