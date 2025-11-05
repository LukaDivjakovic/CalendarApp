package com.example.rmaapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.rmaapp.R
import com.example.rmaapp.fragments.AddEventFragment
import com.example.rmaapp.fragments.DayViewFragment
import com.example.rmaapp.fragments.WeekViewFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar
import java.time.LocalDate

class CalendarActivity : AppCompatActivity() {

    private var isTablet: Boolean = false
    private var currentDate: LocalDate = LocalDate.now()
    private var isWeekView: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        isTablet = resources.getBoolean(R.bool.isTablet)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
            updateView()
            if (isTablet) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.add_event_fragment_container, AddEventFragment())
                    .commit()
            }
            bottomNavigationView.selectedItemId = R.id.action_calendar
        }
    }

    private fun updateView() {
        val fragment = if (isWeekView) {
            WeekViewFragment.newInstance(currentDate)
        } else {
            DayViewFragment.newInstance(currentDate)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.calendar_view_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isTablet) {
            menuInflater.inflate(R.menu.calendar_menu_tablet, menu)
        } else {
            menuInflater.inflate(R.menu.calendar_menu_phone, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_event -> {
                if (!isTablet) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.calendar_view_container, AddEventFragment())
                        .addToBackStack(null)
                        .commit()
                }
                true
            }
            R.id.action_day_view -> {
                isWeekView = false
                updateView()
                true
            }
            R.id.action_week_view -> {
                isWeekView = true
                updateView()
                true
            }
            R.id.action_previous -> {
                currentDate = if (isWeekView) currentDate.minusWeeks(1) else currentDate.minusDays(1)
                updateView()
                true
            }
            R.id.action_next -> {
                currentDate = if (isWeekView) currentDate.plusWeeks(1) else currentDate.plusDays(1)
                updateView()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}