package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
lateinit var patient_btn1 : Button
lateinit var hospital_btn1 : Button
lateinit var doctor_btn1 : Button

class signupas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signupas)
        supportActionBar?.hide()
        patient_btn1 = findViewById(R.id.btn1)
        hospital_btn1 = findViewById(R.id.hospital1)
        doctor_btn1 = findViewById(R.id.doctor1)

        patient_btn1.setOnClickListener {
            val intent = Intent(this@signupas,Register_patient::class.java)
            startActivity(intent)
        }
        hospital_btn1.setOnClickListener {
            val intent = Intent(this@signupas,Register_patient::class.java)
            startActivity(intent)
        }
        doctor_btn1.setOnClickListener {
            val intent = Intent(this@signupas,Register_doctor::class.java)
            startActivity(intent)
        }
    }
}