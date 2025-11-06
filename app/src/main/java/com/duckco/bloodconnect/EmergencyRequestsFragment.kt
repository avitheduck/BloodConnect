package com.duckco.bloodconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class EmergencyRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmergencyRequestAdapter
    private val requestsList = mutableListOf<EmergencyRequest>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emergency_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.emergencyRequestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Pass the click handler to the adapter
        adapter = EmergencyRequestAdapter(requestsList) { request ->
            onAcceptRequest(request)
        }
        recyclerView.adapter = adapter

        fetchEmergencyRequests()
    }

    private fun onAcceptRequest(request: EmergencyRequest) {
        Toast.makeText(context, "Thank you for accepting the request!", Toast.LENGTH_LONG).show()

        // Create a Uri for the map intent
        val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(request.hospitalName)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        // Verify that the intent will resolve to an activity
        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(context, "Google Maps is not installed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchEmergencyRequests() {
        val db = FirebaseFirestore.getInstance()

        db.collection("emergency_requests")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("EmergencyRequests", "Listen failed.", e)
                    Toast.makeText(context, "Failed to load requests.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                requestsList.clear()
                for (document in snapshots!!) {
                    try {
                        val request = document.toObject<EmergencyRequest>()
                        requestsList.add(request)
                    } catch (ex: Exception) {
                        Log.e("EmergencyRequests", "Error converting document", ex)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }
}
