package com.duckco.bloodconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fullNameEditText = view.findViewById<EditText>(R.id.fullName)
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val passwordEditText = view.findViewById<EditText>(R.id.password)

        view.findViewById<Button>(R.id.registerButton).setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val fullName = fullNameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(context, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Registration success, save user data to Firestore
                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser?.uid

                        if (userId != null) {
                            val user = hashMapOf(
                                "name" to fullName,
                                "email" to email
                                // Add other fields like bloodType here later
                            )

                            db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    // Navigate to the home screen after saving data
                                    findNavController().navigate(R.id.action_registerFragment_to_dashboardFragment)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error saving user data: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "Error: Could not get user ID.", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        // If registration fails, display a message to the user.
                        Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        view.findViewById<TextView>(R.id.loginText).setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}
