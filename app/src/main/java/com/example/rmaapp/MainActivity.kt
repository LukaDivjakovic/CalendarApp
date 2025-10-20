package com.example.rmaapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var isTablet: Boolean = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isTablet = resources.getBoolean(R.bool.isTablet)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        val addEventContainer = findViewById<View?>(R.id.add_event_fragment_container)

        bottomNavigationView.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.action_clock -> {
                    selectedFragment = ClockFragment()
                    if (isTablet) {
                        addEventContainer?.visibility = View.GONE
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(mainLayout)
                        constraintSet.connect(R.id.fragment_container, ConstraintSet.END, R.id.main, ConstraintSet.END)
                        constraintSet.applyTo(mainLayout)

                        supportFragmentManager.findFragmentById(R.id.add_event_fragment_container)?.let {
                            supportFragmentManager.beginTransaction().remove(it).commit()
                        }
                    }
                }
                R.id.action_calendar -> {
                    selectedFragment = CalendarFragment()
                    if (isTablet) {
                        addEventContainer?.visibility = View.VISIBLE
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(mainLayout)
                        constraintSet.connect(R.id.fragment_container, ConstraintSet.END, R.id.guideline, ConstraintSet.START)
                        constraintSet.applyTo(mainLayout)

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.add_event_fragment_container, AddEventFragment())
                            .commit()
                    }
                }
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit()
            }
            true
        }

        if (savedInstanceState == null) {
            handleIntent(intent)
        } else {
            bottomNavigationView.selectedItemId = R.id.action_clock
        }

        requestLocationPermission()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (intent?.getBooleanExtra("from_notification", false) == true) {
            val eventId = intent.getIntExtra(EventNotificationReceiver.EVENT_ID_EXTRA, 0)
            val calendarFragment = CalendarFragment().apply {
                arguments = bundleOf("event_id" to eventId)
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, calendarFragment)
                .commit()
            bottomNavigationView.selectedItemId = R.id.action_calendar
        } else {
            bottomNavigationView.selectedItemId = R.id.action_clock
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}
