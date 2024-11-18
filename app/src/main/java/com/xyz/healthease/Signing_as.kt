package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
lateinit var patient_btn :Button
lateinit var hospital_btn :Button
lateinit var doctor_btn :Button
class Signing_as : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_signing_as)
        patient_btn = findViewById(R.id.patient_btn)
        hospital_btn = findViewById(R.id.hospital_btn)
        doctor_btn = findViewById(R.id.doctor_btn)

        patient_btn.setOnClickListener {
            val intent = Intent(this@Signing_as,Patient_splash::class.java)
            startActivity(intent)
        }
        hospital_btn.setOnClickListener {
            val intent = Intent(this@Signing_as,HospitalSplash::class.java)
            startActivity(intent)
        }
        doctor_btn.setOnClickListener {
            val intent = Intent(this@Signing_as,Doctor_splash::class.java)
            startActivity(intent)
        }

    }
}