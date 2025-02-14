package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.signinPatient.SigningAs

class Choice : AppCompatActivity() {
    private lateinit var AddChild: Button
    private lateinit var AddFamily2: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)
        supportActionBar?.hide()

        AddFamily2 = findViewById(R.id.add_family)
        AddChild = findViewById(R.id.add_child)

        AddChild.setOnClickListener {
            val intent = Intent(this@Choice, Add_child::class.java)
            startActivity(intent)
        }

        AddFamily2.setOnClickListener {
            val intent = Intent(this@Choice, AddFamily::class.java)
            startActivity(intent)
        }
    }
}