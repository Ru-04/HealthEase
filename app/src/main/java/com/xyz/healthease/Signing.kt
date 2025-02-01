package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.signinPatient.SigningAs

class Signing : AppCompatActivity() {
    private lateinit var signUp: Button
    private lateinit var signIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signing)
        supportActionBar?.hide()

        signUp = findViewById(R.id.sign_up)
        signIn = findViewById(R.id.sign_in)

        signIn.setOnClickListener {
            val intent = Intent(this@Signing, SigningAs::class.java)
            startActivity(intent)
        }

        signUp.setOnClickListener {
            val intent = Intent(this@Signing, SignupAs::class.java)
            startActivity(intent)
        }
    }
}
