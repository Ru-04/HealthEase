package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.hbb20.CountryCodePicker

lateinit var generate :Button
lateinit var codePicker :CountryCodePicker
lateinit var ph_txt :EditText
lateinit var progress_circular :ProgressBar
class GenerateOTP : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_generate_otp)
        generate = findViewById(R.id.generate)
        codePicker = findViewById(R.id.codePicker)
        ph_txt = findViewById(R.id.ph_txt)
        progress_circular = findViewById(R.id.progress_circular)

        progress_circular.visibility=View.GONE
        codePicker.registerCarrierNumberEditText(ph_txt)
        generate.setOnClickListener {
            if(!codePicker.isValidFullNumber()) {
                ph_txt.setError("Phone number is not valid")
            }
            else {
                val intent = Intent(this@GenerateOTP, Enter_otp::class.java)
                intent.putExtra("phone", codePicker.fullNumberWithPlus)
                startActivity(intent)
            }
        }

    }
}


