package com.example.rmaapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var isTablet: Boolean = false

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

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.action_clock
        }
    }
}