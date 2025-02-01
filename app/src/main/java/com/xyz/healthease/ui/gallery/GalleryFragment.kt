package com.xyz.healthease.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.xyz.healthease.GalleryA
import com.xyz.healthease.R
import com.xyz.healthease.camera
import com.xyz.healthease.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var add: ImageView
    private lateinit var gallery: ImageView
    private lateinit var galleryCam: ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        add = binding.root.findViewById(R.id.add)
        gallery=binding.root.findViewById(R.id.gallery)
        galleryCam = binding.root.findViewById(R.id.galleryCam)

        // Set click listener for the gallery camera ImageView
        galleryCam.setOnClickListener {
            val intent = Intent(activity, camera::class.java)
            startActivity(intent)
        }
        gallery.setOnClickListener {
            val intent = Intent(activity, GalleryA::class.java)
            startActivity(intent)
        }
        add.setOnClickListener {
            val intent = Intent(activity, camera::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}