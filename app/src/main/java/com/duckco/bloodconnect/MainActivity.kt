package com.duckco.bloodconnect

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setupWithNavController(navController)

        // --- Persistent Login and Bottom Nav Visibility --- 
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                // User is logged in, show bottom nav and navigate to dashboard
                bottomNav.visibility = View.VISIBLE
                // Only navigate if we are not already on a dashboard screen
                if (navController.currentDestination?.id != R.id.dashboardFragment) {
                     navController.navigate(R.id.dashboardFragment)
                }
            } else {
                // User is not logged in, hide bottom nav and go to welcome screen
                bottomNav.visibility = View.GONE
                 if (navController.currentDestination?.id != R.id.welcomeFragment) {
                    navController.navigate(R.id.welcomeFragment)
                }
            }
        }

        // Control Bottom Nav visibility based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.dashboardFragment,
                R.id.emergencyRequestsFragment,
                R.id.profileFragment,
                R.id.myDonationsFragment -> bottomNav.visibility = View.VISIBLE
                else -> bottomNav.visibility = View.GONE
            }
        }
    }
}