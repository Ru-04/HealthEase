package com.xyz.healthease

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import com.xyz.healthease.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class RegisterPatient : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_patient)

        val signupButton = findViewById<Button>(R.id.click2) // Replace with your actual button ID
        val etUsername = findViewById<EditText>(R.id.name)
        val etEmail = findViewById<EditText>(R.id.email)
        val etPhone = findViewById<EditText>(R.id.ph_no)
        val etGender = findViewById<EditText>(R.id.gender)
        val etDob = findViewById<EditText>(R.id.dob)
        val progressCircular = findViewById<ProgressBar>(R.id.progress_circular)

        // DatePicker for DOB
        etDob.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etDob.clearFocus()
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format(
                            Locale.US,
                            "%04d/%02d/%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                        )
                        etDob.setText(formattedDate)
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }
        }

        signupButton.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val gender = etGender.text.toString().trim()
            val dob = etDob.text.toString().trim()

            if (dob.matches(Regex("\\d{4}/\\d{2}/\\d{2}"))) { // Validate format YYYY/MM/DD
                val patient = Patient(
                    patientName = username,
                    dob = dob,
                    gender = gender,
                    email = email,
                    contactNo = phone
                )

                progressCircular.visibility = View.VISIBLE // Show progress bar

                // Send data to the Node.js server using Retrofit
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiClient.getApiService().savePatient(patient)
                        runOnUiThread {
                            progressCircular.visibility = View.GONE // Hide progress bar
                            if (response.message.isNotEmpty() && response.patientId.isNotEmpty()) {
                                val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
                                sharedPreferences.edit().putString("PATIENT_ID", response.patientId).apply()
                                Toast.makeText(
                                    this@RegisterPatient,
                                    "Registration Successful! Patient ID: ${response.patientId}",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Pass the patientId to the next screen
                                val intent = Intent(this@RegisterPatient, homepage_patient::class.java)
                                intent.putExtra("PATIENT_ID", response.patientId)
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    this@RegisterPatient,
                                    "Unexpected response: $response",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            progressCircular.visibility = View.GONE // Hide progress bar
                            Toast.makeText(this@RegisterPatient, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Invalid date format (use YYYY/MM/DD)", Toast.LENGTH_SHORT).show()
            }
        }
    }
}