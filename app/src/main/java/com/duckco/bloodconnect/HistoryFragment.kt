package com.duckco.bloodconnect

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: PastDonationAdapter // Reusing the same adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
        historyRecyclerView.layoutManager = LinearLayoutManager(context)
        historyAdapter = PastDonationAdapter(emptyList()) // Initialize with an empty list
        historyRecyclerView.adapter = historyAdapter

        fetchDonationHistory()
    }

    private fun fetchDonationHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("HistoryFragment", "User is not logged in.")
            return
        }

        db.collection("appointments")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING) // Show most recent first
            .get()
            .addOnSuccessListener { documents ->
                val appointments = documents.toObjects(Appointment::class.java)
                historyAdapter.updateData(appointments)
            }
            .addOnFailureListener { exception ->
                Log.w("HistoryFragment", "Error getting donation history: ", exception)
            }
    }
}
