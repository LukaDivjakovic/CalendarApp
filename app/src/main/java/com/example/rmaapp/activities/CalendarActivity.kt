package com.example.rmaapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rmaapp.R
import com.example.rmaapp.fragments.CalendarFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_calendar -> {
                    // Already in CalendarActivity
                    true
                }
                R.id.action_clock -> {
                    val intent = Intent(this, ClockActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CalendarFragment())
                .commit()
            bottomNavigationView.selectedItemId = R.id.action_calendar
        }
    }
}
