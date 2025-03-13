package com.xyz.healthease.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xyz.healthease.ApiService
import com.xyz.healthease.Choice
import com.xyz.healthease.R
import com.xyz.healthease.Search
import com.xyz.healthease.camera
import com.xyz.healthease.databinding.FragmentHomeBinding
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.xyz.healthease.FamilyMembersActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var upload: Button
    private lateinit var search: Button
    private lateinit var medivault: Button
    private lateinit var camera2: Button
    private lateinit var add: Button
    private lateinit var familyList:Button

    private val familyUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val familyName = intent?.getStringExtra("family_name") ?: return
            val familyId = intent.getStringExtra("family_id") ?: return

            Log.d("HomeFragment", "Received family update: $familyName, $familyId")

            // Update family list and notify adapter

        }
    }

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
        familyList=binding.root.findViewById(R.id.manage_family)



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
        familyList.setOnClickListener{
            val intent = Intent(activity, FamilyMembersActivity::class.java)
            startActivity(intent)
        }


        return root
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("com.xyz.healthease.FAMILY_UPDATE")
        ContextCompat.registerReceiver(
            requireContext(),
            familyUpdateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED // Ensure it is not exported
        )
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(familyUpdateReceiver)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}