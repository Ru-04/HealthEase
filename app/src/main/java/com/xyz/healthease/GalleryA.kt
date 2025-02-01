package com.xyz.healthease

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class GalleryA : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val ibgallery=findViewById<Button>(R.id.openGalleryButton)
        ibgallery.setOnClickListener {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_MEDIA_IMAGES)){

        }else{
            requestPermission.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
        }

    }

    val openGalleryLauncher:ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result->
            if(result.resultCode== RESULT_OK && result.data!=null){
                val imageBackGround:ImageView=findViewById(R.id.imageView)
                imageBackGround.setImageURI(result.data?.data)
            }
        }

    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
            permissions.entries.forEach{
                val permissionName=it.key
                val isGranted =it.value

                if(isGranted){
                    Toast.makeText(this@GalleryA,
                        "Permission granted now you can read the storage files.",
                        Toast.LENGTH_LONG).show()

                    val pickIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                }else{
                    if(permissionName== Manifest.permission.READ_MEDIA_IMAGES){
                        Toast.makeText(this@GalleryA,
                            "oops you just denied the permission.",
                            Toast.LENGTH_LONG).show()
                    }

                }
            }
        }
}

