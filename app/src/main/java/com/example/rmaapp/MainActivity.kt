package com.example.rmaapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rmaapp.activities.CalendarActivity
import com.example.rmaapp.activities.ClockActivity
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

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_clock -> {
                    val intent = Intent(this, ClockActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_calendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    if (getIntent().extras != null) {
                        intent.putExtras(getIntent())
                    }
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            handleIntent(intent)
        } else {
            bottomNavigationView.selectedItemId = R.id.action_calendar
        }

        requestLocationPermission()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (intent?.getBooleanExtra("from_notification", false) == true) {
            bottomNavigationView.selectedItemId = R.id.action_calendar
        } else {
            bottomNavigationView.selectedItemId = R.id.action_calendar
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
