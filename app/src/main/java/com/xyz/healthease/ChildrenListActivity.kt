package com.xyz.healthease

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChildrenListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_children_list)

        val children = intent.getParcelableArrayListExtra<ApiService.Child>("children") ?: emptyList<ApiService.Child>()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewChildren)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChildrenAdapter(children) { selectedChild ->
            // Save selected child ID in SharedPreferences
            val childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE)
            childPrefs.edit().putString("CHILD_ID", selectedChild.child_id).apply()

            // Go to child homepage
            val intent = Intent(this, child_homepage::class.java)
            startActivity(intent)
        }
    }
}