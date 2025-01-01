package com.xyz.healthease.ui.gallery

import android.content.Intent
import android.graphics.Camera
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.xyz.healthease.R
lateinit var gallerycan: ImageButton
class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_gallery)

        if (savedInstanceState == null) {
            val fragment = GalleryFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.nav_gallery, GalleryFragment())
                .commit()
        }
        gallerycan.setOnClickListener {
            val intent = Intent(this@GalleryActivity, Camera::class.java)
            startActivity(intent)
        }
    }
}