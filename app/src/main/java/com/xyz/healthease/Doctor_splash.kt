package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Doctor_splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_doctor_splash)
        Handler().postDelayed({
            val intent = Intent(this@Doctor_splash,GenerateOTP::class.java)
            startActivity(intent)
            finish()
        },1000)
    }
}