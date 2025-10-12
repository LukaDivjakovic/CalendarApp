package com.example.rmaapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rmaapp.database.AppDatabase
import com.example.rmaapp.database.entities.Event
import kotlinx.coroutines.launch
import java.time.LocalDate

class DayViewFragment : Fragment() {

    private lateinit var allDayEventsRecyclerView: RecyclerView
    private lateinit var dayEventsRecyclerView: RecyclerView
    private lateinit var allDayEventsContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().supportFragmentManager.setFragmentResultListener("event_saved_key", this) { _, _ ->
            loadEvents()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_day_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allDayEventsRecyclerView = view.findViewById(R.id.all_day_events_recycler_view)
        dayEventsRecyclerView = view.findViewById(R.id.day_events_recycler_view)
        allDayEventsContainer = view.findViewById(R.id.all_day_events_container)

        allDayEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        dayEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadEvents()
    }

    private fun loadEvents() {
        lifecycleScope.launch {
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay()
            val endOfDay = today.plusDays(1).atStartOfDay()

            val eventDao = AppDatabase.getDatabase(requireContext()).eventDao()
            val events = eventDao.getEventsForDate(startOfDay, endOfDay)

            val (allDayEvents, timedEvents) = events.partition { it.isAllDay }

            if (allDayEvents.isNotEmpty()) {
                allDayEventsContainer.visibility = View.VISIBLE
                allDayEventsRecyclerView.adapter = EventAdapter(allDayEvents, ::onEventClicked)
            } else {
                allDayEventsContainer.visibility = View.GONE
            }

            dayEventsRecyclerView.adapter = EventAdapter(timedEvents, ::onEventClicked)
        }
    }

    private fun onEventClicked(event: Event) {
        val isTablet = resources.getBoolean(R.bool.isTablet)
        val detailFragment = EventDetailFragment.newInstance(event)

        if (isTablet) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.add_event_fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        } else {
            parentFragmentManager.beginTransaction()
                .replace(R.id.calendar_view_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}