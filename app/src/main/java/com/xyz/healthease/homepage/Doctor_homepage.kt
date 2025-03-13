package com.xyz.healthease.homepage

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.AddPatient
import com.xyz.healthease.Logout
import com.xyz.healthease.R
lateinit var  profile :ImageView
lateinit var RequestAcess: Button

class doctor_homepage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_homepage)
        profile= findViewById(R.id.profile1)
        RequestAcess =findViewById(R.id.request_button1)
        val sharedPreferences = getSharedPreferences("HealthEasePrefs", Context.MODE_PRIVATE)
        val doctorId = sharedPreferences.getString("DOCTOR_ID", "")

        Log.d("DoctorHomePage", "Retrieved Doctor ID: $doctorId")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermission()
            }
        }

        profile.setOnClickListener {
            val intent =
                Intent(this@doctor_homepage, Logout::class.java)
            startActivity(intent)
        }

        RequestAcess.setOnClickListener {
            val intent = Intent(this@doctor_homepage,AddPatient::class.java)
            startActivity(intent)
        }
    }
    private fun requestPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}