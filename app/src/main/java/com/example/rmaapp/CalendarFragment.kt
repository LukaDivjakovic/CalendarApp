package com.example.rmaapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar

class CalendarFragment : Fragment() {

    private var isTablet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        isTablet = resources.getBoolean(R.bool.isTablet)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        return view
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
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AddEventFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }
            R.id.action_day_view -> {
                Toast.makeText(requireContext(), "Day view clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_week_view -> {
                Toast.makeText(requireContext(), "Week view clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}