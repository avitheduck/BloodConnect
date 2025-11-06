package com.duckco.bloodconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore

class CreateRequestFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()
        return inflater.inflate(R.layout.fragment_create_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bloodTypeSpinner = view.findViewById<Spinner>(R.id.bloodTypeSpinner)
        val patientNameEditText = view.findViewById<EditText>(R.id.patientNameEditText)
        val hospitalNameEditText = view.findViewById<EditText>(R.id.hospitalNameEditText)
        val contactNumberEditText = view.findViewById<EditText>(R.id.contactNumberEditText)
        val additionalInfoEditText = view.findViewById<EditText>(R.id.additionalInfoEditText)
        val submitButton = view.findViewById<Button>(R.id.submitRequestButton)

        // Setup Spinner
        val bloodTypes = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bloodTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bloodTypeSpinner.adapter = adapter

        submitButton.setOnClickListener {
            val patientName = patientNameEditText.text.toString().trim()
            val hospitalName = hospitalNameEditText.text.toString().trim()
            val bloodType = bloodTypeSpinner.selectedItem.toString()
            val contactNumber = contactNumberEditText.text.toString().trim()
            val additionalInfo = additionalInfoEditText.text.toString().trim()

            if (patientName.isEmpty() || hospitalName.isEmpty() || contactNumber.isEmpty()) {
                Toast.makeText(context, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = EmergencyRequest(
                patientName = patientName,
                hospitalName = hospitalName,
                bloodType = bloodType,
                contactNumber = contactNumber,
                additionalInfo = additionalInfo
            )

            db.collection("emergency_requests")
                .add(request)
                .addOnSuccessListener {
                    Toast.makeText(context, "Request submitted successfully.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error submitting request: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
