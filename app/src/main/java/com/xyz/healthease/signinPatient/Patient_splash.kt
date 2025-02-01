package com.xyz.healthease.signinPatient

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.otp.GenerateOTP
import com.xyz.healthease.R

class Patient_splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_patient_splash2)
        Handler().postDelayed({
            val intent = Intent(this@Patient_splash, GenerateOTP::class.java)
            startActivity(intent)
            finish()
        },1000)
    }
}