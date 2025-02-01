package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.doctorsignIn.HospitalSplash
import com.xyz.healthease.homepage.doctor_homepage

lateinit var patient_btn1: Button
lateinit var hospital_btn1: Button
lateinit var doctor_btn1: Button

class SignupAs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signupas)
        supportActionBar?.hide()

        patient_btn1 = findViewById(R.id.btn1)
        hospital_btn1 = findViewById(R.id.hospital1)
        doctor_btn1 = findViewById(R.id.doctor1)

        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        patient_btn1.setOnClickListener {
            checkRegistrationAndNavigate(sharedPreferences, "patient")
        }
        hospital_btn1.setOnClickListener {
            checkRegistrationAndNavigate(sharedPreferences, "hospital")
        }
        doctor_btn1.setOnClickListener {
            checkRegistrationAndNavigate(sharedPreferences, "doctor")
        }
    }

    private fun checkRegistrationAndNavigate(sharedPreferences: android.content.SharedPreferences, role: String) {
        val isRegistered = sharedPreferences.getString("isRegistered", null)
        val userRole = sharedPreferences.getString("userRole", null)

        if (isRegistered == "true" && userRole == role) {
            // User is already registered for this role
            when (role) {
                "patient" -> startActivity(Intent(this, homepage_patient::class.java))
                "hospital" -> startActivity(Intent(this, doctor_homepage::class.java))
                "doctor" -> startActivity(Intent(this, HospitalSplash::class.java))
            }
        } else {
            // User not registered or role mismatch → Navigate to role-specific registration
            when (role) {
                "patient" -> startActivity(Intent(this, RegisterPatient::class.java))
                "hospital" -> startActivity(Intent(this, Register_doctor::class.java))
                "doctor" -> startActivity(Intent(this, Register_doctor::class.java))
            }
        }
    }
}
