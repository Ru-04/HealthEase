package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.signinPatient.SigningAs


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        supportActionBar?.hide()

        Handler().postDelayed({
            // Check registration status and navigate accordingly
             val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
            val patientId = sharedPreferences.getString("PATIENT_ID", null)

            if (patientId == null) {
                // No Patient ID found → Go to registration/login screen
                startActivity(Intent(this, SignupAs::class.java))
            } else {
                // Patient ID exists → Go to homepage
                startActivity(Intent(this, homepage_patient::class.java))
            }
            finish()
        }, 3000) // 3-second splash delay
    }
}
