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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_day_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val allDayEventsRecyclerView = view.findViewById<RecyclerView>(R.id.all_day_events_recycler_view)
        val dayEventsRecyclerView = view.findViewById<RecyclerView>(R.id.day_events_recycler_view)
        val allDayEventsContainer = view.findViewById<View>(R.id.all_day_events_container)

        allDayEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        dayEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

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

        val transaction = parentFragmentManager.beginTransaction()

        if (isTablet) {
            transaction.replace(R.id.add_event_fragment_container, detailFragment)
        } else {
            transaction.replace(R.id.calendar_view_container, detailFragment)
        }

        transaction.addToBackStack(null)
        transaction.commit()
    }
}