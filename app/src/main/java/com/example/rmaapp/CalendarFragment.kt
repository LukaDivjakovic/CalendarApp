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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class CalendarFragment : Fragment() {

    private var isTablet: Boolean = false
    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        locationHelper = LocationHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isTablet = resources.getBoolean(R.bool.isTablet)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        val eventId = arguments?.getInt("event_id", 0) ?: 0
        if (eventId > 0) {
            view.post {
                val eventDetailFragment = EventDetailFragment().apply {
                    arguments = bundleOf("event_id" to eventId)
                }
                childFragmentManager.beginTransaction()
                    .replace(R.id.calendar_view_container, eventDetailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        } else if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.calendar_view_container, DayViewFragment())
                .commit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                Toast.makeText(
                    requireContext(),
                    "Location: ${location.latitude}, ${location.longitude}",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission not granted.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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
                if (childFragmentManager.backStackEntryCount > 0) {
                    childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
                childFragmentManager.beginTransaction()
                    .replace(R.id.calendar_view_container, DayViewFragment())
                    .commit()
                true
            }
            R.id.action_week_view -> {
                if (childFragmentManager.backStackEntryCount > 0) {
                    childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
                childFragmentManager.beginTransaction()
                    .replace(R.id.calendar_view_container, WeekViewFragment())
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}