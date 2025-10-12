package com.example.rmaapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rmaapp.database.AppDatabase
import com.example.rmaapp.database.entities.Event
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayColumnFragment : Fragment() {

    private lateinit var date: LocalDate
    private var showOutline: Boolean = false
    private lateinit var allDayEventsRecyclerView: RecyclerView
    private lateinit var dayEventsRecyclerView: RecyclerView
    private lateinit var allDayEventsContainer: View
    private lateinit var dayHeaderText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            date = LocalDate.parse(it.getString("date"))
            showOutline = it.getBoolean("showOutline", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_day_column, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (showOutline && date.isEqual(LocalDate.now())) {
            view.background = ContextCompat.getDrawable(requireContext(), R.drawable.current_day_outline)
        }

        allDayEventsRecyclerView = view.findViewById(R.id.all_day_events_recycler_view)
        dayEventsRecyclerView = view.findViewById(R.id.day_events_recycler_view)
        allDayEventsContainer = view.findViewById(R.id.all_day_events_container)
        dayHeaderText = view.findViewById(R.id.day_header_text)

        allDayEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        dayEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val formatter = DateTimeFormatter.ofPattern("EEE d")
        dayHeaderText.text = date.format(formatter)

        refreshEvents()
    }

    fun refreshEvents() {
        lifecycleScope.launch {
            val startOfDay = date.atStartOfDay()
            val endOfDay = date.plusDays(1).atStartOfDay()

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

        val fragmentManager = if (isTablet) {
            requireActivity().supportFragmentManager
        } else {
            parentFragment?.parentFragmentManager
        } ?: return

        val containerId = if (isTablet) {
            R.id.add_event_fragment_container
        } else {
            R.id.calendar_view_container
        }

        // Pop the previous event detail from the back stack if it exists.
        fragmentManager.popBackStack("event_detail", FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Add the new fragment in a transaction that is added to the back stack.
        fragmentManager.beginTransaction()
            .replace(containerId, detailFragment)
            .addToBackStack("event_detail")
            .commit()
    }

    companion object {
        fun newInstance(date: LocalDate, showOutline: Boolean = false) = DayColumnFragment().apply {
            arguments = Bundle().apply {
                putString("date", date.toString())
                putBoolean("showOutline", showOutline)
            }
        }
    }
}
