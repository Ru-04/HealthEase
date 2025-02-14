package com.xyz.healthease.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xyz.healthease.AddFamily
import com.xyz.healthease.Choice
import com.xyz.healthease.R
import com.xyz.healthease.Search
import com.xyz.healthease.camera
import com.xyz.healthease.databinding.FragmentHomeBinding
import com.xyz.healthease.ui.gallery.GalleryFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var upload: Button
    private lateinit var search: Button
    private lateinit var medivault: Button
    private lateinit var camera2: Button
    private lateinit var add: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        upload = binding.root.findViewById(R.id.upload)
        search = binding.root.findViewById(R.id.search_txt)
        medivault = binding.root.findViewById(R.id.medi)
        camera2 = binding.root.findViewById(R.id.load)
        add = binding.root.findViewById(R.id.add)

        upload.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_galleryFragment)
        }
        medivault.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_slide_show)
        }
        camera2.setOnClickListener {
            val intent = Intent(activity, camera::class.java)
            startActivity(intent)
        }
        search.setOnClickListener {
            val intent = Intent(activity, Search::class.java)
            startActivity(intent)
        }

        add.setOnClickListener {
            val intent = Intent(activity, Choice::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = parentFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment_content_homepage, fragment)
        fragmentTransaction.addToBackStack(null) // Optional: Add this transaction to the back stack
        fragmentTransaction.commit()
    }
}