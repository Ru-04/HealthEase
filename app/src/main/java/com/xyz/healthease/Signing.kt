package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
lateinit var sign_up:Button
lateinit var sign_in:Button
class Signing : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signing)
        supportActionBar?.hide()
        sign_up = findViewById(R.id.sign_up)
        sign_in = findViewById(R.id.sign_in )

        sign_in.setOnClickListener {
           val intent =Intent(this@Signing,Signing_as::class.java)
            startActivity(intent)
        }
        sign_up.setOnClickListener {
            val intent =Intent(this@Signing,signupas::class.java)
            startActivity(intent)
        }
    }
}