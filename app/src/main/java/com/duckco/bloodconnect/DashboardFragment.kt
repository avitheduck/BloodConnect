package com.duckco.bloodconnect

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class DashboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var pastDonationsRecyclerView: RecyclerView
    private lateinit var pastDonationsAdapter: PastDonationAdapter

    // TextViews for the urgent request card
    private lateinit var urgentRequestTitleTextView: TextView
    private lateinit var activeRequestsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TextViews for urgent request
        urgentRequestTitleTextView = view.findViewById(R.id.urgentRequestTitle)
        activeRequestsTextView = view.findViewById(R.id.activeRequests)

        // Navigation buttons
        view.findViewById<Button>(R.id.viewRequestsButton).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_emergencyRequestsFragment)
        }

        view.findViewById<Button>(R.id.scheduleAppointmentButton).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_scheduleDonationFragment)
        }

        view.findViewById<Button>(R.id.createRequestButton).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_createRequestFragment)
        }

        // Setup RecyclerView for past donations
        pastDonationsRecyclerView = view.findViewById(R.id.pastDonationsRecyclerView)
        pastDonationsRecyclerView.layoutManager = LinearLayoutManager(context)
        pastDonationsAdapter = PastDonationAdapter(emptyList())
        pastDonationsRecyclerView.adapter = pastDonationsAdapter

        // Fetch data
        fetchPastDonations()
        fetchUrgentRequestInfo()
    }

    private fun fetchUrgentRequestInfo() {
        db.collection("emergency_requests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("DashboardFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val requestCount = snapshots.size()
                    val mostRecentRequest = snapshots.documents[0].toObject(EmergencyRequest::class.java)

                    if (mostRecentRequest != null) {
                        urgentRequestTitleTextView.text = "Urgent: ${mostRecentRequest.bloodType} Blood Needed"
                    }
                    activeRequestsTextView.text = "$requestCount active requests"

                } else {
                    urgentRequestTitleTextView.text = "No Urgent Requests"
                    activeRequestsTextView.text = "0 active requests"
                }
            }
    }

    private fun fetchPastDonations() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("DashboardFragment", "User is not logged in.")
            return
        }

        db.collection("appointments")
            .whereEqualTo("userId", userId)
            .whereLessThan("date", Date()) // Filter for dates in the past
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val pastDonations = documents.toObjects(Appointment::class.java)
                pastDonationsAdapter.updateData(pastDonations)
            }
            .addOnFailureListener { exception ->
                Log.w("DashboardFragment", "Error getting past donations: ", exception)
            }
    }
}
