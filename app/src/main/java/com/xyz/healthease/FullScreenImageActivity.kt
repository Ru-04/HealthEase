package com.xyz.healthease

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FullscreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageView = findViewById<ImageView>(R.id.fullscreenImageView)
        val imageUrl = intent.getStringExtra("imageUrl")
        val publicId = intent.getStringExtra("publicId")


        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).into(imageView)
        }
    }
}
