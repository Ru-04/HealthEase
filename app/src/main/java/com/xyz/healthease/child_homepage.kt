package com.xyz.healthease

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class child_homepage : AppCompatActivity() {

    private lateinit var childIdTextView: TextView
    private lateinit var upbutton: Button
    private lateinit var mediButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_child_homepage)

        childIdTextView = findViewById(R.id.child_id_text)
        upbutton=findViewById(R.id.upload_button)
        mediButton=findViewById(R.id.medivault_button)

        // Retrieve childId from SharedPreferences
        val childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE)
        val childId = childPrefs.getString("CHILD_ID", null)
        val sharedPreferences = getSharedPreferences("HealthEasePrefs", Context.MODE_PRIVATE)

        if (!childId.isNullOrEmpty()) {
            childIdTextView.text = "Child ID: $childId"
        } else {
            Toast.makeText(this, "No Child ID found", Toast.LENGTH_SHORT).show()
        }

       upbutton.setOnClickListener {
            val intent = Intent(this, child_camera::class.java)
            startActivity(intent) // Starts the SecondActivity
        }
        mediButton.setOnClickListener{
            val intent = Intent(this, ChildReportsActivity::class.java)
            startActivity(intent)
        }
    }
}