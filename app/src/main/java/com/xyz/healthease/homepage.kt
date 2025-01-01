package com.xyz.healthease

import android.content.Intent
import android.graphics.Camera
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.xyz.healthease.databinding.ActivityHomepageBinding
import com.xyz.healthease.ui.gallery.GalleryActivity
import com.xyz.healthease.ui.gallery.GalleryFragment
import com.xyz.healthease.ui.slideshow.SlideshowFragment

private lateinit var uploadbtn : Button
private lateinit var medivaultbtn : Button
private lateinit var camera3: ImageButton

class homepage : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomepageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHomepage.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_homepage)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        uploadbtn = findViewById(R.id.uploadbtn)
        medivaultbtn = findViewById(R.id.medivaultbtn)
        camera3 = findViewById(R.id.camera)

        uploadbtn.setOnClickListener {
            val intent = Intent(this@homepage,GalleryActivity::class.java)
            startActivity(intent)
        }
        medivaultbtn.setOnClickListener {
            val intent =Intent(this@homepage,SlideshowFragment::class.java)
            startActivity(intent)
        }
        camera3.setOnClickListener {
            val intent = Intent(this@homepage,Camera::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.homepage, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_homepage)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}