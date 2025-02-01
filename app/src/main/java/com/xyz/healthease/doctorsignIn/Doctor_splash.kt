package com.xyz.healthease.doctorsignIn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.R
import com.xyz.healthease.otp.GenerateOTP

class Doctor_splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_doctor_splash)
        Handler().postDelayed({
            val intent = Intent(this@Doctor_splash, GenerateOTP::class.java)
            startActivity(intent)
            finish()
        },1000)
    }
}