package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register_doctor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_doctor)

        val signup_button: Button = findViewById(R.id.btn_sign_up)

        signup_button.setOnClickListener {
            val intent= Intent(this,doctor_homepage::class.java)
            startActivity(intent)
        }

    }
}