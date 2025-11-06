package com.duckco.bloodconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.Calendar

class ScheduleDonationFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var centerNameTextView: TextView
    private lateinit var centerAddressTextView: TextView
    private lateinit var datePicker: DatePicker
    private lateinit var scheduleButton: Button

    private var selectedCenter: Pair<String, DonationCenter>? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableMyLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_schedule_donation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomSheet = view.findViewById<View>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        centerNameTextView = view.findViewById(R.id.centerNameTextView)
        centerAddressTextView = view.findViewById(R.id.centerAddressTextView)
        datePicker = view.findViewById(R.id.datePicker)
        scheduleButton = view.findViewById(R.id.scheduleButton)

        // Set the minimum date to today
        datePicker.minDate = System.currentTimeMillis() - 1000

        scheduleButton.setOnClickListener {
            scheduleAppointment()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        // Move camera to Jaipur
        val jaipurLatLng = LatLng(26.9124, 75.7873)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jaipurLatLng, 12f))

        enableMyLocation()
        fetchDonationCenters()
        addJaipurHospitals()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val centerInfo = marker.tag as? Pair<String, DonationCenter> ?: return false
        selectedCenter = centerInfo

        centerNameTextView.text = centerInfo.second.name
        centerAddressTextView.text = centerInfo.second.address

        datePicker.visibility = View.VISIBLE
        scheduleButton.isEnabled = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        return true
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun addJaipurHospitals() {
        val jaipurHospitals = listOf(
            DonationCenter("Sawai Man Singh (SMS) Hospital", "Jawahar Lal Nehru Marg, Jaipur", GeoPoint(26.8954, 75.8037)),
            DonationCenter("Fortis Escorts Hospital", "Jawahar Lal Nehru Marg, Malviya Nagar, Jaipur", GeoPoint(26.8553, 75.8051)),
            DonationCenter("Narayana Multispeciality Hospital", "Sector 28, Kumbha Marg, Pratap Nagar, Jaipur", GeoPoint(26.8422, 75.8119)),
            DonationCenter("Mahatma Gandhi Hospital", "RIICO Institutional Area, Sitapura, Jaipur", GeoPoint(26.7644, 75.8732)),
            DonationCenter("Eternal Heart Care Centre (EHCC)", "Jawahar Circle, Jaipur", GeoPoint(26.8741, 75.8111))
        )

        for (hospital in jaipurHospitals) {
            hospital.location?.let { geoPoint ->
                val position = LatLng(geoPoint.latitude, geoPoint.longitude)
                val marker = mMap.addMarker(MarkerOptions().position(position).title(hospital.name))
                marker?.tag = Pair("jaipur_${hospital.name}", hospital)
            }
        }
    }

    private fun fetchDonationCenters() {
        db.collection("donation_centers").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val center = document.toObject(DonationCenter::class.java)
                center.location?.let {
                    val position = LatLng(it.latitude, it.longitude)
                    val marker = mMap.addMarker(MarkerOptions().position(position).title(center.name))
                    marker?.tag = Pair(document.id, center)
                }
            }
        }
    }

    private fun scheduleAppointment() {
        val center = selectedCenter ?: return
        val userId = auth.currentUser?.uid ?: return

        val calendar = Calendar.getInstance()
        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        val selectedDate = calendar.timeInMillis

        val appointment = Appointment(
            userId = userId,
            centerId = center.first,
            centerName = center.second.name,
            date = selectedDate
        )

        db.collection("appointments").add(appointment).addOnSuccessListener {
            Toast.makeText(context, "Appointment scheduled successfully!", Toast.LENGTH_SHORT).show()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            scheduleButton.isEnabled = false
            datePicker.visibility = View.GONE
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to schedule appointment.", Toast.LENGTH_SHORT).show()
        }
    }
}
