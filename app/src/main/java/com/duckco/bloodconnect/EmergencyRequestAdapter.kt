package com.duckco.bloodconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmergencyRequestAdapter(
    private var requestsList: MutableList<EmergencyRequest>,
    private val onAcceptClicked: (EmergencyRequest) -> Unit
) :
    RecyclerView.Adapter<EmergencyRequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val patientName: TextView = view.findViewById(R.id.patientNameTextView)
        val bloodType: TextView = view.findViewById(R.id.bloodTypeTextView)
        val hospitalName: TextView = view.findViewById(R.id.hospitalNameTextView)
        val location: TextView = view.findViewById(R.id.locationTextView) // Added location TextView
        val acceptButton: Button = view.findViewById(R.id.acceptRequestButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requestsList[position]
        holder.patientName.text = request.patientName
        holder.bloodType.text = "Blood Type: ${request.bloodType}"
        holder.hospitalName.text = request.hospitalName // Simplified text
        holder.location.text = "At: ${request.location}" // Set location text

        holder.acceptButton.setOnClickListener {
            onAcceptClicked(request)
        }
    }

    override fun getItemCount() = requestsList.size

    fun updateData(newRequests: List<EmergencyRequest>) {
        requestsList.clear()
        requestsList.addAll(newRequests)
        notifyDataSetChanged() // Refresh the list
    }
}
