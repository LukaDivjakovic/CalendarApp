package com.example.rmaapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.rmaapp.R
import com.google.android.material.appbar.MaterialToolbar
import java.time.LocalDate

class CalendarFragment : Fragment() {

    private var isTablet: Boolean = false
    private var currentDate: LocalDate = LocalDate.now()
    private var isWeekView: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        isTablet = resources.getBoolean(R.bool.isTablet)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        if (savedInstanceState == null) {
            updateView()
        }
    }

    private fun updateView() {
        val fragment = if (isWeekView) {
            WeekViewFragment.Companion.newInstance(currentDate)
        } else {
            DayViewFragment.newInstance(currentDate)
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.calendar_view_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (isTablet) {
            inflater.inflate(R.menu.calendar_menu_tablet, menu)
        } else {
            inflater.inflate(R.menu.calendar_menu_phone, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_event -> {
                childFragmentManager.beginTransaction()
                    .replace(R.id.calendar_view_container, AddEventFragment())
                    .addToBackStack(null)
                    .commit()
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
