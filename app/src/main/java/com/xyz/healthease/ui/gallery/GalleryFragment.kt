package com.xyz.healthease.ui.gallery

import android.content.Intent
import android.graphics.Camera
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.xyz.healthease.R
import com.xyz.healthease.databinding.FragmentGalleryBinding
import com.xyz.healthease.homepage

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentGalleryBinding.inflate(inflater, container, false)
            val root: View = binding.root
            return root

        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}