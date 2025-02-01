package com.xyz.healthease

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.app.DatePickerDialog
import android.content.Intent

import android.view.View
import android.widget.*
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
        val etHospitalID = findViewById<EditText>(R.id.hos_id)
        val etAddress = findViewById<EditText>(R.id.address)
        val etDateOfEstablish = findViewById<EditText>(R.id.date_of_establishment)
        val etAdminID = findViewById<EditText>(R.id.admin_id)
        val etDepartment = findViewById<EditText>(R.id.department)
        val etLicenceNo = findViewById<EditText>(R.id.licence_no)
        val etCapacity = findViewById<EditText>(R.id.capacity)
        val etContactNo = findViewById<EditText>(R.id.contact_no)
        val etMedicalService = findViewById<EditText>(R.id.medical_sevice)
        val progressBar = findViewById<ProgressBar>(R.id.progress_circular1)
        val signUpButton = findViewById<Button>(R.id.btn_sign_up)
        val certification = findViewById<EditText>(R.id.certification)
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
            val hospitalID = etHospitalID.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val dateOfEstablish = etDateOfEstablish.text.toString().trim()
            val adminID = etAdminID.text.toString().trim()
           // val department = etDepartment.text.toString().trim()
            val licenceNo = etLicenceNo.text.toString().trim()
            val capacity = etCapacity.text.toString().trim().toIntOrNull() ?: 0
            val contactNo = etContactNo.text.toString().trim()
            val certification =certification.text.toString().trim()
           // val medicalService = etMedicalService.text.toString().trim()

            // Validate inputs
            if (hospitalName.isEmpty() || licenceNo.isEmpty() || address.isEmpty() ||
                contactNo.isEmpty() || dateOfEstablish.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!dateOfEstablish.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { // Validate date format
                Toast.makeText(this, "Invalid date format (use YYYY-MM-DD)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create hospital object
            val hospital = Hospital(
                hospitalId = hospitalID,
                licenseNo = licenceNo,
                hospitalName = hospitalName,
                address = address,
                contactNo = contactNo,
                email = adminID, // Assuming Admin Email represents Hospital's Email
                capacity = capacity,
                dateOfEstablish = dateOfEstablish,
                adminId = adminID,
                certification = certification)
               // medicalServices = listOf(medicalService),
               // departments = listOf(mapOf("Department_Name" to department))


            progressBar.visibility = View.VISIBLE // Show ProgressBar

            // Send data to the server using Retrofit
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiClient.getApiService().saveHospital(hospital)
                    runOnUiThread {
                        progressBar.visibility = View.GONE // Hide ProgressBar
                        if (response.containsKey("message")) {
                            Toast.makeText(
                                this@Register_hospital,
                                response["message"],
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@Register_hospital, homepage_hospital::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@Register_hospital,
                                "Unexpected response: $response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        progressBar.visibility = View.GONE // Hide ProgressBar
                        Toast.makeText(this@Register_hospital, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
