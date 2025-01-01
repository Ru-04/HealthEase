package com.xyz.healthease

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class Camera : AppCompatActivity() {

    private lateinit var captureIV: ImageView
    private lateinit var imageUrl: Uri

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                launchCamera()

            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied.
                Toast.makeText(this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show()
            }
        }

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            captureIV.setImageURI(imageUrl)
            // Set the captured image to the ImageView
            captureIV.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        imageUrl = createImageUri()
        captureIV = findViewById(R.id.captureImageView)
        val captureImgBtn = findViewById<Button>(R.id.captureImgBtn)
        captureImgBtn.setOnClickListener {
            checkCameraPermissionAndOpenCamera()

        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                Toast.makeText(this, "Camera permission is needed to take pictures.", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private fun launchCamera() {
        contract.launch(imageUrl)
    }

    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photos.png")
        return FileProvider.getUriForFile(
            this@Camera,
            "com.coding.healthease.FileProvider", // Replace with your authority
            image
        )
    }
}