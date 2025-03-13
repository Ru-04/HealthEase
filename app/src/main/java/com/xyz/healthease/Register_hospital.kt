package com.xyz.healthease

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.messaging.FirebaseMessaging
import com.xyz.healthease.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class Register_hospital : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_hospital)

        // UI references
        val etHospitalName = findViewById<EditText>(R.id.hospital_name)
        val etAddress = findViewById<EditText>(R.id.address)
        val etDateOfEstablish = findViewById<EditText>(R.id.date_of_establishment)
        val etAdminID = findViewById<EditText>(R.id.admin_id)
        val etLicenceNo = findViewById<EditText>(R.id.licence_no)
        val etCapacity = findViewById<EditText>(R.id.capacity)
        val etContactNo = findViewById<EditText>(R.id.contact_no)
        val progressBar = findViewById<ProgressBar>(R.id.progress_circular1)
        val signUpButton = findViewById<Button>(R.id.btn_sign_up)
        progressBar.visibility = View.GONE // Initially hide the ProgressBar

        // DatePicker for Date of Establishment
        etDateOfEstablish.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etDateOfEstablish.clearFocus()
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                        )
                        etDateOfEstablish.setText(formattedDate)
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }
        }

        // Sign Up Button Click Listener
        signUpButton.setOnClickListener {
            val hospitalName = etHospitalName.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val dateOfEstablish = etDateOfEstablish.text.toString().trim()
            val adminID = etAdminID.text.toString().trim()
            val licenceNo = etLicenceNo.text.toString().trim()
            val capacity = etCapacity.text.toString().trim().toIntOrNull() ?: 0
            val contactNo = etContactNo.text.toString().trim()

            if (hospitalName.isEmpty() || licenceNo.isEmpty() || address.isEmpty() ||
                contactNo.isEmpty() || dateOfEstablish.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!dateOfEstablish.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                Toast.makeText(this, "Invalid date format (use YYYY-MM-DD)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching token failed", task.exception)
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "FCM Token error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                val token = task.result

                val hospital = Hospital(
                    License_No = licenceNo,
                    Hospital_Name = hospitalName,
                    Address = address,
                    Contact_No = contactNo,
                    Email = adminID,
                    Capacity = capacity,
                    Date_Of_Establish = dateOfEstablish,
                    Fcm_token = token
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiClient.getApiService().saveHospital(hospital)
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            if (response.hospitalId.isNotEmpty()) {
                                // Store hospitalId in SharedPreferences
                                val sharedPreferences = getSharedPreferences("HealtheasePrefs", MODE_PRIVATE)
                                sharedPreferences.edit().putString("hospitalId", response.hospitalId).apply()
                                Toast.makeText(this@Register_hospital, response.message, Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@Register_hospital, homepage_hospital::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@Register_hospital, "Unexpected response", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this@Register_hospital, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
